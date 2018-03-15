package com.jorgesg1986.matsuri.higher

import cats.Id
import com.jorgesg1986.matsuri.model.Tweet
import com.jorgesg1986.matsuri.second.TestStatus
import org.scalatest.{FlatSpec, Matchers}

class HigherBusinessLogicTest extends FlatSpec with Matchers {

  val userJson = "{id:219570417,id_str:219570417,name:testUser}"

  "HigherBusinessLogic" should "return a properly structured tweet" in {

    val status = new TestStatus(12345L, userJson, "Good Better Amazing", Some(2, 2), "en")

    val tweet = HigherBusinessLogic.processStatus[Id](status)

    val expectedTweet = Some(Tweet("12345", "testUser", "Good Better Amazing", 2, 2, 1))

    assert(expectedTweet === tweet)
  }

  "HigherBusinessLogic" should "return None when the language is not English" in {

    val status = new TestStatus(12345L, userJson, "Good Better Amazing", Some(2, 2), "it")

    val tweet = HigherBusinessLogic.processStatus[Id](status)

    val expectedTweet = None

    assert(expectedTweet === tweet)
  }

}
