package com.jorgesg1986.matsuri.higher


import java.util.Properties

import cats.Monad
import cats.data.OptionT
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.util.CoreMap
import twitter4j.{GeoLocation, Status}
import com.jorgesg1986.matsuri.model.{Sentiment, Tweet}

import scala.util.{Random, Try}

object HigherBusinessLogic {

  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  def processStatus[F[_] : Monad](status: Status): F[Option[Tweet]] = {

    val result =
      for {
        tweet <- OptionT.fromOption[F](checkLanguage(status, "EN"))
        location <- OptionT.fromOption[F](getLocation(tweet))
        sentiment <- OptionT.fromOption[F](getSentiment(tweet.getText))
      } yield {
        Tweet(status.getId.toString, status.getUser.getName, status.getText,
          location.getLatitude, location.getLongitude, sentiment)
      }

    result.value

  }

  def checkLanguage(status: Status, lang: String): Option[Status] = {
      if (status.getLang.equalsIgnoreCase(lang)) Some(status)
      else None
  }

  def getSentiment(input: String): Option[Int] = {
    Try(extractSentiment(input)).toOption
  }

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
    Try {
      new GeoLocation(status.getGeoLocation.getLatitude.floor,
        status.getGeoLocation.getLongitude.floor)
    }.toOption match {
      case r@Some(g) => r
      case _ => Some(new GeoLocation((30 + Random.nextInt(15)).toDouble,
        (-80 - Random.nextInt(40)).toDouble))
    }
  }



}
