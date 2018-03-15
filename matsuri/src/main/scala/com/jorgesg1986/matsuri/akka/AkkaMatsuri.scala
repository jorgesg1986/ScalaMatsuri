package com.jorgesg1986.matsuri.akka

import java.util.Properties

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import com.jorgesg1986.matsuri.model.{AvroSerializer, Tweet}
import com.jorgesg1986.matsuri.second.BusinessLogic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.logging.log4j.{LogManager, Logger}
import twitter4j.Status


object AkkaMatsuri {

  val logger: Logger = LogManager.getLogger(AkkaMatsuri.this)

  def main(args: Array[String]): Unit = {

    val topic = "matsuri"
    val brokers: String = "localhost:9092"

    val properties = new Properties()

    properties.put("bootstrap.servers", brokers)

    val producer = new KafkaProducer[String, Tweet](properties, new StringSerializer(), new AvroSerializer[Tweet])

    implicit val actorSystem: ActorSystem = ActorSystem("Matsuri-ActorSystem")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val sourceGraph: StatusGraphStage = new StatusGraphStage()

    val sourceStatus: Source[Status, NotUsed] = Source.fromGraph(sourceGraph)

    val flow: Flow[Status, Option[Tweet], NotUsed] = Flow.
      fromFunction(BusinessLogic.processStatus)

    val sinkGraph = new TweetGraphStage(producer, topic)

    val res: RunnableGraph[NotUsed] = sourceStatus
      .via(flow)
      .to(sinkGraph)

    res.run()

  }



}
