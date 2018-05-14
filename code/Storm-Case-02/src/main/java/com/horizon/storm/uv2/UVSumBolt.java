package com.horizon.storm.uv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
  
/** 
* 进行单线程汇总最终结果 
*  
* 
*/  
public class UVSumBolt extends BaseRichBolt {  
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
     /** 
     *  
     */  
     private static final long serialVersionUID = 1L;  
     OutputCollector collector;  
     Map<String, Integer> counts = new HashMap<String, Integer>();  

     @Override  
     public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {  
          this.collector = collector;  
     }  

     @Override  
     public void execute(Tuple input) {
            
          String url = input.getStringByField("url");
          Integer count = input.getIntegerByField("count");  


          counts.put(url, count);// 汇总每个访客  对应的UV数,这里可以通过map或者hbase作为去重的持久化操作

          for (Map.Entry<String, Integer> e : counts.entrySet()) {
               System.out.println(e.getKey()+"="+e.getValue());
          }
          System.out.println("============");
     }  
     
  
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
  
     }  
  
}  
