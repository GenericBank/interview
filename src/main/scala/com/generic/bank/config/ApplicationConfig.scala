package com.generic.bank.config

case class ApplicationConfig(
    messageFolder: ApplicationConfig.MessageFolder,
    fraudApi: ApplicationConfig.FraudApi,
    notificationApi: ApplicationConfig.NotificationApi
  )

object ApplicationConfig {
  case class MessageFolder(path: String, maxFileSize: Int)
  case class FraudApi(maxParallelCalls: Int)
  case class NotificationApi(maxParallelCalls: Int)
}
