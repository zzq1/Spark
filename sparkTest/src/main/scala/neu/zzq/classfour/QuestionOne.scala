package neu.zzq.classfour


import org.apache.spark.sql.{SaveMode, SparkSession}

/**
  * Created by zzq on 2017/11/30.
  */
object QuestionOne {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("QuestionOne").getOrCreate()
    val df = spark.read.csv("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\basketball\\leagues_NBA_2016_per_game_per_game.csv")
      .select("_c1","_c10","_c20","_c13","_c23","_c3").toDF("Player","FG%","FT%","3P11","TRB","Age").filter((_.get(0) != "Player"))
    df.cache()
    import org.apache.spark.sql.functions._
    //每场比赛的平均值
    val every = df.groupBy("Player").agg(("FG%","avg"),("FT%","avg"),("3p11","avg"),("TRB","avg"))
    //全部队员数据的平均值和标准差
    val all = df.agg(mean("FG%").as("mean(FG%)"),mean("FT%").as("mean(FT%)"),mean("3p11").as("mean(3p11)"),mean("TRB").as("mean(TRB)"),stddev("FG%"),stddev("FT%"),stddev("3p11"),stddev("TRB"))
    //sss.show()
    //求笛卡尔积
    val dkr = every.crossJoin(all)
   dkr.show()
    //分别求z-core
    val fg = dkr.withColumn("z_core_FG",round(((dkr("avg(FG%)")-dkr("mean(FG%)")) / dkr("stddev_samp(FG%)")),1))
    val ft = fg.withColumn("z_core_FT",round(((dkr("avg(FT%)")-dkr("mean(FT%)")) / dkr("stddev_samp(FT%)")),1))
    val ppp = ft.withColumn("z_core_3P",round(((dkr("avg(3p11)")-dkr("mean(3p11)")) / dkr("stddev_samp(3p11)")),1))
    val trb = ppp.withColumn("z_core_TRB",round(((dkr("avg(TRB)")-dkr("mean(TRB)")) / dkr("stddev_samp(TRB)")),1))
    //选择出结果，降序排列
    val fg_sort = trb.orderBy(trb("z_core_FG").desc).select("Player","z_core_FG")
    val ft_sort = trb.orderBy(trb("z_core_FT").desc).select("Player","z_core_FT")
    val ppp_sort = trb.orderBy(trb("z_core_3P").desc).select("Player","z_core_3P")
    val trb_sort = trb.orderBy(trb("z_core_TRB").desc).select("Player","z_core_TRB")
   fg_sort.coalesce(1).write.mode(SaveMode.Overwrite).csv("C:\\Users\\zzq\\Desktop\\spark第八次作业-20155120-周志强\\fg")
   ft_sort.coalesce(1).write.mode(SaveMode.Overwrite).csv("C:\\Users\\zzq\\Desktop\\spark第八次作业-20155120-周志强\\ft")
   ppp_sort.coalesce(1).write.mode(SaveMode.Overwrite).csv("C:\\Users\\zzq\\Desktop\\spark第八次作业-20155120-周志强\\3p")
   trb_sort.coalesce(1).write.mode(SaveMode.Overwrite).csv("C:\\Users\\zzq\\Desktop\\spark第八次作业-20155120-周志强\\trb")


    //trb.show()
    //fg_sort.show()
    //ft_sort.show()
    //ppp_sort.show()
    //trb_sort.show()
  }
}
