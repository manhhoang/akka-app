package com.scala.akka

import akka.actor.Actor

class WorkExecutor extends Actor {

  def receive = {
    case n: Int =>
      val result = n
      sender() ! Worker.WorkComplete(result)
  }

}