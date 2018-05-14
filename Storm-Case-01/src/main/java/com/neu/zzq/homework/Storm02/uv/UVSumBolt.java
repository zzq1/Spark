package com.neu.zzq.homework.Storm02.uv;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.util.HashMap;
import java.util.Map;


public class UVSumBolt extends BaseBasicBolt {
     Map<String, Integer> counts = new HashMap<String, Integer>();

     @Override  
     public void execute(Tuple input, BasicOutputCollector collector) {

          String url = input.getStringByField("url");
          Integer count = input.getIntegerByField("count");
          counts.put(url, count);// 汇总每个访客  对应的UV数

          for (Map.Entry<String, Integer> e : counts.entrySet()) {
               System.out.println(e.getKey()+"="+e.getValue());
          }
          System.out.println("============");
     }  
     
  
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
  
     }  
  
}  
