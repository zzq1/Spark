package com.neu.zzq.homework.Storm02.sid_url_count;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.text.SimpleDateFormat;


public class UVFmtBolt extends BaseBasicBolt {
     @Override  
     public void execute(Tuple input, BasicOutputCollector collector) {
          //2014-01-07 08:40:50     ABYH6Y4V4SCV00     http://www.jd.com/1.html
          String line = input.getStringByField("line");
          String date = line.split("\t")[0];  
          String sid = line.split("\t")[1];
          String url = line.split("\t")[2];

          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          collector.emit(new Values(url,sid));
     }  
       
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {
          declarer.declare(new Fields("url","sid"));
     }  
}  