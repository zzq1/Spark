package com.horizon.storm.uv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
  
/** 
* 进行单线程汇总最终结果 
*  
* 
*/  
public class UVSumBolt extends BaseRichBolt {  
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
     /** 
     *  
     */  
     private static final long serialVersionUID = 1L;  
     OutputCollector collector;  
     Map<String, Integer> counts = new HashMap<String, Integer>();  
     //<k,v> 为每个访客  对应的PV数  
     @Override  
     public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {  
          this.collector = collector;  
     }  

     @Override  
     public void execute(Tuple input) {  
          int pv = 0;  
          int uv = 0;  
            
          String dateSid = input.getStringByField("date_sid");  
          Integer count = input.getIntegerByField("count");  
          // 访问日期若不是以今天开头并且访问日期大于当前日期,则计算新的一天的UV  
          String currDate = sdf.format(new Date());  
          try {  
               Date accessDate = sdf.parse(dateSid.split("_")[0]);  
               if (!dateSid.startsWith(currDate) && accessDate.after(new Date())) {  
                    counts.clear();  
               }  
          } catch (ParseException e1) {
               e1.printStackTrace();  
          }  
          counts.put(dateSid, count);// 汇总每个访客  对应的PV数,这里可以通过map或者hbase作为去重的持久化操作  
            
          for (Map.Entry<String, Integer> e : counts.entrySet()) {  
               if(dateSid.split("_")[0].startsWith(currDate)){  
                    uv++;  
                    pv+=e.getValue();  
               }  
          }  
          //保存到HBase或者数据库中  
          System.out.println(currDate + "的pv数为"+pv+",uv数为"+uv);  
     }  
     
  
     @Override  
     public void declareOutputFields(OutputFieldsDeclarer declarer) {  
  
     }  
  
}  
