package com.neu.zzq.storm03.ack;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.testing.TestWordSpout;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * This is a basic example of a Storm topology.
 */
public class ExclamationTopology2 {

    public static class ExclamationBolt extends BaseBasicBolt {


        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            collector.emit(new Values(tuple.getString(0) + "!!!"));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word"));
        }
    }

    public static class PrintBolt extends BaseBasicBolt {


        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            System.out.println("Result:" + tuple.getString(0));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }


    }

    public static class TestWordSpout2 extends BaseRichSpout {
        public static Logger LOG = LoggerFactory.getLogger(TestWordSpout.class);
        boolean _isDistributed;
        SpoutOutputCollector _collector;

        public TestWordSpout2() {
            this(true);
        }

        public TestWordSpout2(boolean isDistributed) {
            _isDistributed = isDistributed;
        }

        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            _collector = collector;
        }

        public void close() {

        }

        public void nextTuple() {
            Utils.sleep(100);
            final String[] words = new String[] {"nathan", "mike", "jackson", "golda", "bertels"};
            final Random rand = new Random();
            final String word = words[rand.nextInt(words.length)];
            String messageId = UUID.randomUUID().toString().replaceAll("-", "");
            _collector.emit(new Values(word),messageId);
        }

        public void ack(Object msgId) {
            System.out.println("ack==========="+msgId);
        }

        public void fail(Object msgId) {
            System.out.println("fail==========="+msgId);
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word"));
        }

        @Override
        public Map<String, Object> getComponentConfiguration() {
            if(!_isDistributed) {
                Map<String, Object> ret = new HashMap<String, Object>();
                ret.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 1);
                return ret;
            } else {
                return null;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("word", new TestWordSpout2(), 1);
        builder.setBolt("exclaim1", new ExclamationBolt(), 1).shuffleGrouping("word");
        builder.setBolt("exclaim2", new PrintBolt(), 1).shuffleGrouping("exclaim1");

        Config conf = new Config();
        conf.setDebug(true);

        if (args != null && args.length > 0) {
            conf.setNumWorkers(3);

            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        } else {

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(1000000);
            cluster.killTopology("test");
            cluster.shutdown();
        }
    }
}
