package com.horizon.ss.example01.util

object KafkaRedisProperties {
  val REDIS_SERVER: String = "localhost"
  val REDIS_PORT: Int = 6379

  val KAFKA_SERVER: String = "localhost"
  val KAFKA_ADDR: String = KAFKA_SERVER + ":9092"
  val KAFKA_USER_TOPIC: String = "user_events"
  val KAFKA_RECO_TOPIC: String = "reco4"

}