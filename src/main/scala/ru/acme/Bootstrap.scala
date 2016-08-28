package ru.acme

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scalikejdbc._
import scalikejdbc.config._

object Bootstrap extends App {

  DBs.setup('default)

  implicit val ac = ActorSystem("tvi-system")

  val service = ac.actorOf(Props[TVIActor], "service")

  implicit val timeout = Timeout(5.seconds)
  
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}