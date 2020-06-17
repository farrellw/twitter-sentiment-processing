package com.github.labs1904

import java.util.Properties

import com.github.labs1904.models.{Tweet, TweetWithSentiment}
import org.apache.log4j.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{ArrayType, BooleanType, FloatType, IntegerType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.ling.CoreAnnotations
import org.apache.spark.sql.streaming.OutputMode

// TODO: CICD of Dockerfile.
// TODO: for batchDF, don't nest it under the value argument
// TODO: Combine sentiment of entire tweet rather than the first sentence.
// TODO: re-use pipeline intead of creating new every time. e.g. connection pool
// TODO: fix warnings of untokenizable
/**
 * Spark Structured Streaming app
 */
object SparkStreamingApp {
  lazy val logger: Logger = Logger.getLogger(this.getClass)

  val OutputSerialization: String = "json"
  val JobName: String = "Twitter Processing Application"

  val userSchema: StructType = new StructType()
    .add("id", StringType, nullable = true)
    .add("name", StringType, nullable = true)
    .add("screen_name", StringType, nullable = true)
    .add("location", StringType, nullable = true)
    .add("url", StringType, nullable = true)
    .add("description", StringType, nullable = true)
    .add("verified", BooleanType, nullable = true)
    .add("followers_count", IntegerType, nullable = true)
    .add("friends_count", IntegerType, nullable = true)
    .add("created_at", StringType, nullable = true)

  val schema: StructType = new StructType()
    .add("text", StringType, nullable = true)
    .add("id", StringType, nullable = true)
    .add("created_at", StringType, nullable = true)
    .add("truncated", BooleanType, nullable = true)
    .add("coordinates",
      new StructType()
        .add("coordinates", ArrayType(FloatType))
        .add("type", StringType),
      nullable = true)
    .add("place",
      new StructType()
        .add("id", StringType, nullable = true)
        .add("place_type", StringType, nullable = true)
        .add("name", StringType, nullable = true)
        .add("full_name", StringType, nullable = true)
        .add("bounding_box",
          new StructType()
            .add("type", StringType, nullable = true)
            .add("coordinates", ArrayType(ArrayType(ArrayType(FloatType)))),
          nullable = true)
    )
    .add("user", userSchema, nullable = true)

  def main(args: Array[String]): Unit = {
    try {
      val spark = SparkSession.builder().appName(JobName).getOrCreate()

      // Set aws environment variables
      spark.sparkContext
        .hadoopConfiguration.set("fs.s3a.access.key", sys.env("ACCESS_KEY"))
      spark.sparkContext
        .hadoopConfiguration.set("fs.s3a.secret.key", sys.env("SECRET_KEY"))
      spark.sparkContext
        .hadoopConfiguration.set("fs.s3a.endpoint", "s3.amazonaws.com")

      val bootstrapServers = sys.env("BOOTSTRAP_SERVER")

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

      val query = df.writeStream.foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        batchDF.persist()

        val out = parseJsonFromString(batchDF).as[Tweet].mapPartitions(p => {
          val pipeline = createSentimentPipeline()

          p.map(t => {
            val a = pipeline.process(t.text)
            calculateSentiment(a, t)
          })
        }).filter(df => df.sentiment != null)

        batchDF.write.format(OutputSerialization).mode(SaveMode.Append).save("s3a://geospatial-project-data/will-spark-dump/raw/tweets")
        out.write.format(OutputSerialization).mode(SaveMode.Append).save("s3a://geospatial-project-data/will-spark-dump/transformed/tweets")

        // Program will not compile if batchDf.unpersist() is the last line within foreachBath
        batchDF.unpersist()
        println(batchId)
      }.outputMode(OutputMode.Append()).start()

      query.awaitTermination()
    } catch {
      case e: Exception => logger.error(s"$JobName error in main", e)
    }
  }

  def calculateSentiment(annotation: Annotation, t: Tweet): TweetWithSentiment = {
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])

    if (sentences.size() > 0) {
      val first = sentences.get(0)
      val sentiment = first.get(classOf[SentimentCoreAnnotations.ClassName])

      TweetWithSentiment(t.text, t.id, t.created_at, t.truncated, t.coordinates, t.place, t.user, sentiment)
    } else {
      TweetWithSentiment(t.text, t.id, t.created_at, t.truncated, t.coordinates, t.place, t.user, null)
    }
  }

  def parseJsonFromString(df: DataFrame): DataFrame = {
    df.select(from_json(df("value"), schema) as "js").select("js.*")
  }

  def createSentimentPipeline(): StanfordCoreNLP = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize ssplit pos parse sentiment")
    props.setProperty("tokenize.options", "untokenizable=allKeep")

    new StanfordCoreNLP(props)
  }
}
