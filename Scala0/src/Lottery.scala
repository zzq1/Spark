import scala.util.control._
/**
  * Created by zzq on 2017/11/8.
  */
object Lottery {
  var inputNumber=new Array[Int](7)
  var winNumber=new Array[Int](7)
  def main(args: Array[String]): Unit ={
    buyLottery
    generateWinningNumbers
    getLevel
    levelMatch
  }
  def buyLottery():Unit={
    println("请输入1-35之间的七个整数：")
    var i=0
    while(i<7) {
      inputNumber(i)=scala.io.StdIn.readInt
      if(inputNumber(i)>=1 && inputNumber(i)<=35) {
        val loop = new Breaks
        loop.breakable {
          for(j<- 0 to i-1) {
            if(inputNumber(i)==inputNumber(j)) {
              println("输入重复，请重新输入：")
              i-=1
              loop.break()
            }
          }
        }
        i+=1
      } else {
        println("非法数值,重新输入(1-35的整数)：")
        i-=1
      }
    }
    println("用户所买的号码是：")
    inputNumber.sortWith(_<_).foreach(println)
  }
  def generateWinningNumbers():Unit={
    var i=0
    while(i<7) {
      winNumber(i)=(math.random*35+1).toInt
      val loop = new Breaks
      loop.breakable {
        for (j <- 0 to i-1) {
          if (winNumber(i)==winNumber(j)) {
            i-=1
            loop.break()
          }
        }
      }
      i+=1
    }
    println("中将号码为：")
    winNumber.sortWith(_<_).foreach(println)
  }
  var level=0
  def getLevel():Unit={
    printf("中奖等级为：")
    for(i<- 0 to 6) {
      val loop = new Breaks
      loop.breakable {
        for(j <- 0 to 6) {
          if(inputNumber(i)==winNumber(j)) {
            level+=1
            loop.break()
          }
        }
      }
    }
  }
  def levelMatch():Unit={
    level match{
      case 7 => println("1等奖")
      case 6 => println("2等奖")
      case 5 => println("3等奖")
      case 4 => println("4等奖")
      case 3 => println("5等奖")
      case 2 => println("6等奖")
      case 1 => println("7等奖")
      case 0 => println("很遗憾没有中奖！！！")
    }
  }
}