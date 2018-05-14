package com.esoft.traitDemo
import java.io.PrintStream
/**
  * Created by zzq on 2017/11/9.
  */
trait FileLogger extends Logger{
  val fileName: String
  val out = new PrintStream(fileName)
  def log(msg: String) {out.println(msg); out.flush()}
}

trait Logger {
  def log(msg: String)
}
object Testor {
  println()
//    def main(args: Array[String]): Unit = {
//      val acc = new {
//        val fileName = "myApp.log"
//      } with SavingAccount with FileLogger
//    }
}



