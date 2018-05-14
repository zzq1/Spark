package neu.ds


import neu.test.createDF.ReadFromParquet
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Dataset, SQLContext}

/**
  * Created by Administrator on 2017/6/10.
  */
object DataSetDemo {
  case  class  Person(name: String, age: Long)
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("ReadFromParquet")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    //val root = ReadFromParquet.getClass.getResource("/")
    val dataframe = sqlContext.read.json("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\data\\people.json")
    import sqlContext.implicits._
    //映射为ds 对象，通过Person 对象的
    val ds: Dataset[Person] = dataframe.as[Person]
    //p.page 类型是确定的， 而 dataframe.filter("salary > 1000").show() 在运行时才能发觉异常
    val dsNew = ds.filter(p => p.age > 25)
    dsNew.show()
    //转换回来，泛型还在
    val rdd: RDD[Person] = dsNew.rdd
    //ds.filter(p => p.salary > 12500) 编译出错    
  }
}
