package com.neu.zzq.homework.Storm04.sid_url_count;


import org.apache.commons.io.FileUtils;
import org.apache.storm.Config;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import com.neu.zzq.homework.Storm04.Kpi;

import java.io.File;
import java.util.*;

/**
 * Created by zzq on 2018/1/15.
 */
public class LogSpout implements IBatchSpout {
    Fields fields;
    HashMap<Long, List<List<Object>>> batches = new HashMap();
    Kpi k;

    public LogSpout(Fields fields) {
        this.fields = fields;
    }

    @Override
    public void open(Map map, TopologyContext topologyContext) {
    }
    @Override
    public void emitBatch(long batchId, TridentCollector tridentCollector){
        List<List<Object>> batch = (List) this.batches.get(Long.valueOf(batchId));
        if (null == batch) {
            batch = new ArrayList<List<Object>>();
            try {
                String dataDir = "C:\\Users\\zzq\\Desktop\\Storm\\code\\Storm-Case-02\\logs\\";
                File file = new File(dataDir);
                Collection<File> listFiles = FileUtils.listFiles(file, new String[]{"log"},true);

                for (File f : listFiles) {
                    List<String> readLines = FileUtils.readLines(f);
                    for (String ll : readLines) {
                        k = Kpi.parse(ll);
                        if (k.getIs_validate() && !k.getHttp_referrer().equals("-")){
                            String line = k.getRemote_addr() + "_" + k.getHttp_referrer();
                            batch.add(new Values(line));
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
