package com.neu.zzq.homework.Storm06;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.hbase.bolt.mapper.HBaseProjectionCriteria;
import org.apache.storm.hbase.trident.mapper.SimpleTridentHBaseMapper;
import org.apache.storm.hbase.trident.mapper.TridentHBaseMapper;
import org.apache.storm.hbase.trident.state.HBaseState;
import org.apache.storm.hbase.trident.state.HBaseStateFactory;
import org.apache.storm.hbase.trident.state.HBaseUpdater;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.BaseAggregator;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.state.StateFactory;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.trident.windowing.config.SlidingDurationWindow;
import org.apache.storm.trident.windowing.config.WindowConfig;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zzq on 2018/1/17.
 */


public class WordCount {
    public static class Split extends BaseFunction {

        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            for(String word: tuple.getString(0).split(" ")) {
                if(word.length() > 0) {
                    collector.emit(new Values(word));
                }
            }
        }
    }

    public static class Aggregate extends BaseAggregator<Map<String,Integer>> {

        @Override
        public Map<String, Integer> init(Object o, TridentCollector tridentCollector) {
            return new HashMap<String,Integer>();
        }

        @Override
        public void aggregate(Map<String, Integer> map, TridentTuple tridentTuple, TridentCollector tridentCollector) {
            String word=tridentTuple.getStringByField("word");
            if(map.containsKey(word)){
                int num=map.get(word);
                map.put(word,num+1);
            }else {
                map.put(word,1);
            }
        }

        @Override
        public void complete(Map<String, Integer> map, TridentCollector tridentCollector) {

            List<Map.Entry<String,Integer>> list=new ArrayList<Map.Entry<String, Integer>>(map.entrySet());

            Collections.sort(list, new Comparator<Map.Entry<String,Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue()-o1.getValue();
                }
            });

            for(int i=0;i<5 && i<list.size();i++){
                tridentCollector.emit(new Values(String.valueOf(i+1),list.get(i).getKey().toString(),list.get(i).getValue().toString()));
            }
        }
    }

    public static StormTopology buildTopology(){
        Fields fields = new Fields("word", "count");
        FixedBatchSpout spout = new FixedBatchSpout(new Fields("sentence"), 3,
                new Values("tanjie is a good man"), new Values(
                "what is your name"), new Values("how old are you"),
                new Values("my name is tanjie"), new Values("i am 18"));
        spout.setCycle(true);


        //HBase相关配置
        TridentHBaseMapper tridentHBaseMapper=new SimpleTridentHBaseMapper()
                .withColumnFamily("result")
                .withColumnFields(new Fields("word","count"))
                .withRowKeyField("rank");

        //定义HBase数据到Tuple的投影，加入cf列族的word和count列
        HBaseProjectionCriteria projectionCriteria=new HBaseProjectionCriteria()
                .addColumn(new HBaseProjectionCriteria.ColumnMetaData("result","word"))
                .addColumn(new HBaseProjectionCriteria.ColumnMetaData("result","count"));

        //定义HbaseState的属性类Options
        HBaseState.Options options=new HBaseState.Options()
                .withConfigKey("hbase.conf")
                .withMapper(tridentHBaseMapper)
                .withDurability(Durability.SYNC_WAL)
                .withProjectionCriteria(projectionCriteria)
                .withTableName("HBseWC");

        //使用工厂类生产HbaseState对象
        StateFactory factory=new HBaseStateFactory(options);
        //构造滑动窗口对象
        WindowConfig slidingDurationWindow = SlidingDurationWindow.of(new BaseWindowedBolt.Duration(30, TimeUnit.SECONDS), new BaseWindowedBolt.Duration(10,TimeUnit.SECONDS));

        TridentTopology topology = new TridentTopology();
        topology.newStream("sentencestream", spout)
                .each(new Fields("sentence"), new Split(), new Fields("word"))
                .window(slidingDurationWindow, new Fields("word"), new Aggregate(), new Fields("rank", "word", "count"))
                .partitionPersist(factory, new Fields("rank", "word", "count"), new HBaseUpdater(), new Fields());

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
