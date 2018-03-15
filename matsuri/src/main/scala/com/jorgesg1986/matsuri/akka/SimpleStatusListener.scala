package com.jorgesg1986.matsuri.akka

import org.apache.logging.log4j.{LogManager, Logger}
import twitter4j.{StallWarning, Status, StatusDeletionNotice, StatusListener}

import scala.collection.mutable

class SimpleStatusListener() extends StatusListener {

  val logger: Logger = LogManager.getLogger(classOf[SimpleStatusListener])

  var statusQ: mutable.Queue[Status] = mutable.Queue.empty[Status]

  override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

  override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}

  override def onStatus(status: Status): Unit = {
    statusQ.enqueue(status)
  }

  override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}

  override def onStallWarning(warning: StallWarning): Unit = {}

  override def onException(ex: Exception): Unit = {}

}