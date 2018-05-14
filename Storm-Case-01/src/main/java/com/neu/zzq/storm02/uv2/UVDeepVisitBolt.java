package com.neu.zzq.storm02.uv2;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     Set<String> set = new HashSet<String>();
  
     @Override  
     public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {  
          this.collector = collector;  
     }  

     @Override  
     public void execute(Tuple input) {  
  
          String url = input.getStringByField("url");
          String sid = input.getStringByField("sid");  
          String key = url + "_" + sid;
          Integer count = 0;  
          try {  
               set.add(sid);
               System.out.println("【nextTuple】】】="+url + "222" + set.size());
               this.collector.emit(new Values(url, set.size()));//每个访客  PV数
               this.collector.ack(input);  
          } catch (Exception e) {  
               e.printStackTrace();  
               this.collector.fail(input);
          }  
     }  
  
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
          declarer.declare(new Fields("url", "count"));
     }  
}  
