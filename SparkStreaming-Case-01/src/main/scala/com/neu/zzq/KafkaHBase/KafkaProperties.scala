package com.neu.zzq.KafkaHBase

/**
  * Created by zzq on 2017/12/17.
  */
object KafkaProperties {
  val REDIS_SERVER: String = "localhost"
  val REDIS_PORT: Int = 6379

  val KAFKA_SERVER: String = "localhost"
  //val KAFKA_ADDR: String = KAFKA_SERVER + ":9092"
  val KAFKA_ADDR: String ="171.17.11.156:9092,172.17.11.155:9092,172.17.11.154:9092"
  //val KAFKA_USER_TOPIC: String = "user_events"
  val KAFKA_USER_TOPIC: String = "tttt"
  val KAFKA_RECO_TOPIC: String = "reco4"
}
