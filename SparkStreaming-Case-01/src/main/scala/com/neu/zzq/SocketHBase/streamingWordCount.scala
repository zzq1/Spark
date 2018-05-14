package com.neu.zzq.SocketHBase

/**
  * Created by zzq on 2017/12/13.
  */

import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.Try

object streamingWordCount {
  case class wordColunName(rank: Int, word: String, count: Int)
  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: NetworkWordCount <hostname> <port>")
      System.exit(1)
    }
    val sc = new SparkConf().setAppName("streamingWordCount").setMaster("local[4]")
    val ssc = new StreamingContext(sc, Seconds(1))
    val lines = ssc.socketTextStream(args(0), args(1).toInt, StorageLevel.MEMORY_AND_DISK_SER)
    val words = lines.flatMap(_.split(" "))
    var rank=0
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _).reduceByKeyAndWindow((a:Int,b:Int)=>(a+b),Seconds(10),Seconds(3))
    val res = wordCounts.transform(rdd=>{
      val sqlContext = new SQLContext(rdd.context)
      //import sqlContext.implicits._
      rdd.sortBy(_._2,false,1).map(x=>{
        rank+=1
        (rank,x._1,x._2)
        //new wordColunName(rank,x._1,x._2)
      }).filter(_._1<=5)
        //.toDF().alias("wordCounts").where("wordCounts.rank <= 5").select("wordCounts.rank","wordCounts.word","wordCounts.count").rdd
    })
    res.foreachRDD(rdd => {
      rdd.foreachPartition(partitionRecords => {
        val conf = HBaseConfiguration.create()
        conf.set("hbase.zookeeper.property.clientPort","2181")
        conf.set("hbase.zookeeper.quorum","172.17.11.156,172.17.11.155,172.17.11.154")
        val conn=ConnectionFactory.createConnection(conf)
        //从Connection获得 Admin 对象(相当于以前的 HAdmin)
        val admin=conn.getAdmin
        //本例将操作的表名
        val tableName=TableName.valueOf("T_spark") //获取HBase连接,分区创建一个连接，分区不跨节点，不需要序列化
        if(admin.tableExists(tableName)){
          println("Table exists!")
          //admin.disableTable(userTable)
          //admin.deleteTable(userTable)
          //exit()
        }else{
          val tableDesc=new HTableDescriptor(tableName)
          tableDesc.addFamily(new HColumnDescriptor("cf".getBytes))
          //tableDesc.addFamily(new HColumnDescriptor("cf2".getBytes))
          admin.createTable(tableDesc)
          println("Create table success!")
        }


        partitionRecords.foreach(s => {
          val table = conn.getTable(tableName)//获取表连接
          val p = new Put(s._1.toString.getBytes())
          p.addColumn("cf".getBytes,"word".getBytes,s._2.toString.getBytes)
          p.addColumn("cf".getBytes,"count".getBytes,s._3.toString.getBytes)
//          val put = new Put(Bytes.toBytes((s(0)).toString()))
//          put.add(Bytes.toBytes("f1"),Bytes.toBytes("word"),Bytes.toBytes(s(1).toString()))//word作为列名1
//          put.add(Bytes.toBytes("f2"),Bytes.toBytes("count"),Bytes.toBytes(s(2).toString()))//count作为列名2
//          (new ImmutableBytesWritable, put)
          Try(table.put(p)).getOrElse(table.close())//将数据写入HBase，若出错关闭table
          table.close()//分区数据写入HBase后关闭连接
        })
      })
    })
    res.print()
    ssc.start()
    ssc.awaitTermination()
  }
}