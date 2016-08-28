package ru.acme

import scala.util.{Try, Success, Failure}
import scalikejdbc.config._
import scalikejdbc._

object DbSetup {

  var isInitialized = false 

  DBs.setup('default)
  
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = true,
    singleLineMode = true,
    printUnprocessedStackTrace = false,
    stackTraceDepth= 10,
    logLevel = 'info,
    warningEnabled = false,
    warningThresholdMillis = 3000L,
    warningLogLevel = 'warn )

  
  def initialize(force : Boolean = false) = {
    if (!isInitialized || force) {
      generateTables()
    }
    isInitialized = true
  }
  
  private def generateTables() : Unit = {
    DB autoCommit { implicit ss => 
      sql"""
        create sequence gledger_seq start with 1;
        create table gledger ( 
           txid bigint not null default nextval('gledger_seq') primary key, 
           created_at timestamp not null, 
           accId varchar(20) not null, 
           debet decimal(10, 2) not null default 0,
           credit decimal(10, 2) not null default 0); 

        insert into gledger(created_at, accId, debet, credit) values(current_timestamp, 'acc_01', 0, 100.0);
        insert into gledger(created_at, accId, debet, credit) values(current_timestamp, 'acc_02', 0, 100.0); 
                                 
      """.execute.apply()
    }
  }
  
}