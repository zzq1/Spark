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
package com.neu.zzq.storm06.window.trident;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.topology.base.BaseWindowedBolt.Duration;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.BaseAggregator;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.trident.windowing.config.SlidingCountWindow;
import org.apache.storm.trident.windowing.config.SlidingDurationWindow;
import org.apache.storm.trident.windowing.config.TumblingCountWindow;
import org.apache.storm.trident.windowing.config.TumblingDurationWindow;
import org.apache.storm.trident.windowing.config.WindowConfig;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

public class TridentWindowCount {

	
	public static class NumberIntegerSpout implements IBatchSpout {

    	private static final long serialVersionUID = 1L;
		private int num = 1;

		@Override
		public void open(Map conf, TopologyContext context) {
			
		}

		@Override
		public void emitBatch(long batchId, TridentCollector collector) {
			Utils.sleep(1000);
//        	System.out.println("send:"+num);
        	collector.emit(new Values(num));
        	num ++;
			
		}
		
		@Override
		public Fields getOutputFields() {
			return new Fields("value");
		}

		@Override
		public void ack(long batchId) {
		}

		@Override
		public void close() {
			
		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			return null;
		}

    } 
	
	public static class SumAggregator extends BaseAggregator<Integer> {
		int sum = 0;
		@Override
		public Integer init(Object batchId, TridentCollector collector) {
			sum = 0;
			return 0;
		}

		@Override
		public void aggregate(Integer val, TridentTuple tuple,
				TridentCollector collector) {
			sum += tuple.getInteger(0);
			System.out.println("aggregate tuple="+tuple.getInteger(0)+" sum="+sum);
		}

		@Override
		public void complete(Integer val, TridentCollector collector) {
			collector.emit(new Values(sum));
//			System.out.println("complete emit="+sum);
		}

	}
	
	public static class PrinterFunc extends BaseFunction {
		@Override
		public void execute(TridentTuple tuple, TridentCollector collector) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println(sdf.format(new Date())+"\tResult " + tuple.get(0));
			System.out.println();
		}
	}
	
	public static StormTopology buildTopology() {
		
		WindowConfig slidingCountWindow = SlidingCountWindow.of(5, 2);
		WindowConfig slidingDurationWindow = SlidingDurationWindow.of(new Duration(5,TimeUnit.SECONDS), new Duration(2,TimeUnit.SECONDS));
		
		WindowConfig tumblingCountWindow = TumblingCountWindow.of(3);
		WindowConfig tumblingDurationWindow = TumblingDurationWindow.of(new Duration(3,TimeUnit.SECONDS));
		
		TridentTopology topology = new TridentTopology();
		topology.newStream("spout1", new NumberIntegerSpout())
				.window(slidingCountWindow, new Fields("value"), new SumAggregator(), new Fields("sum"))
				.each(new Fields("sum"), new PrinterFunc(), new Fields(""));

		return topology.build();
	}

	public static void main(String[] args) throws Exception {
		Config conf = new Config();
		conf.setMaxSpoutPending(20);
		if (args.length == 0) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("wordCounter", conf, buildTopology());

		} else {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopologyWithProgressBar(args[0], conf,
					buildTopology());
		}
	}
}
