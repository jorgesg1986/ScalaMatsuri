package com.jorgesg1986.minicluster

import java.util.Properties

import com.github.sakserv.minicluster.impl.{KafkaLocalBroker, ZookeeperLocalCluster}
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils


object MiniCluster {

  def main(args: Array[String]): Unit = {

    val miniZk: ZookeeperLocalCluster = new ZookeeperLocalCluster.Builder()
      .setPort(2181)
      .setTempDir("embedded_zookeeper")
      .setZookeeperConnectionString("localhost:2181")
      .setMaxClientCnxns(60)
      .setElectionPort(20001)
      .setQuorumPort(20002)
      .setDeleteDataDirectoryOnClose(false)
      .setServerId(1)
      .setTickTime(2000)
      .build()

    miniZk.cleanUp()

    miniZk.start()

    Thread.sleep(1000L)

    val kafkaProperties = new Properties()

    kafkaProperties.setProperty("offsets.topic.replication.factor", "1")

    val miniKafka: KafkaLocalBroker = new KafkaLocalBroker.Builder()
      .setKafkaHostname("localhost")
      .setKafkaPort(9092)
      .setKafkaBrokerId(0)
      .setKafkaProperties(kafkaProperties)
      .setKafkaTempDir("embedded_kafka")
      .setZookeeperConnectionString("localhost:2181")
      .build()

    miniKafka.cleanUp()

    miniKafka.start()

    def clean(zk: ZookeeperLocalCluster, kafka: KafkaLocalBroker): Unit = {

      kafka.stop()

      zk.stop()

    }

    sys.addShutdownHook(clean(miniZk, miniKafka))

    val zkUtils = ZkUtils.apply("localhost:2181", 100, 100, false)

    AdminUtils.createTopic(zkUtils, "matsuri", 1, 1)

    while(true) {

      Thread.sleep(3000L)

    }

  }


}
