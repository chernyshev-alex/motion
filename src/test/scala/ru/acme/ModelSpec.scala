package ru.acme

//import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import org.joda.time._
import scalikejdbc._

class ModelSpec extends Specification {

  DbSetup.initialize()

  "accounts balance" should {
      "be correct after transfer 10$ from acc_01 to acc_02" in { 
      
        // getting initial balances
        val v_01_before = GLTxEntry.balance("acc_01")
        val v_02_before = GLTxEntry.balance("acc_021")

        // transfer 10$ acc_01 -> acc_02  
        GLTxEntry.post("acc_01", "acc_02", 10.0)
        
        // getting resulted balances
        val v_01_after = GLTxEntry.balance("acc_01")
        val v_02_after = GLTxEntry.balance("acc_02")
        
        val r = v_01_before flatMap { before => v_01_after map { after =>   
               before.balance - after.balance mustEqual -10.0
               
           v_02_before flatMap { before => v_02_after map { after =>   
               before.balance - after.balance mustEqual 10.0
              }
           }
        }} 
        
        true
    }
  } 
}
