package com.jorgesg1986.matsuri.higher

import java.util.Properties

import com.jorgesg1986.matsuri.model.{AvroSerializer, StopStreaming, Tweet}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter._
import twitter4j.Status
import monix.eval.Task
import org.apache.kafka.common.serialization.StringSerializer
import monix.execution.Scheduler.Implicits.global

object HigherMatsuriTask {

  val logger: Logger = LogManager.getLogger(HigherMatsuriTask.getClass)

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("Third-Matsuri-Task")
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

              val res =
              for {
                taskTweet <- HigherBusinessLogic.processStatus[Task](status)
              } yield {
                taskTweet.map(tweet => producer.send(new ProducerRecord[String, Tweet](topic, tweet.id, tweet)))
              }

              res.runAsync

            }
          }
      }

    ssc.start()

    ssc.awaitTermination()

  }



}
