package com.generic.bank.runner

import akka.actor.ActorSystem
import com.google.inject.Inject

class AppRunner @Inject()(system: ActorSystem) {
  def run = {
    system.terminate()
  }
}
