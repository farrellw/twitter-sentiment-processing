package com.github.labs1904

import com.github.labs1904.models.WrappedReview
import org.apache.hadoop.hbase.util.Bytes
import org.apache.log4j.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types.{IntegerType, StringType, StructType, TimestampType}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * Spark Structured Streaming app
 *
 * Takes one argument, for Kafka bootstrap servers (ex: localhost:9092)
 */
object SparkStreamingApp {
  lazy val logger: Logger = Logger.getLogger(this.getClass)
  implicit def stringToBytes(str: String): Array[Byte] = Bytes.toBytes(str)
  implicit def bytesToString(bytes: Array[Byte]): String = Bytes.toString(bytes)

  val jobName = "SparkStreamingApp"
  val schema: StructType = new StructType()
    .add("marketplace", StringType, nullable = true)
    .add("customer_id", IntegerType, nullable = true)
    .add("review_id", StringType, nullable = true)
    .add("product_id", StringType, nullable = true)
    .add("product_parent", IntegerType, nullable = true)
    .add("product_title", StringType, nullable = true)
    .add("product_category", StringType, nullable = true)
    .add("star_rating", IntegerType, nullable = true)
    .add("helpful_votes", IntegerType, nullable = true)
    .add("total_votes", IntegerType, nullable = true)
    .add("vine", StringType, nullable = true)
    .add("verified_purchase", StringType, nullable = true)
    .add("review_headline", StringType, nullable = true)
    .add("review_body", StringType, nullable = true)
    .add("review_date", TimestampType, nullable = true)

  def main(args: Array[String]): Unit = {
    try {
      try {
        val spark = SparkSession.builder().appName(jobName).master("local[*]").getOrCreate()
        val bootstrapServers = "107.178.221.42:9092,35.225.13.175:9092,35.192.67.106:9092"
        val df = spark
          .readStream
          .format("kafka")
          .option("kafka.bootstrap.servers", bootstrapServers)
          .option("subscribe", "reviews")
          .option("startingOffsets", "earliest")
          .option("maxOffsetsPerTrigger", "200")
          .load()
          .selectExpr("CAST(value AS STRING)")

        val out = compute(df)

        out.printSchema()
        import spark.implicits._
        val structured = out.as[WrappedReview].map(_.js)

        val query = structured.writeStream
          .outputMode(OutputMode.Update())
          .format("console")
          .trigger(Trigger.ProcessingTime("5 seconds"))
          .start()

        query.awaitTermination()
      } catch {
        case e: Exception => logger.error(s"$jobName error in main", e)
      }
    }
  }

  def compute(df: DataFrame): DataFrame = {
    df.select(from_json(df("value"), schema) as "js")
  }
}
