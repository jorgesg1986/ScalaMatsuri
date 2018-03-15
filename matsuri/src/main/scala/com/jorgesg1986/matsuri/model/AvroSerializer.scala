package com.jorgesg1986.matsuri.model

import java.io.ByteArrayOutputStream
import java.util

import com.sksamuel.avro4s.{AvroOutputStream, SchemaFor, ToRecord}
import org.apache.kafka.common.serialization.Serializer

class AvroSerializer[T : SchemaFor : ToRecord] extends Serializer[T] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def serialize(topic: String, data: T): Array[Byte] = {

    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[T](baos)
    output.write(data)
    output.close()

    baos.toByteArray

  }

  override def close(): Unit = {}

}
