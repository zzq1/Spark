package com.neu.zzq.homework.Storm02.uv;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;


public class UVFmtBolt extends BaseBasicBolt {
     @Override  
     public void execute(Tuple input, BasicOutputCollector collector) {
          String line = input.getStringByField("line");
          String sid = line.split("\t")[1];
          String url = line.split("\t")[2];
          collector.emit(new Values(url,sid));
     }  
       
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {
          declarer.declare(new Fields("url","sid"));
     }  
}  