package com.neu.zzq.homework.Storm05;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.hbase.trident.mapper.SimpleTridentHBaseMapMapper;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.trident.OpaqueTridentKafkaSpout;
import org.apache.storm.kafka.trident.TridentKafkaConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.builtin.Count;
import org.apache.storm.trident.state.StateType;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.trident.testing.Split;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.HashMap;

/**
 * Created by zzq on 2018/1/17.
 */


public class WordCount {

    public static StormTopology buildTopology(){
        ZkHosts zkHosts;
        String topic;
//        Fields fields = new Fields("word", "count");
//        FixedBatchSpout spout = new FixedBatchSpout(new Fields("sentence"), 3,
//                new Values("tanjie is a good man"), new Values(
//                "what is your name"), new Values("how old are you"),
//                new Values("my name is tanjie"), new Values("i am 18"));
//        spout.setCycle(true);


        //配置TridentKafkaConfig
        zkHosts = new ZkHosts("172.17.11.156:2181,172.17.11.155:2181,172.17.11.154:2181");
        topic = "stormforhbase";
        TridentKafkaConfig kafkaConfig = new TridentKafkaConfig(zkHosts, topic);
        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        OpaqueTridentKafkaSpout opaqueTridentKafkaSpout = new OpaqueTridentKafkaSpout(kafkaConfig);
        //配置HBaseMapState
        HBaseMapState.Options options = new HBaseMapState.Options();
        options.tableName = "WordCount";
        options.columnFamily = "cf";
        options.mapMapper = new SimpleTridentHBaseMapMapper("q2");


        TridentTopology topology = new TridentTopology();
        topology.newStream("sentencestream", opaqueTridentKafkaSpout)
                .each(new Fields("str"), new Split(), new Fields("word"))
                .groupBy(new Fields("word"))
                //构造Hbase状态为：StateType.OPAQUE
                .persistentAggregate(new HBaseMapState.Factory(StateType.OPAQUE, options), new Count(), new Fields("count"));

        return topology.build();
    }

    public static void main(String[] args) throws Exception {

        Config conf = new Config();
        conf.setMaxSpoutPending(5);
        conf.put("hbase.conf", new HashMap());

        if (args.length == 0) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("wordCounter", conf, buildTopology());
            Thread.sleep(60 * 1000);
//            cluster.killTopology("wordCounter");
//            cluster.shutdown();
//            System.exit(0);
        }
        else if(args.length == 2) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[1], conf, buildTopology());
        } else{
            System.out.println("Usage: TridentFileTopology <hdfs url> [topology name]");
        }
    }

}
