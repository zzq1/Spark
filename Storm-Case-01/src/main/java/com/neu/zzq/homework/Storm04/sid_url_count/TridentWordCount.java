package com.neu.zzq.homework.Storm04.sid_url_count;

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
            String sentence = tuple.getStringByField("line");
            collector.emit(new Values(sentence));
        }
    }

    public static StormTopology buildTopology(LocalDRPC drpc) {
        TridentTopology topology = new TridentTopology();
        TridentState wordCounts = topology.newStream("spout1", new LogSpout(new Fields("line")))
                .parallelismHint(4)
                .each(new Fields("line"), new Split(), new Fields("word")).groupBy(new Fields("word"))
                .persistentAggregate(new MemoryMapState.Factory(), new Count(), new Fields("count"))
                .parallelismHint(4);

        topology.newDRPCStream("words", drpc)
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
                System.out.println("DRPC RESULT: " + drpc.execute("words", "114.112.141.6_\"http://www.itpub.net/thread-1558574-3-9.html\""));
                System.out.println("DRPC RESULT: " + drpc.execute("words", "110.75.173.35_\"http://www.itpub.net/thread-1558574-3-9.html\""));
                System.out.println("DRPC RESULT: " + drpc.execute("words", "123.126.50.73_\"http://www.itpub.net/thread-1558574-3-9.html\""));
                System.out.println("DRPC RESULT: " + drpc.execute("words", "116.205.130.2_\"http://www.itpub.net/forum-6-1.html?ts=28\""));
                System.out.println("DRPC RESULT: " + drpc.execute("words", "110.6.179.88_\"http://www.itpub.net/forum.php?mod=attachment&aid=NTczNzU3fDFjNDdjZTgzfDEzMjI4NzgwMDV8MTMzOTc4MDB8MTEwMTcxMA%3D%3D&mobile=yes\""));
                System.out.println("DRPC RESULT: " + drpc.execute("words", "123.126.50.73_\"http://www.itpub.net/forum.php?mod=attachment&aid=NTczNzU3fDFjNDdjZTgzfDEzMjI4NzgwMDV8MTMzOTc4MDB8MTEwMTcxMA%3D%3D&mobile=yes\""));
                Thread.sleep(10000);
            }
        }
        else {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, buildTopology(null));
        }
    }
}
