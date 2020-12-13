package com.generic.bank.modules

import com.generic.bank.config.ApplicationConfig
import com.google.inject.AbstractModule
import pureconfig.ConfigSource
import pureconfig.generic.auto._

class ConfigModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ApplicationConfig])
      .toInstance(ConfigSource.default.at("app").loadOrThrow[ApplicationConfig])
  }
}
