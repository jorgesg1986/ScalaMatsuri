package com.jorgesg1986.matsuri.akka

import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import com.jorgesg1986.matsuri.model.Tweet
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.logging.log4j.{LogManager, Logger}

class TweetGraphStage(producer: KafkaProducer[String, Tweet], topic: String) extends GraphStage[SinkShape[Option[Tweet]]] {

  val logger: Logger = LogManager.getLogger(classOf[TweetGraphStage])

  val in: Inlet[Option[Tweet]] = Inlet("StatusSource")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    logger.info("createLogic")

    setHandler(in, new InHandler {
      override def onPush(): Unit = {

        grab(in) match {
          case Some(tweet) => producer.send(new ProducerRecord[String, Tweet](topic, tweet.id, tweet))
          case None => ()
        }

        pull(in)

      }
    })

    override def preStart(): Unit = {
      super.preStart()
      logger.info("Pulling in PreStart")
      pull(in)
    }

  }

  override def shape: SinkShape[Option[Tweet]] = SinkShape.of(in)

}
