package com.neu.zzq.homework.Storm02.sid_url_count;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class UVSumBolt extends BaseBasicBolt {
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
     Map<String, Integer> counts = new HashMap<String, Integer>();
     @Override  
     public void execute(Tuple input, BasicOutputCollector collector) {
            
          String url = input.getStringByField("sid_url");
          //Integer count = input.getIntegerByField("count");
          Integer count = counts.get(url);
          if(count == null)
               count = 0;
          count++;
          counts.put(url,count);

          for (Map.Entry<String, Integer> e : counts.entrySet()) {
               System.out.println(e.getKey()+"="+e.getValue());
          }
          System.out.println("============");
     }  
     
  
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
  
     }  
  
}  
