package com.generic.bank.modules

import com.generic.bank.fraud.api.{DefaultFraudApi, FraudApi}
import com.generic.bank.fraud.client.{FraudClient, SimpleFraudClient}
import com.generic.bank.notification.{DummyNotificationService, NotificationService}
import com.google.inject.AbstractModule

class MainModule extends AbstractModule {
  override def configure(): Unit = {
    install(new ConfigModule())
    install(new ActorSystemModule())

    bind(classOf[FraudClient]).to(classOf[SimpleFraudClient])
    bind(classOf[FraudApi]).to(classOf[DefaultFraudApi])
    bind(classOf[NotificationService]).to(classOf[DummyNotificationService])

    (): Unit
  }
}
