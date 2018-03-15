package com.jorgesg1986.matsuri.model

import org.apache.spark.streaming.StreamingContext

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object StopStreaming {

  def stop(ssc: StreamingContext, duration: Long) = Future {
    Thread.sleep(duration)

    ssc.stop(true, true)

  }

}
