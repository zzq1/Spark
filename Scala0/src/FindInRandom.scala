import collection.mutable.BitSet
import scala.util.Random
/**
  * Created by ZQZ on 2017/11/17.
  */
object FindInRandom {
  def main(args: Array[String]): Unit = {
    var b1 = BitSet()
    var b2 = BitSet()
    val a= b1 ++ (1 to 100).par.toList
    val b= b2 ++ (1 to 10).map(_ => Random.nextInt(9) + 1).par.toList
    println(a--b)
    println((a--b).size)
  }
}

