package com.github.labs1904

import java.util.Properties

import com.github.labs1904.models.{EnrichedTweet, Tweet}
import org.apache.log4j.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.ling.CoreAnnotations

/**
 * Spark Structured Streaming app
 */
object SparkStreamingApp {
  lazy val logger: Logger = Logger.getLogger(this.getClass)

  val jobName = "Twitter Processing Application"

  //  TODO add more things from the schema
  val schema: StructType = new StructType()
    .add("text", StringType, nullable = true)
    .add("id", StringType, nullable = true)

  def main(args: Array[String]): Unit = {
      try {
        val spark = SparkSession.builder().appName(jobName).master("local[*]").getOrCreate()
        val bootstrapServers = "http://ec2-54-175-45-152.compute-1.amazonaws.com:9092"

        val df = spark
          .readStream
          .format("kafka")
          .option("kafka.bootstrap.servers", bootstrapServers)
          .option("subscribe", "raw-tweets")
          .option("startingOffsets", "earliest")
          .option("maxOffsetsPerTrigger", "200")
          .load()
          .selectExpr("CAST(value AS STRING)")

        import spark.implicits._
        val structured = compute(df).as[Tweet]

        val out = structured.mapPartitions(p => {
          val props = new Properties()
          props.setProperty("annotators", "tokenize ssplit pos parse sentiment")
          props.setProperty("tokenize.options", "untokenizable=allKeep")

          // TODO re-use pipeline intead of creating new every time. e.g. connection pool
          val pipeline = new StanfordCoreNLP(props)

          p.map(t => {
            val annotation = pipeline.process(t.text)
            val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])

            if(sentences.size() > 0) {
              val first = sentences.get(0)

              // TODO: Combine sentiment of entire tweet rather than sentences.
              val sentiment = first.get(classOf[SentimentCoreAnnotations.ClassName])

              EnrichedTweet(t.text, t.id, first.toString, sentiment)
            } else {
              EnrichedTweet(t.text, t.id, null, null)
            }
          })
        }).filter(df => df.sent != null)


        //  Write to output sink
        //  TODO switch to HDFS
        val query = out.writeStream
          .outputMode(OutputMode.Update())
          .format("console")
          .option("truncate", false)
          .trigger(Trigger.ProcessingTime("5 seconds"))
          .start()

        query.awaitTermination()
      } catch {
        case e: Exception => logger.error(s"$jobName error in main", e)
      }
  }

  def compute(df: DataFrame): DataFrame = {
    df.select(from_json(df("value"), schema) as "js").select("js.*")
  }
}
