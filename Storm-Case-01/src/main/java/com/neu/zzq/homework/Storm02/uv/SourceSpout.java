package com.neu.zzq.homework.Storm02.uv;

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

public class SourceSpout extends BaseRichSpout {  
  
     ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
     private SpoutOutputCollector collector;
     private Kpi k;
     private static final long serialVersionUID = 1L;
     @Override  
     public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

          try {
               String dataDir = "C:\\Users\\zzq\\Desktop\\Storm\\code\\Storm-Case-02\\logs\\";
               File file = new File(dataDir);
               Collection<File> listFiles = FileUtils.listFiles(file, new String[]{"log"},true);

               for (File f : listFiles) {
                    List<String> readLines = FileUtils.readLines(f);

                    for (String line : readLines) {
                         k = Kpi.parse(line);
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
