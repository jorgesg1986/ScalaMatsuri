package com.jorgesg1986.matsuri.second

import java.util.Properties

import com.jorgesg1986.matsuri.model.{AvroSerializer, StopStreaming, Tweet}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter._
import twitter4j.Status
import org.apache.kafka.common.serialization.StringSerializer

object SecondMatsuri {

  val logger: Logger = LogManager.getLogger(SecondMatsuri.getClass)

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

            statusIterator.flatMap { status =>

              BusinessLogic.processStatus(status)

              }

              .foreach{ tweet: Tweet =>

                producer.send(new ProducerRecord[String, Tweet](topic, tweet.id, tweet))

              }
          }
      }

    ssc.start()

    StopStreaming.stop(ssc, 5000L)

    ssc.awaitTermination()

  }


}