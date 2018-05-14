package com.neu.zzq.homework.Storm04.UV;

import org.apache.commons.io.FileUtils;
import org.apache.storm.Config;
import com.neu.zzq.homework.Storm02.Kpi;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zzq on 2018/1/16.
 */
public class  BachSpout implements IBatchSpout {
    Kpi k;
    Fields fields;
    HashMap<Long, List<List<Object>>> batches = new HashMap();

    public BachSpout(Fields fields) {
        this.fields = fields;
    }

    @Override
    public void open(Map map, TopologyContext topologyContext) {
    }

    @Override
    public void emitBatch(long batchId, TridentCollector tridentCollector) {
        List<List<Object>> batch = (List) this.batches.get(Long.valueOf(batchId));
        if (null == batch) {
            batch = new ArrayList<List<Object>>();
            //读取日志文件列表
            String dataDir = "C:\\Users\\zzq\\Desktop\\Storm\\code\\Storm-Case-02\\logs\\";
            File file = new File(dataDir);
            Collection<File> listFiles = FileUtils.listFiles(file, new String[]{"log"}, true);
            for (File f : listFiles) {
                List<String> readLines = null;
                try {
                    readLines = FileUtils.readLines(f);
                    for (String ll : readLines) {
                        k = Kpi.parse(ll);
                        if (k.getIs_validate() && k.getHttp_referrer().contains("www")){
                           String line =  k.getRemote_addr() + "\t" + k.getHttp_referrer();
                           batch.add(new Values(line));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 文件已经处理完成,在末尾添加done和时间戳，避免重复读取
//                try {
//                    File srcFile = f.getAbsoluteFile();
//                    File destFile = new File(srcFile + ".done." + System.currentTimeMillis());
//                    FileUtils.moveFile(srcFile, destFile);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                this.batches.put(batchId, batch);
            }

        }
        for (List<Object> list : batch) {
            tridentCollector.emit(list);
        }
    }

    @Override
    public void ack(long batchId) {
        this.batches.remove(Long.valueOf(batchId));
    }

    @Override
    public void close() {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        conf.setMaxTaskParallelism(1);
        return conf;
    }

    @Override
    public Fields getOutputFields() {
        return this.fields;
    }
}
