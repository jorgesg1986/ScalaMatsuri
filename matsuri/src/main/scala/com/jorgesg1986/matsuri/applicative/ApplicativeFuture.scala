package com.jorgesg1986.matsuri.applicative

import java.util.Properties

import cats.Applicative
import com.jorgesg1986.matsuri.model.{AvroSerializer, Tweet}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.Status
import cats.implicits._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ApplicativeFuture {

  val logger: Logger = LogManager.getLogger(ApplicativeFuture.getClass)

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("Second-Matsuri")
      .setMaster("local[*]")
      .set("spark.executor.cores","2")
      .set("spark.executor.instances", "2")

    val topic: String = "matsuri"
    val brokers: String = "localhost:9092"

    val ssc = new StreamingContext(conf, Seconds(1))

    val properties = new Properties()

    properties.put("bootstrap.servers", brokers)

    TwitterUtils
      .createStream(ssc, None, Seq("trump"))
      .foreachRDD{ (rdd: RDD[Status]) =>

        rdd
          .foreachPartition { statusIterator =>

            val producer = new KafkaProducer[String, Tweet](properties, new StringSerializer(), new AvroSerializer[Tweet])

            statusIterator.foreach { status: Status =>

              implicit val applicativeFuture: Applicative[Future] = Applicative[Future]

              for {
                futureTweet <- HigherApplicative.processStatus[Future](status)
              } yield {
                futureTweet.map(tweet => producer.send(new ProducerRecord[String, Tweet](topic, tweet.id, tweet)))
              }

            }
          }
      }

    ssc.start()

    ssc.awaitTermination()

  }


}
