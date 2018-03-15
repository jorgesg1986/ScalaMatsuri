package com.jorgesg1986.matsuri.model

case class Tweet(id:           String,
                 user:         String,
                 text:         String,
                 latitude:     Double,
                 longitude:    Double,
                 sentiment:    Int = 0) {

  override def toString: String = {
    s"$id|||$user|||$text|||$latitude|||$longitude|||$sentiment"
  }

}
