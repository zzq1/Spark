package com.neu.zzq.homework.Storm02.uv;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
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
public class UVDeepVisitBolt extends BaseBasicBolt {
     Map<String, Integer> counts = new HashMap<String, Integer>();
     Set<String> set = new HashSet<String>();

     @Override  
     public void execute(Tuple input, BasicOutputCollector collector) {
          String url = input.getStringByField("url");
          String sid = input.getStringByField("sid");
          String key = url + "_" + sid;
          Integer count = 0;
          set.add(sid);
          collector.emit(new Values(url, set.size()));//每个访客  PV数
     }  
  
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
          declarer.declare(new Fields("url", "count"));
     }  
}  
