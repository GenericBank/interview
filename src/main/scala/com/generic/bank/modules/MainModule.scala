package com.generic.bank.modules

import com.google.inject.AbstractModule

class MainModule extends AbstractModule {
  override def configure(): Unit = {
    install(new ConfigModule())
    install(new ActorSystemModule())
  }
}
