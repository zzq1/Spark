package com.neu.zzq.storm01;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.starter.bolt.PrinterBolt;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.testing.TestWordSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;

/**
 * This is a basic example of a Storm topology.
 */
public class ExclamationTopology {

  public static class ExclamationBolt extends BaseRichBolt {
    OutputCollector _collector;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
      _collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
      _collector.emit(tuple, new Values(tuple.getString(0) + "!!!"));
      _collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields("word"));
    }


  }
  
  public static class PrintBolt extends BaseRichBolt {
	    OutputCollector _collector;

	    @Override
	    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
	      _collector = collector;
	    }

	    @Override
	    public void execute(Tuple tuple) {
	    	System.out.println("Result:"+tuple.getString(0));
	    }

	    @Override
	    public void declareOutputFields(OutputFieldsDeclarer declarer) {
	    }


	  }

  public static void main(String[] args) throws Exception {
    TopologyBuilder builder = new TopologyBuilder();

    builder.setSpout("word", new TestWordSpout(), 1);
    builder.setBolt("exclaim1", new ExclamationBolt(), 1).shuffleGrouping("word");
    builder.setBolt("exclaim2", new PrinterBolt(), 1).shuffleGrouping("exclaim1");

    Config conf = new Config();
    conf.setDebug(true);

    if (args != null && args.length > 0) {
      conf.setNumWorkers(3);

      StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
    }
    else {

      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("test", conf, builder.createTopology());
      //Utils.sleep(1000000);
      cluster.killTopology("test");
      cluster.shutdown();
    }
  }
}
