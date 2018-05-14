package com.neu.zzq.fenci

import org.ansj.library.DicLibrary
import org.ansj.splitWord.analysis.ToAnalysis

/**
  * Created by taos on 2017/12/25.
  */
object SimpleDefineDict {
  def main(args: Array[String]): Unit = {
    DicLibrary.put(DicLibrary.DEFAULT,"D:/userDict/part-00000")
    var result= ToAnalysis.parse("\\r\\n\\n\\r\\n" +
      "1、计算机、电子等相关专业，学习能力强；\\n " +
      "2、3-5年及以上.NET/C#项目开发经验；\\n " +
      "3、扎实的C#基础，熟悉.Net4.0/4.5框架类库，熟悉VS开发工具。\\n " +
      "4、熟悉HTTP、HTTPS、WebSocket、消息队列常用通讯协议等，有RabbitMQ开发经验优先；\\n " +
      "5、熟悉当前主流编程思想和设计模式，熟练掌握.NET分层开发、MVC/MVVM开发思想及组件式开发模式。\\n熟悉多线程和异步技术；具有C/S客户端开发经验者优先。\\n " +
      "6、熟练使用Oracle数据库，有一定的查询优化和性能调优能力；\\n " +
      "7、有WPF、MEF、Bootstrap、clickonce等技术开发经验者优先；\\n " +
      "8、具有良好的编码习惯，热衷于技术钻研，有较强的系统分析能力和解决问题能力；\\n " +
      "9.团队、合作协作能力强，良好的自学能力。\\n福利：\\n五险一金，商业补充医疗保险，年终奖金，节日福利，交通补助，通讯补助，单身公寓，电影卡，生日卡，海关游泳卡，海关健身卡，公园年票，郊游拓展，党团活动，工会活动（电影，音乐会等）")
    println(result.getTerms)
  }
}
