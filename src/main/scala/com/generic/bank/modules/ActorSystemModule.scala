package com.generic.bank.modules

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

class ActorSystemModule extends AbstractModule {
  override def configure(): Unit = {
    val actorSystem: ActorSystem = ActorSystem("interview", ConfigFactory.load())
    bind(classOf[ActorSystem]).toInstance(actorSystem)
    bind(classOf[ExecutionContext]).toInstance(actorSystem.dispatcher)
  }
}
