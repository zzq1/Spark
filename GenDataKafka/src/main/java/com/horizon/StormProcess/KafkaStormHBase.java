package com.horizon.StormProcess;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.storm.Config;
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
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.apache.storm.windowing.TupleWindow;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zzq on 2018/1/14.
 */
public class KafkaStormHBase {
    public static void main(String[] args) throws AlreadyAliveException,
            InvalidTopologyException, InterruptedException ,AuthorizationException,IOException{
        TopologyBuilder builder = new TopologyBuilder();
        SpoutConfig spoutConf = new SpoutConfig(new ZkHosts("172.17.11.156:2181,172.17.11.155:2181,172.17.11.154:2181"),
                "stormshixun", "/test", UUID.randomUUID().toString());
        //spoutConf.forceFromStart = true;
        spoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConf);

        builder.setSpout("spout", kafkaSpout, 5);
        builder.setBolt("split", new SentenceSplitBolt(),8).shuffleGrouping("spout");
        builder.setBolt("hbase",new MyHBaseBolt(), 6).shuffleGrouping("split");
        builder.setBolt("slidingsum", new SlidingWindowSumBolt()
                        .withWindow(new BaseWindowedBolt.Duration(30, TimeUnit.SECONDS),
                        new BaseWindowedBolt.Duration(5,TimeUnit.SECONDS)), 1)
                        .shuffleGrouping("hbase");
        builder.setBolt("jdbc",new JDBCToMySql(),1).shuffleGrouping("slidingsum");

        Config conf = new Config();
        conf.setDebug(true);
        conf.setMessageTimeoutSecs(60);
        if(args != null && args.length > 0){
            conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        }else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("KafkaStormHBase", conf, builder.createTopology());
            Thread.sleep(60 * 1000);
            //Thread.sleep(100000);
            //cluster.shutdown();
        }
    }
}
