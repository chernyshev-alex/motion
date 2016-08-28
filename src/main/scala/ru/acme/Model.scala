package ru.acme

import scalikejdbc._
import org.joda.time.DateTime
import spray.json._
import DefaultJsonProtocol._  

/**
 * Presents account state
 */
case class AccountState(accId : String, debet : Double,  credit : Double) {
  def balance = debet - credit
}

// AccountState json format 
object JsonProtocol extends DefaultJsonProtocol {
  implicit val balFormat = jsonFormat3(AccountState)
}

/**
 * Presents account transaction posting in general ledger
 */
case class GLTxEntry(txid : Long, created_at : Option[DateTime], accId : String, debet : Double, credit : Double)

object GLTxEntry extends SQLSyntaxSupport[GLTxEntry] {
  
  override val tableName = "gledger"
  
  //val gl = GLTxEntry.syntax("gl")
  
  def apply(c: SyntaxProvider[GLTxEntry])(rs: WrappedResultSet): GLTxEntry = apply(c.resultName)(rs)  
  
  def apply(c: ResultName[GLTxEntry])(rs: WrappedResultSet) : GLTxEntry = new GLTxEntry(
      rs.long("txid"), 
      rs.jodaDateTimeOpt("created_at"),
      rs.string("accid"), 
      rs.double("debet"), 
      rs.double("credit"))

//=== API ============================================
  
  /**
   * Transfer amount from/to accounts with the local db transaction
   */
  def post(acc_id_from : String, acc_id_to : String, amount :  Double) : Unit = {
    DB localTx { implicit ss =>
      sql"""
        insert into gledger(created_at, accid, debet, credit) values(current_timestamp, ${acc_id_from}, ${amount}, 0.0);   
        """.execute.apply() 
        
      sql"""
        insert into gledger(created_at, accid, debet, credit) values(current_timestamp, ${acc_id_to}, 0.0, ${amount});   
        """.execute.apply() 
    }
  }

  /**
   * Get account balance for account
   */
  def balance(accId : String)(implicit ss :  scalikejdbc.DBSession = autoSession) : Option[AccountState] = {
    def  mapToObj(map : Map[String, Any], accId : String) : AccountState = AccountState(accId, 
        map("D").asInstanceOf[java.math.BigDecimal].doubleValue(), 
        map("C").asInstanceOf[java.math.BigDecimal].doubleValue())
    
    sql"select sum(debet) as D, sum(credit) as C from gledger where accid=${accId}".map(_.toMap)
      .single.apply() match {
            case Some(map) if map.nonEmpty => Some(mapToObj(map, accId))
            case _  => None
          }
  }

}