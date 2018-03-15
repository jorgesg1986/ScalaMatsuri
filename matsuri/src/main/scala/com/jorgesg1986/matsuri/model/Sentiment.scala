package com.jorgesg1986.matsuri.model

object Sentiment {

  val positive = 1
  val negative = -1
  val neutral = 0

  def toSentiment(sentiment: Int): Int =
    if(sentiment < 2)  Sentiment.negative
    else if (sentiment == 2) Sentiment.neutral
    else Sentiment.positive

}
