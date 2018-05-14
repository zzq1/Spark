/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.horizon.storm.window;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.starter.bolt.PrinterBolt;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.topology.base.BaseWindowedBolt.Count;
import org.apache.storm.topology.base.BaseWindowedBolt.Duration;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.apache.storm.windowing.TupleWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TumblingWindowDurationTopology {

    private static final Logger LOG = LoggerFactory.getLogger(TumblingWindowDurationTopology.class);

    public static class NumberIntegerSpout extends BaseRichSpout {

    	private static final long serialVersionUID = 1L;
		private SpoutOutputCollector collector;
		private int num = 1;
        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("value"));
        }

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void nextTuple() {
            Utils.sleep(1000);
      
            if(num <= 10){
            	collector.emit(new Values(num));
            	num ++;
            }
        }

    }
    
    public static class TumblingWindowSumBolt extends BaseWindowedBolt {

        private int sum = 0;
        private OutputCollector collector;

        @Override
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void execute(TupleWindow inputWindow) {
            List<Tuple> tuplesInWindow = inputWindow.get();

            LOG.debug("Events in current window: " + tuplesInWindow.size());
                
            sum = 0;
            for (Tuple tuple : tuplesInWindow) {
              sum += (int) tuple.getValue(0);
            }
            collector.emit(new Values(sum));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("sum"));
        }
    }
    


    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("integer", new NumberIntegerSpout());
        builder.setBolt("tumblingsum", new TumblingWindowSumBolt().withTumblingWindow(new Duration(3,TimeUnit.SECONDS)))
                .shuffleGrouping("integer");
        builder.setBolt("printer", new PrinterBolt()).shuffleGrouping("tumblingsum");
        
        Config conf = new Config();
        conf.setDebug(true);
        if (args != null && args.length > 0) {
            conf.setNumWorkers(1);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(40000);
            cluster.killTopology("test");
            cluster.shutdown();
        }
    }
}