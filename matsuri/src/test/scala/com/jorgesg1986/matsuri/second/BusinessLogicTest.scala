package com.jorgesg1986.matsuri.second

import com.jorgesg1986.matsuri.model.Tweet
import org.scalatest.{FlatSpec, Matchers}
import twitter4j.{GeoLocation, Status}

class BusinessLogicTest extends FlatSpec with Matchers {

  val userJson = "{id:219570417,id_str:219570417,name:testUser}"

  it should "return the status when it has the required language" in {

    val status = new TestStatus(12345L, userJson, "text", None, "en")

    val checkedStatus: Option[Status] = BusinessLogic.checkLanguage(status, "EN")

    assert(checkedStatus.isDefined && checkedStatus.get.equals(status))

  }

  it should "return None when it doesn't have the required language" in {

    val status = new TestStatus(12345L, userJson, "text", None, "it")

    val checkedStatus: Option[Status] = BusinessLogic.checkLanguage(status, "EN")

    assert(checkedStatus.isEmpty)

  }

  it should "return None when evaluating the sentiment for an empty String" in {

    val sentiment: Option[Int] = BusinessLogic.getSentiment("")

    assert(sentiment.isEmpty)

  }

  it should "return Some(1) when evaluating the sentiment for a 'good' sentence" in {

    val sentiment: Option[Int] = BusinessLogic.getSentiment("Good Better Amazing")

    assert(sentiment.isDefined && sentiment.get == 1)

  }

  it should "return Some(-1) when evaluating the sentiment for a 'bad' sentence" in {

    val sentiment: Option[Int] = BusinessLogic.getSentiment("Bad Worse Horrible")

    assert(sentiment.isDefined && sentiment.get == -1)

  }

  it should "return Some(GeoLocation) when the status has a Location" in {

    val status = new TestStatus(12345L, userJson, "text", Some(2, 2), "en")

    val checkedLocation: Option[GeoLocation] = BusinessLogic.getLocation(status)

    assert(checkedLocation.isDefined && checkedLocation.get.equals(new GeoLocation(2, 2)))

  }

  it should "return Some(GeoLocation) when the status doesn't have a Location" in {

    val status = new TestStatus(12345L, userJson, "text", None, "en")

    val checkedLocation: Option[GeoLocation] = BusinessLogic.getLocation(status)

    def checkLocationLimit(gl: GeoLocation): Boolean = {

      val lat = gl.getLatitude
      val long = gl.getLongitude

      lat >= 30 && lat <= 45 &&
        long >= -120 && long <= -80
    }

    assert(checkedLocation.isDefined && checkLocationLimit(checkedLocation.get))

  }

  it should "return a properly formed Tweet" in {

    val status = new TestStatus(12345L, userJson, "Good Better Amazing", Some(2, 2), "en")

    val tweet = BusinessLogic.processStatus(status)

    val expectedTweet = Some(Tweet("12345", "testUser", "Good Better Amazing", 2, 2, 1))

    assert(tweet === expectedTweet)

  }



}
