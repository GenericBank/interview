package com.generic.bank

import com.generic.bank.modules.MainModule
import com.generic.bank.runner.AppRunner
import com.google.inject.Guice

object Main extends App {
  val injector = Guice.createInjector(new MainModule())
  injector.getInstance(classOf[AppRunner]).run
}
