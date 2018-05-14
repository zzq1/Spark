package com.neu.zzq.homework.Storm03;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.shade.com.google.common.collect.Maps;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.Config;
import org.apache.storm.tuple.Values;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zzq on 2018/1/14.
 */
public class KafkaStormHBase {
    public static class SentenceSplitBolt extends BaseBasicBolt {
        @Override
        public void execute(Tuple input, BasicOutputCollector collector) {
            String sentence = input.getStringByField("str");
            String[] words = sentence.split(" ");

            if (words.length > 0) {
                for (String word : words) {
                    collector.emit(new Values(words));
                }
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word"));
        }
    }

    public static class CountBolt extends BaseBasicBolt {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        @Override
        public void execute(Tuple input, BasicOutputCollector collector) {
            String word = input.getStringByField("word");
            Integer count = counts.get(word);
            if(count == null)
                count = 0;
            count++;
            counts.put(word, count);
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                System.out.println(e.getKey()+"="+e.getValue());
            }
            System.out.println("=====================");
            collector.emit(new Values(word, count));
            collector.emit(new Values(word, String.valueOf(count)));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word", "count"));
        }
    }
    public static void main(String[] args) throws AlreadyAliveException,
            InvalidTopologyException, InterruptedException ,AuthorizationException,IOException{
        TopologyBuilder builder = new TopologyBuilder();
        SpoutConfig spoutConf = new SpoutConfig(new ZkHosts("172.17.11.156:2181,172.17.11.155:2181,172.17.11.154:2181"),
                "stormcase", "/test", UUID.randomUUID().toString());
        //spoutConf.forceFromStart = true;
        spoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConf);

        builder.setSpout("spout", kafkaSpout, 5);
        builder.setBolt("split", new SentenceSplitBolt(),8).shuffleGrouping("spout");
        builder.setBolt("count", new CountBolt()).fieldsGrouping("split", new Fields("word"));

        Configuration confh = HBaseConfiguration.create();
        confh.set("hbase.rootdir", "hdfs://172.17.11.156:9000/hbase");
        confh.set("hbase.zookeeper.quorum", "172.17.11.156:2181");

        HBaseAdmin admin = new HBaseAdmin(confh);
        HTableDescriptor tableDescriptor = new HTableDescriptor("kafkastormhbase");
        tableDescriptor.addFamily(new HColumnDescriptor("result"));
        admin.createTable(tableDescriptor);

        SimpleHBaseMapper mapper = new SimpleHBaseMapper();
        mapper.withColumnFamily("result");
        mapper.withColumnFields(new Fields("count"));
        mapper.withRowKeyField("word");

        Map<String, Object> map = Maps.newTreeMap();
        map.put("hbase.rootdir", "hdfs://172.17.11.156:9000/hbase");
        map.put("hbase.zookeeper.quorum", "172.17.11.156:2181");

        // hbase-bolt
        HBaseBolt hBaseBolt = new HBaseBolt("kafkastormhbase", mapper).withConfigKey("hbase.conf");
        builder.setBolt("hbase", hBaseBolt, 6).shuffleGrouping("count");


        Config conf = new Config();
        conf.setDebug(true);
        conf.put("hbase.conf", map);
        if(args != null && args.length > 0){
            conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        }else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("KafkaStormHBase", conf, builder.createTopology());
            //Thread.sleep(100000);
            //cluster.shutdown();
        }
    }
}
