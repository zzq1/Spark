package com.neu.zzq.storm02.uv2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
  

public class UVFmtBolt extends BaseRichBolt {  

     private static final long serialVersionUID = 1L;  
  
     OutputCollector collector;  
     @Override  
     public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {  
          this.collector = collector;  
     }  
     @Override  
     public void execute(Tuple input) {  
            
          //2014-01-07 08:40:50     ABYH6Y4V4SCV00     http://www.jd.com/1.html
          String line = input.getStringByField("line");
          String date = line.split("\t")[0];  
          String sid = line.split("\t")[1];
          String url = line.split("\t")[2];

          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
          try {
               date = sdf.format(new Date(sdf.parse(date).getTime()));  
               
//               this.collector.emit(new Values(date,sid));
               System.out.println("【nextTuple】】="+ sid + "111" + url);
               this.collector.emit(new Values(url,sid));
               this.collector.ack(input);
          } catch (Exception e) {  
               e.printStackTrace();  
               this.collector.fail(input);  
          }  
     }  
       
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
//          declarer.declare(new Fields("date","sid"));
          declarer.declare(new Fields("url","sid"));
     }  
}  