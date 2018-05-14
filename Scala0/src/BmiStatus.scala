/**
  * Created by zzq on 2017/11/8.
  */
object BmiStatus {
  def main(args: Array[String]): Unit = {
    print("输入体重：")
    var weight = scala.io.StdIn.readInt()
    print("输入身高：")
    var height = scala.io.StdIn.readInt()
    val a = (height + 0.2) / 100
    var b = weight / a
    if (b < 18.5) {
      print("过轻")
    } else if (b >= 18.5 && b < 24) {
      print("正常")
    } else {
      print("过重")
    }
  }
}
