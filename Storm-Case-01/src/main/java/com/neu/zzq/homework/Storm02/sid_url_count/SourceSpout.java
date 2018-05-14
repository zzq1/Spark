package com.neu.zzq.homework.Storm02.sid_url_count;

import com.neu.zzq.homework.Storm02.Kpi;
import org.apache.commons.io.FileUtils;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
* 模拟消息队列产生的数据 
*  
* 
*/  
public class SourceSpout extends BaseRichSpout {  
  
     ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();// 原子操作线程安全  
     private SpoutOutputCollector collector;
     private Kpi k;
//     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
  
//          String[] sid = {  "ABYH6Y4V4SCV00","ABYH6Y4V4SCV00", "ABYH6Y4V4SCV01", "ABYH6Y4V4SCV01","ABYH6Y4V4SCV03","ABYH6Y4V4SCV03"  };
//          String[] url = {  "http://www.jd.com/1.html","http://www.jd.com/1.html", "http://www.jd.com/2.html", "http://www.jd.com/3.html" ,"http://www.jd.com/3.html" ,"http://www.jd.com/1.html"};
//
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//          for (int i = 0; i < 6; i++) {
//               queue.add(sdf.format(new Date()) + "\t" + sid[i] + "\t" + url[i]);
//
//          }
          try {
               String dataDir = "C:\\Users\\zzq\\Desktop\\Storm\\code\\Storm-Case-02\\logs\\";
               File file = new File(dataDir);
               Collection<File> listFiles = FileUtils.listFiles(file, new String[]{"log"},true);

               for (File f : listFiles) {
                    List<String> readLines = FileUtils.readLines(f);
                    for (String line : readLines) {
                         k = Kpi.parse(line);
                         //this.collector.emit(new Values(line));
                         if (k.getIs_validate()){
                              if (k.getHttp_referrer().contains("www")) {
                                   queue.add(sdf.format(new Date()) + "\t" + k.getRemote_addr() + "\t" + k.getHttp_referrer());
                              }
                         }
                    }
                    try {
                         File srcFile = f.getAbsoluteFile();
                         File destFile = new File(srcFile + ".done." + System.currentTimeMillis());
                         FileUtils.moveFile(srcFile, destFile);
                    } catch (Exception e) {
                         e.printStackTrace();
                    }
               }
          } catch (Exception e) {
               e.printStackTrace();
          }
          this.collector = collector;  
     }  
  
      
     @Override  
     public void nextTuple() {
//          try {
//               String dataDir = "C:\\Users\\zzq\\Desktop\\Storm\\code\\Storm-Case-02\\logs\\";
//               File file = new File(dataDir);
//               Collection<File> listFiles = FileUtils.listFiles(file, new String[]{"log"},true);
//
//               for (File f : listFiles) {
//                    List<String> readLines = FileUtils.readLines(f);
//
//                    for (String line : readLines) {
////                         k = Kpi.parse(line);
//                         this.collector.emit(new Values(line));
////                         if (k.getIs_validate()){
////                              queue.add(sdf.format(new Date()) + "\t" + k.getRemote_addr() + "\t" + k.getHttp_referrer());
////                         }
//                    }
//                    try {
//                         File srcFile = f.getAbsoluteFile();
//                         File destFile = new File(srcFile + ".done." + System.currentTimeMillis());
//                         FileUtils.moveFile(srcFile, destFile);
//                    } catch (Exception e) {
//                         e.printStackTrace();
//                    }
//               }
//          } catch (Exception e) {
//               e.printStackTrace();
//          }

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
