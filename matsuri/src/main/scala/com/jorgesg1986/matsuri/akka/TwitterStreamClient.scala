package com.jorgesg1986.matsuri.akka

import twitter4j._
import twitter4j.conf.ConfigurationBuilder

class TwitterStreamClient() {

  val factory = new TwitterStreamFactory(new ConfigurationBuilder().build())
  val twitterStream: TwitterStream = factory.getInstance()

  def init = {
//    twitterStream.setOAuthConsumer(CretentialsUtils.appKey, CretentialsUtils.appSecret)
//    twitterStream.setOAuthAccessToken(new AccessToken(CretentialsUtils.accessToken, CretentialsUtils.accessTokenSecret))
//    twitterStream.addListener(simpleStatusListener)
//    twitterStream.sample
  }

  def simpleStatusListener = new StatusListener() {
    def onStatus(s: Status): Unit = {

    }

    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}

    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}

    def onException(ex: Exception) {
      ex.printStackTrace
    }

    def onScrubGeo(arg0: Long, arg1: Long) {}

    def onStallWarning(warning: StallWarning) {}
  }

  def stop = {
    twitterStream.cleanUp
    twitterStream.shutdown
  }

}
