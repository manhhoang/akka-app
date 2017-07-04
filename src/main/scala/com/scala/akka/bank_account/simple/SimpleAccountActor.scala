package com.scala.akka.bank_account.simple

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

// Define message protocol
sealed trait Msg
case class DepositMoney(amt: BigDecimal) extends Msg
case class WithdrawMoney(amt: BigDecimal) extends Msg
case class ConfigMaxBalanceAllowance(maxBalance: BigDecimal) extends Msg
case class GetMaxBalanceAllowance() extends Msg
case class GetBalance() extends Msg

class SimpleAccountActor extends Actor{

  // Account State
  var balance: BigDecimal = 0.0
  var maximumBalance: BigDecimal = 0.0


  // Message handler
  override def receive: Receive = {
    case m: DepositMoney => {
      if (m.amt+balance > maximumBalance){ // Exceed maximum balance allowance
        println("Operation is not allowed: exceeded maximum balance allowance")
      }
      else {  // update balance
        balance = balance + m.amt
      }
    }

    case m: WithdrawMoney => {
      if (m.amt > balance){ // Not enough money
        println("Operation is not allowed: not enough money")
      }
      else { // update balance
        balance = balance - m.amt
      }
    }

    case m: ConfigMaxBalanceAllowance => maximumBalance = m.maxBalance
    case m: GetMaxBalanceAllowance => sender() ! maximumBalance // reply message back to sender
    case m: GetBalance => sender() ! balance // reply balance back to sender
    case _ => println("Invalid message")
  }

}

object SimpleAccountApp extends App {

  // Create akka system and account actor
  val system = ActorSystem("simple-account")
  val acc = system.actorOf(Props[SimpleAccountActor], "account-actor")

  // Test sending command message
  acc ! ConfigMaxBalanceAllowance(2000.00) // set maximum allowance
  acc ! DepositMoney(200.00)
  acc ! WithdrawMoney(100.00)

  // Test withdraw overdraft; Actor should not allow
  acc ! WithdrawMoney(101.00)

  // Test deposit more than maximum allowance
  acc ! DepositMoney(2000.00)

  // Test Get Balance
  // Set timeout for 5 seconds
  implicit val timeout = Timeout(5 seconds)
  val future = acc ? GetBalance()
  val result = Await.result(future, timeout.duration)

  println(s"Current balance: ${result}")
}
