package com.jorgesg1986.matsuri.akka

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import org.apache.logging.log4j.{LogManager, Logger}
import twitter4j.conf.ConfigurationBuilder
import twitter4j._

import scala.util.{Failure, Success, Try}

/**
  * A simple ActorPublisher[Status], that receives Status from the event bus
  * and forwards to the Source
  *
  */
//class StatusPublisherActor extends ActorPublisher[Tweet] {
//
//  val sub = context.system.eventStream.subscribe(self, classOf[Tweet])
//
//  override def receive: Receive = {
//    case s: Tweet => {
//      if (isActive && totalDemand > 0) onNext(s)
//    }
//    case _ =>
//  }
//
//  override def postStop(): Unit = {
//    context.system.eventStream.unsubscribe(self)
//  }
//
//}

class StatusGraphStage extends GraphStage[SourceShape[Status]] {

  val out: Outlet[Status] = Outlet("StatusSource")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageStatusListener(shape, out)

  override def shape: SourceShape[Status] = SourceShape.of(out)

}


class GraphStageStatusListener(shape: Shape, out: Outlet[Status]) extends GraphStageLogic(shape)  {

  val logger: Logger = LogManager.getLogger(classOf[GraphStageStatusListener])

  logger.info(s"Creating Twitter Factory and Stream")

  val factory = new TwitterStreamFactory(new ConfigurationBuilder().build())
  val twitterStream: TwitterStream = factory.getInstance()

  logger.info(s"Creating SimpleStatusListener")

  val simpleStatusListener = new SimpleStatusListener()

//  val query: FilterQuery = new FilterQuery().track("trump")

  twitterStream.addListener(simpleStatusListener)
  twitterStream.filter("Trump")

  while(simpleStatusListener.statusQ.size <= 1) { Thread.sleep(100L) }

  logger.info(s"Sample from Twitter Stream")

  setHandler(out, new OutHandler {
    override def onPull(): Unit = {

      var ready = false

      while(!ready) {
        Try(simpleStatusListener.statusQ.dequeue()) match {
          case Success(s) =>
            ready = true
            push(out, s)
          case Failure(err) =>
            logger.info(s"Error dequeueing status!! ${err.getMessage}")
            Thread.sleep(500L)
            ()
        }
      }

    }
  })

  override def postStop(): Unit = {
    super.postStop()

    twitterStream.cleanUp()
    twitterStream.shutdown()
  }

}

