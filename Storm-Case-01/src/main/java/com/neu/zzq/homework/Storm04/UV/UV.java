package com.neu.zzq.homework.Storm04.UV;


import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.TridentState;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.*;
import org.apache.storm.trident.operation.builtin.Count;
import org.apache.storm.trident.operation.builtin.FilterNull;
import org.apache.storm.trident.operation.builtin.MapGet;
import org.apache.storm.trident.testing.MemoryMapState;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.trident.operation.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zzq on 2018/1/15.
 */

public class UV {

    public static class Split extends BaseFunction {
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            String line = tuple.getStringByField("line");
            String sid = line.split("\t")[0];
            String url = line.split("\t")[1];
            String sid_url = sid + "_" +url;
            System.out.println("#################"+sid+"####################"+url);
            collector.emit(new Values(sid_url));
//            String sentence = tuple.getString(0);
//            for (String word : sentence.split(" ")) {
//                collector.emit(new Values(word));
//            }
        }
    }
//    private static FlatMapFunction split = new FlatMapFunction() {
//        @Override
//        public Iterable<Values> execute(TridentTuple input) {
//            List<Values> valuesList = new ArrayList<>();
//            String line = input.getStringByField("line");
//            String sid = line.split("\t")[0];
//            String url = line.split("\t")[1];
//            String sid_url = sid + "_" +url;
//            valuesList.add(new Values(sid_url));
////            for (String word : input.getStringByField("line").split("/t")) {
////                valuesList.add(new Values(word[0] + "_" +word.));
////            }
//            return valuesList;
//        }
//    };

//    private static class Filter extends BaseFilter {
//        Map<String,Integer> map = new HashMap<>();
//        @Override
//        public boolean isKeep(TridentTuple tuple) {
//            String sid_url=tuple.getStringByField("sid_url");
//            //String[] words=sid_url.split("_");
//            //String url=words[1];
//            if (map.containsKey(sid_url)){
//                return false;
//            }else{
//                map.put(sid_url,1);
//                return true;
//            }
//            //return tuple.getString(0).equals("THE");
//        }
//    };
    private static  Filter theFilter = new BaseFilter() {
    Map<String,Integer> map = new HashMap<>();
        @Override
        public boolean isKeep(TridentTuple tuple) {
            String sid_url=tuple.getStringByField("sid_url");
            //String[] words=sid_url.split("_");
            //String url=words[1];
            if (map.containsKey(sid_url)){
                return false;
            }else{
                map.put(sid_url,1);
                return true;
            }
            //return tuple.getString(0).equals("THE");
        }
    };
    public static class Aggregate extends BaseAggregator<Map<String,Integer>> {

        @Override
        public Map<String,Integer> init(Object o, TridentCollector tridentCollector) {
            return new HashMap<String,Integer>() ;
        }

        @Override
        public void aggregate(Map<String,Integer> map, TridentTuple tridentTuple, TridentCollector tridentCollector) {
            String IP_browser=tridentTuple.getStringByField("sid_url");
            String[] words=IP_browser.split("_");
            String url=words[1];
            map.put(url,1);
        }

        @Override
        public void complete(Map<String,Integer> map, TridentCollector tridentCollector) {
            for (Map.Entry<String,Integer> e:map.entrySet()){
                System.out.println("#################"+e.getKey()+"####################"+e.getValue());
                tridentCollector.emit(new Values(e.getKey()));
            }
        }
    }

    public static StormTopology buildTopology(LocalDRPC drpc) {
//        FixedBatchSpout spout = new FixedBatchSpout(new Fields("sentence"), 3, new Values("the cow jumped over the moon"),
//                new Values("the man went to the store and bought some candy"), new Values("four score and seven years ago"),
//                new Values("how many apples can you eat"), new Values("to be or not to be the person"));
//        spout.setCycle(true)
        TridentTopology topology = new TridentTopology();
        BachSpout spout = new BachSpout(new Fields("line"));
        TridentState wordCounts = topology.newStream("spout1", spout)
                .parallelismHint(4)
                .each(new Fields("line"),new Split(), new Fields("sid_url"))
                .filter(theFilter)
                .groupBy(new Fields("sid_url"))
                .partitionAggregate(new Fields("sid_url"),new Aggregate(),new Fields("url"))
                .toStream()
                .parallelismHint(4)
                .groupBy(new Fields("url"))
                .persistentAggregate(new MemoryMapState.Factory(),new Count(), new Fields("count")).parallelismHint(4);
                //.groupBy(new Fields("word")).persistentAggregate(new MemoryMapState.Factory(),
                //new Count(), new Fields("count")).parallelismHint(4);

        topology.newDRPCStream("uv_count", drpc)
                //.each(new Fields("args"), new Split(), new Fields("url"))
                //.groupBy(new Fields("url"))
                .stateQuery(wordCounts, new Fields("args"), new MapGet(), new Fields("count"))
                .each(new Fields("count"), new FilterNull());
//        .aggregate(new Fields("count"), new Sum(), new Fields("sum"));
        return topology.build();
    }

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        //conf.setMaxSpoutPending(20);
        if (args.length == 0) {
            LocalDRPC drpc = new LocalDRPC();
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("wordCounter", conf, buildTopology(drpc));

            for (int i = 0; i < 10; i++) {
                System.out.println("DRPC RESULT: " + drpc.execute("uv_count", "\"http://www.itpub.net/forum.php?mod=attachment&aid=NTczNzU3fDFjNDdjZTgzfDEzMjI4NzgwMDV8MTMzOTc4MDB8MTEwMTcxMA%3D%3D&mobile=yes\""));
                System.out.println("DRPC RESULT: " + drpc.execute("uv_count", "\"http://www.itpub.net/forum-6-1.html?ts=28\""));
                System.out.println("DRPC RESULT: " + drpc.execute("uv_count", "\"http://www.itpub.net/thread-1558574-3-9.html\""));
                Thread.sleep(1000);
            }
        }
        else {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, buildTopology(null));
        }
    }
}

