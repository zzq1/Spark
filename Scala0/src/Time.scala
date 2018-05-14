/**
  * Created by zzq on 2017/11/9.
  */
class Time {
  var hours = ""
  var minutes = ""
  def this(hours:String,minutes:String){
    this
    this.hours = hours
    this.minutes = minutes
  }
  def before(other:Time):Boolean = {
    if (other.hours > this.hours && other.minutes >this.minutes) true else false
  }
  val t = new Time("10","20")

  def main(args: Array[String]): Unit = {
    println(t)
  }
}
