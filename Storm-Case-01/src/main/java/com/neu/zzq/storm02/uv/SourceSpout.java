package com.neu.zzq.storm02.uv;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
  
/** 
* 模拟消息队列产生的数据 
*  
* 
*/  
public class SourceSpout extends BaseRichSpout {  
  
     ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();// 原子操作线程安全  
     private SpoutOutputCollector collector;  
     /** 
     *  
     */  
     private static final long serialVersionUID = 1L;  
     /** 
     * Called when a task for this component is initialized within a worker on the cluster. 
     * It provides the spout with the environment in which the spout executes. 
     * 
     * <p>This includes the:</p> 
     * 
     * @param conf The Storm configuration for this spout. This is the configuration provided to the topology merged in with cluster configuration on this machine. 
     * @param context This object can be used to get information about this task's place within the topology, including the task id and component id of this task, input and output information, etc. 
     * @param collector The collector is used to emit tuples from this spout. Tuples can be emitted at any time, including the open and close methods. The collector is thread-safe and should be saved as an instance variable of this spout object. 
     */  
     @Override  
     public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {  
  
//          String[] time = { "2015-10-12 08:40:50", "2015-10-12 08:40:50", "2015-10-12 08:40:50","2015-10-12 08:40:50" };  
          String[] sid = {  "ABYH6Y4V4SCV00", "ABYH6Y4V4SCV01", "ABYH6Y4V4SCV01","ABYH6Y4V4SCV03"  };  
          String[] url = {  "http://www.jd.com/1.html", "http://www.jd.com/2.html", "http://www.jd.com/3.html" ,"http://www.jd.com/3.html"};  

          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          for (int i = 0; i < 6; i++) {
               Random r = new Random();  
               int k = r.nextInt(4);  
//               queue.add(time[k] + "\t" + sid[k] + "\t" + url[k]);
               queue.add(sdf.format(new Date()) + "\t" + sid[k] + "\t" + url[k]);
               
          }  
          this.collector = collector;  
     }  
  
      
     @Override  
     public void nextTuple() {  
  
          if(queue.size()>0){  
               String line = queue.poll();  
               System.out.println("【nextTuple】="+line);
               this.collector.emit(new Values(line));  
          }  
          try {  
               //Thread.sleep(1000);  
          } catch (Exception e) {  
               e.printStackTrace();  
          }  
     }  
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
          declarer.declare(new Fields("line"));  
     }  
}  
