package com.jorgesg1986.matsuri.model

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

sealed trait Sink[T] {
  def sink(data: T): Unit
  def close(): Unit
}

class TweetKafkaSink(producer: KafkaProducer[String, Tweet], topic: String) extends Sink[Tweet] {

  override def sink(data: Tweet): Unit = {
    producer.send(new ProducerRecord[String, Tweet](topic, data.id, data))
  }

  override def close(): Unit = {
    producer.close()
  }

}