package ru.acme

import akka.actor.Actor
import spray.routing._
import spray.http._
import StatusCodes._
import spray.json._
import DefaultJsonProtocol._  

class TVIActor extends Actor with TVIService {
  def actorRefFactory = context
  def receive = runRoute(tvi_route)
}

// == Service API==================

/**
 * API 
 * 
 * get /ping = test healthy
 * get /transfer/from_acc/to_acc/amount  - transfer money
 * get /balance/accId  - get balance on account
 *  
 */
trait TVIService extends HttpService {

    import JsonProtocol._
  
    val tvi_route =
      pathPrefix("api") {
        path("ping") {
          complete("pong")
        } ~
        path("transfer" / Segment / Segment / DoubleNumber) {(acc_from, acc_to, amount) => ctx =>
             transfer(acc_from, acc_to, amount)
             ctx.complete(StatusCodes.OK)
        } ~
        path("balance" / Segment) { (acc_Id) => ctx =>
          getAccountBalance(acc_Id) match {
            case Some(ac) => ctx.complete(ac.toJson.toString())
            case _ => ctx.complete(StatusCodes.BadRequest)
          }
        }
      }

      def transfer(accFrom : String, accTo : String, amount : Double) : Unit = GLTxEntry.post(accFrom, accTo, amount)

      def getAccountBalance(accId : String) : Option[AccountState] = GLTxEntry.balance(accId)
}