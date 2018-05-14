package com.horizon.storm.uv;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
  
/** 
* 进行多线程局部汇总，统计每天UV数 
*  
* 
*/  
public class UVDeepVisitBolt extends BaseRichBolt {  
     /** 
     *  
     */  
     private static final long serialVersionUID = 1L;  
  
     OutputCollector collector;  
     Map<String, Integer> counts = new HashMap<String, Integer>();// 每个task实例都会输出：  
                                                                                     // k为日期_sid,v  
                                                                                     // 为每个用户的PV  
  
     @Override  
     public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {  
          this.collector = collector;  
     }  
  
     /** 
     * 输入内容：
     *  2014-01-07 ABYH6Y4V4SCV 
     *  2014-01-07 ABYH6Y4V4SCV 
     *  2014-01-07 ACYH6Y4V4SCV 
     */  
     @Override  
     public void execute(Tuple input) {  
  
          String date = input.getStringByField("date");  
          String sid = input.getStringByField("sid");  
          String key = date + "_" + sid;  
          Integer count = 0;  
          try {  
               count = counts.get(key);  
               if (count == null) {  
                    count = 0;  
               }  
               count++;  
               counts.put(key, count);  
               this.collector.emit(new Values(date + "_" + sid, count));//每个访客  PV数  
               this.collector.ack(input);  
          } catch (Exception e) {  
               e.printStackTrace();  
               System.err.println("UVDeepVisitBolt is failure.date:" + date + ",sid:" + sid + ",uvCount" + count);   
               this.collector.fail(input);  
          }  
     }  
  
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
          declarer.declare(new Fields("date_sid", "count"));  
     }  
}  
