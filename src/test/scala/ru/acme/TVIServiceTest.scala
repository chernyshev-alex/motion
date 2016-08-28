package ru.acme

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

import akka.event.Logging
import spray.json._
import DefaultJsonProtocol._  

class TVIServiceSpec extends Specification with Specs2RouteTest with TVIService {

    val log = Logging.getLogger(system, this);
  
    import JsonProtocol._

    def actorRefFactory = system

    DbSetup.initialize()

    def json2Balance(resp : String) : Option[AccountState] = resp.parseJson.convertTo[Option[AccountState]]

    "service" should {
      "return pong status" in {
         Get("/api/ping") ~> tvi_route ~> check {
           responseAs[String] must contain("pong")           
         }
      }
    }

    "service" should {
      "return BadRequest to get balance error for unknown account" in {
         Get("/api/balance/unknown_accound_id") ~> tvi_route ~> check {
            status should equalTo(BadRequest)
         }
      }
    }
    
    "account balance" should {
      "be correct after transfer 10$ from acc_01 to acc_02" in {
        // get initial balance of acc_02
        Get("/api/balance/acc_01") ~> tvi_route ~> check {
          
          status should equalTo(OK)
          
          val acc_01_balance_before = json2Balance(responseAs[String])

          // get initial balance for acc_02
          Get("/api/balance/acc_02") ~> tvi_route ~> check {

            status should equalTo(OK)
            
            val acc_02_balance_before = json2Balance(responseAs[String])

            // transfer 10$ from acc_01 to acc_02
            Get("/api/transfer/acc_01/acc_02/10.00") ~> tvi_route ~> check {
              
              status should equalTo(OK)
            }

            // get resulted balance for acc_01
            Get("/api/balance/acc_01") ~> tvi_route ~> check {
  
              status should equalTo(OK)
  
              val acc_01_balance_after = json2Balance(responseAs[String])

              // get resulted balance for acc_02
              Get("/api/balance/acc_02") ~> tvi_route ~> check {

                status should equalTo(OK)
                
                val acc_02_balance_after = json2Balance(responseAs[String])

                // check  transfer result
                
                 val r = acc_01_balance_before flatMap { before => acc_01_balance_after map { after =>   
                     before.balance - after.balance should equalTo (-10.0)
                 }}

                 val r2 = acc_02_balance_after flatMap { before => acc_02_balance_before map { after =>   
                     after.balance - before.balance should equalTo (10.0)
                 }}
                
                 true
                 
              }                
            }
          }
        }
      }
    }
    
}
