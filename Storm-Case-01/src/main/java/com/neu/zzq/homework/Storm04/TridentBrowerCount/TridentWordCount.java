package com.neu.zzq.homework.Storm04.TridentBrowerCount;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.TridentState;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.builtin.Count;
import org.apache.storm.trident.operation.builtin.FilterNull;
import org.apache.storm.trident.operation.builtin.MapGet;
import org.apache.storm.trident.testing.MemoryMapState;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

/**
 * Created by zzq on 2018/1/15.
 */
public class TridentWordCount {

    public static class Split extends BaseFunction {
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            String sentence = tuple.getString(0);
            for (String word : sentence.split(" ")) {
                collector.emit(new Values(word));
            }
        }
    }

    public static StormTopology buildTopology(LocalDRPC drpc) {
//        FixedBatchSpout spout = new FixedBatchSpout(new Fields("sentence"), 3, new Values("the cow jumped over the moon"),
//                new Values("the man went to the store and bought some candy"), new Values("four score and seven years ago"),
//                new Values("how many apples can you eat"), new Values("to be or not to be the person"));
//        spout.setCycle(true)
        TridentTopology topology = new TridentTopology();
        TridentState wordCounts = topology.newStream("spout1", new LogSpout(new Fields("line")))
                .parallelismHint(1).each(new Fields("line"),new Split(), new Fields("word"))
                .groupBy(new Fields("word"))
                .persistentAggregate(new MemoryMapState.Factory(),new Count(), new Fields("count"))
                .parallelismHint(4);

        topology.newDRPCStream("words", drpc)
                //.each(new Fields("args"), new Split(), new Fields("word"))
                //.groupBy(new Fields("word"))
                .stateQuery(wordCounts, new Fields("args"), new MapGet(), new Fields("count"))
                .each(new Fields("count"), new FilterNull());
        return topology.build();
    }

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        //conf.setMaxSpoutPending(20);
        if (args.length == 0) {
            LocalDRPC drpc = new LocalDRPC();
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("wordCounter", conf, buildTopology(drpc));
            for (int i = 0; i < 100; i++) {
                System.out.println("DRPC RESULT: " + drpc.execute("words", "\"Mozilla/5.0"));
                System.out.println("DRPC RESULT: " + drpc.execute("words", "\"Yahoo!"));
                System.out.println("DRPC RESULT: " + drpc.execute("words", "\"Mozilla/4.0"));
                System.out.println("DRPC RESULT: " + drpc.execute("words", "\"Sogou"));
                Thread.sleep(10000);
            }
        }
        else {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, buildTopology(null));
        }
    }
}
