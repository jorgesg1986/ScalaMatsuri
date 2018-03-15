package com.jorgesg1986.matsuri.first

import java.io.ByteArrayOutputStream
import java.util.Properties

import com.sksamuel.avro4s.AvroOutputStream
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.util.CoreMap
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter._
import twitter4j.{GeoLocation, Status}

import scala.util.{Random, Success, Try}
import com.jorgesg1986.matsuri.model.{Sentiment, StopStreaming, Tweet}

object FirstMatsuri {

  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  val logger: Logger = LogManager.getLogger(FirstMatsuri.getClass)

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("First-Matsuri")
      .setMaster("local[*]")
      .set("spark.executor.cores","2")
      .set("spark.executor.instances", "2")
      .set("spark.streaming.stopGracefullyOnShutdown","true")

    val topic: String = "matsuri"
    val brokers: String = "localhost:9092"

    val ssc = new StreamingContext(conf, Seconds(5))

    val properties = new Properties()

    properties.put("bootstrap.servers", brokers)
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")

    TwitterUtils
      .createStream(ssc, None, Seq("trump"))
      .foreachRDD{ (rdd: RDD[Status]) =>

        rdd
          .filter{ status => status.getLang.equalsIgnoreCase("EN") }
          .foreachPartition { statusIterator =>

            val producer = new KafkaProducer[String, Array[Byte]](properties)

            statusIterator.foreach { status =>

              // This is discouraged
              val location = getLocation(status).get

              val sentiment = getSentiment(status.getText).getOrElse(0)

              val tweet = Tweet(status.getId.toString, status.getUser.getName, status.getText,
                location.getLatitude, location.getLongitude, sentiment)

              val baos = new ByteArrayOutputStream()
              val output = AvroOutputStream.binary[Tweet](baos)
              output.write(tweet)
              output.close()

              val record = new ProducerRecord[String, Array[Byte]](topic, tweet.id, baos.toByteArray)

              producer.send(record)
            }
            producer.close()
          }
      }

    ssc.start()

    StopStreaming.stop(ssc, 5000L)

    ssc.awaitTermination()

  }

  def getSentiment(input: String): Option[Int] =
    Try(extractSentiment(input)).toOption

  private def extractSentiment(text: String): Int = {
    val (_, sentiment) = extractSentiments(text)
      .maxBy { case (sentence, _) => sentence.length }
    sentiment
  }

  def extractSentiments(text: String): List[(String, Int)] = {
    val annotation: Annotation = pipeline.process(text)
    val sentences: Array[CoreMap] = annotation
      .get(classOf[CoreAnnotations.SentencesAnnotation])
      .toArray(Array.empty[CoreMap])

    sentences
      .map { (sentence: CoreMap) =>
        (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree]))}
      .map { case (sentence, tree) =>
        (sentence.toString, Sentiment.toSentiment(RNNCoreAnnotations.getPredictedClass(tree))) }
      .toList
  }

  def getLocation(status: Status): Option[GeoLocation] = {
    Try{new GeoLocation(status.getGeoLocation.getLatitude.floor,
      status.getGeoLocation.getLongitude.floor)} match {
      case Success(gl) => Some(gl)
      case _ => Some(new GeoLocation((30 + Random.nextInt(15)).toDouble ,
        (- 80 - Random.nextInt(40)).toDouble ))
    }
  }

}
