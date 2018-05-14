package com.neu.zzq.homework.Strom01;

import org.apache.commons.io.FileUtils;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.io.*;
import java.util.*;

/**
 * Created by zzq on 2018/1/10.
 */


public class  wordCount{

    // 定义一个喷头，用于产生数据。该类继承自BaseRichSpout
    public static class LogSpout extends BaseRichSpout {
        private SpoutOutputCollector _collector;
        private Map stormConf;

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
            this._collector = collector;
            this.stormConf = conf;
        }

        @Override
        public void nextTuple(){

            try {
                String dataDir = "C:\\Users\\zzq\\Desktop\\Flume\\conf\\log\\";
                File file = new File(dataDir);
                Collection<File> listFiles = FileUtils.listFiles(file, new String[]{"log"},true);

                for (File f : listFiles) {
                    List<String> readLines = FileUtils.readLines(f);
                    for (String line : readLines) {
                        this._collector.emit(new Values(line.trim()));
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
        @Override
        public void ack(Object id){
        }
        @Override
        public void fail(Object id){
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer){
            declarer.declare(new Fields("word"));
        }
    }

    public static class SplitSentence extends BaseRichBolt {
        private OutputCollector _collector;
        private TopologyContext context;

        /**
         *
         * @param stormConf
         * @param context
         * @param collector
         */
        @Override
        public void prepare(Map stormConf, TopologyContext context,OutputCollector collector) {
            this.context = context;
            this._collector = collector;
        }

        /**
         *
         * @param tuple
         */
        @Override
        public void execute(Tuple tuple){
            try {
                String sentence = tuple.getStringByField("word");
                String data[] = sentence.split(" ");
                if (data.length > 4) {
                    this._collector.emit(new Values(data[2]));
                    this._collector.ack(tuple);
                }
            } catch (Exception e){
                e.printStackTrace();
                this._collector.fail(tuple);
            }

        }

        /**
         *
         * @param declarer
         */
        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer){
            declarer.declare(new Fields("word"));
    }
}

    public static class WordCount extends BaseRichBolt {
        private static final long serialVersionUID = 1L;
        private OutputCollector _collector;
        private TopologyContext context;
        @Override
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.context = context;
            this._collector = collector;
        }
        Map<String, Integer> counts = new HashMap<String, Integer>();

        @Override
        public void execute(Tuple tuple){
            try{
                String word = tuple.getStringByField("word");
                Integer count = counts.get(word);
                if(count == null)
                    count = 0;
                count++;
                counts.put(word,count);
                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                    System.out.println(e.getKey()+"="+e.getValue());
                }
                System.out.println("=====================");
                this._collector.emit(new Values(word, count));
                this._collector.ack(tuple);
            } catch (Exception e){
                e.printStackTrace();
                this._collector.fail(tuple);
            }

        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer){
            declarer.declare(new Fields("word","count"));
        }
    }
    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("Spout", new LogSpout());
        builder.setBolt("split", new SplitSentence()).shuffleGrouping("Spout");
        builder.setBolt("count", new WordCount()).fieldsGrouping("split", new Fields("word"));

        Config conf = new Config();
        conf.setDebug(true);
        if(args != null && args.length > 0){
              conf.setNumWorkers(3);
              StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        }else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("word-count", conf, builder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();
        }
    }
}
