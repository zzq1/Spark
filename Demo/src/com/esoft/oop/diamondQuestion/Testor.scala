package com.esoft.oop.diamondQuestion
/**
  * Created by zzq on 2017/11/9.
  */
trait Person {
  def speak(msg: String) = println(msg)
}
trait Teacher extends Person{
  val str: String = "I am Teacher"
  override def speak(msg: String) = println(str.concat(msg))
}
trait Student extends Person {
  val str2: String = "I am Student"
  override def speak(msg: String) = println(str2 + msg)
}
class Minxin private(msg: String) extends Person{
  def this() = this("Minxin")
}
object Minxin{
  def apply(msg: String) = new Minxin(msg) with Teacher with Student
}
object Testor {
  val t: Int = 10
  def main(args: Array[String]): Unit = {
    val pp = Minxin("zzz")
    pp.speak(" heihei")
  }
}
