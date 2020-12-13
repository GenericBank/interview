package com.generic.bank.config

import com.generic.bank.modules.ConfigModule
import com.google.inject.{Guice, Injector}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ConfigSpec extends AnyWordSpecLike with Matchers {
  import ConfigSpec._

  "Configuration" can {

    "ApplicationConfig" should {

      "be correctly parsed" in {

        val injector: Injector = Guice.createInjector(new ConfigModule())
        val result = injector.getInstance(classOf[ApplicationConfig])

        result shouldBe applicationConfig

      }
    }
  }
}

object ConfigSpec {

  val applicationConfig: ApplicationConfig = ApplicationConfig(
    messageFolder = ApplicationConfig.MessageFolder("/messages")
  )

}
