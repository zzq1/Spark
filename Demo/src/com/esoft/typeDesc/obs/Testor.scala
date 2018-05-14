package com.esoft.typeDesc.obs

/**
  * Created by zzq on 2017/11/9.
  */
class IntStore(private var value: Int) extends Subject with DefaultHandles{
  def get : Int = value
  def set(newValue: Int): Unit = {
    value = newValue
    notifyListenners()
  }

  override def toString: String = "IntStore("+ value +")"
}

object Testor {
  def main(args: Array[String]): Unit = {
    val x = new IntStore(5)
    val handle = x.attach(println)
    x.set(2)
  }
}