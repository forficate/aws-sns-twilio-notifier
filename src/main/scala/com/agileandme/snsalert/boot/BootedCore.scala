package com.agileandme.snsalert.boot

import akka.actor.ActorSystem
import com.agileandme.snsalert.core.Core

trait BootedCore extends Core {
  implicit val system = ActorSystem("akka-spray")
  sys.addShutdownHook(system.shutdown())
}
