package com.jorgesg1986.matsuri.second

import java.util.Date

import twitter4j._

class TestStatus(id: Long, jsonUser: String, text: String, location: Option[(Double, Double)], lang: String)
  extends Status {
  override def getWithheldInCountries: Array[String] = Array.empty[String]

  override def getQuotedStatus: Status = this

  override def getUser: User = TwitterObjectFactory.createUser(jsonUser)

  override def getId: Long = id

  override def getDisplayTextRangeEnd: Int = 0

  override def getCreatedAt: Date = new Date()

  override def getFavoriteCount: Int = 0

  override def isPossiblySensitive: Boolean = false

  override def getContributors: Array[Long] = Array.empty[Long]

  override def getGeoLocation: GeoLocation = {
    location match {
      case Some(pair) => new GeoLocation(pair._1, pair._2)
      case _ => null
    }
  }

  override def getCurrentUserRetweetId: Long = 0L

  override def getInReplyToStatusId: Long = 0L

  override def getQuotedStatusId: Long = 0L

  override def isRetweeted: Boolean = false

  override def isTruncated: Boolean = false

  override def getRetweetedStatus: Status = this

  override def getLang: String = lang

  override def getRetweetCount: Int = 0

  override def getText: String = text

  override def isFavorited: Boolean = false

  override def getPlace: Place = ???

  override def getScopes: Scopes = ???

  override def getInReplyToUserId: Long = 0L

  override def isRetweetedByMe: Boolean = false

  override def getSource: String = ""

  override def getInReplyToScreenName: String = ""

  override def getDisplayTextRangeStart: Int = 0

  override def isRetweet: Boolean = false

  override def getHashtagEntities: Array[HashtagEntity] = Array.empty[HashtagEntity]

  override def getURLEntities: Array[URLEntity] = Array.empty[URLEntity]

  override def getSymbolEntities: Array[SymbolEntity] = Array.empty[SymbolEntity]

  override def getUserMentionEntities: Array[UserMentionEntity] = Array.empty[UserMentionEntity]

  override def getMediaEntities: Array[MediaEntity] = Array.empty[MediaEntity]

  override def getRateLimitStatus: RateLimitStatus = ???

  override def getAccessLevel: Int = 0

  override def compareTo(o: Status): Int = ???
}
