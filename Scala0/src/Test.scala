/**
  * Created by zzq on 2017/11/7.
  */
object Test {
  def main(args: Array[String]): Unit = {
    def getUserAage(age: Int = 15): String = {
      if(age > 18) {
        return "adult"
      } else {
        return "teenager"
      } //if 或者 else 的结果就是这一行的返回值
    }
    def  bmiStatus(weight: Int,height: Int):Unit = {
      val a = (height + 0.2)/100
      var b = weight/a
      if (b <18.5){
        return "过轻"
      } else if (b >=18.5 && b <24){
        return "正常"
      } else {
        return "过重"
      }
    }
    println(bmiStatus(52,155))
    //Assert("Normal weight" ==  normal,"Expected Normal Weight, Got"+ normal)

    for (i <- 1 to 3; j <- 1 to 3 if i != j)
      println ((10  * i + j) + " " )
  }
}
