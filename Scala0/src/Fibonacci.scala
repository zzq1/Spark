/**
  * Created by zzq on 2017/11/16.
  */
object Fibonacci {
  @annotation.tailrec
  def fib(n:Int,a:Int,b:Int): Int ={
    if(n==1) return a
    else if (n == 2) b
    else fib(n-1,b,a+b)
  }
  def main(args: Array[String]): Unit = {
    println("请输入所求第n个斐波那契数：")
    var a = scala.io.StdIn.readInt()
    println(fib(a,0,1))
  }
}
