/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neu.zzq.storm03.ack;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.task.ShellBolt;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.starter.bolt.PrinterBolt;
import org.apache.storm.starter.spout.RandomSentenceSpout;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * This topology demonstrates Storm's stream groupings and multilang
 * capabilities.
 */
public class WordCountTopology {

	public static class RandomSentenceSpout extends BaseRichSpout {
		SpoutOutputCollector _collector;
		Random _rand;

		private HashMap<String, String> waitAck = new HashMap<String, String>();

		@Override
		public void open(Map conf, TopologyContext context,
				SpoutOutputCollector collector) {
			_collector = collector;
			_rand = new Random();
		}

		@Override
		public void nextTuple() {
			Utils.sleep(100);
			String[] sentences = new String[] { "the cow jumped over the moon",
					"an apple a day keeps the doctor away",
					"four score and seven years ago",
					"snow white and the seven dwarfs",
					"i am at two with nature" };
			String sentence = sentences[_rand.nextInt(sentences.length)];

			String messageId = UUID.randomUUID().toString().replaceAll("-", "");
			waitAck.put(messageId, sentence);

			_collector.emit(new Values(sentence), messageId);
		}

		@Override
		public void ack(Object msgId) {
			System.out.println("消息处理成功:" + msgId);
			System.out.println("删除缓存中的数据...");
			waitAck.remove(msgId);
		}

		@Override
		public void fail(Object msgId) {
			System.out.println("消息处理失败:" + msgId);
			System.out.println("重新发送失败的信息...");
			// 重发如果不开启ackfail机制，那么spout的map对象中的该数据不会被删除的。
			_collector.emit(new Values(waitAck.get(msgId)), msgId);
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word"));
		}

	}

	public static class Split extends BaseBasicBolt {

		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			String words = tuple.getString(0);// the cow jumped over the moon
			for (String word : words.split(" ")) {
				collector.emit(new Values(word));
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word"));
		}

	}

	public static class WordCount extends BaseBasicBolt {
		Map<String, Integer> counts = new HashMap<String, Integer>();

		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			String word = tuple.getString(0);
			Integer count = counts.get(word);
			if (count == null)
				count = 0;
			count++;
			counts.put(word, count);
//			collector.emit(new Values(word, count));
			
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word", "count"));
		}
	}

	public static void main(String[] args) throws Exception {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("spout", new RandomSentenceSpout(), 1);
		
		builder.setBolt("split", new Split(), 1).shuffleGrouping("spout");
		builder.setBolt("count", new WordCount(), 1).fieldsGrouping("split",
				new Fields("word"));

		Config conf = new Config();
		conf.setDebug(true);

		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopologyWithProgressBar(args[0], conf,
					builder.createTopology());
		} else {
			// conf.setMaxTaskParallelism(3);

			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("word-count", conf, builder.createTopology());

			Thread.sleep(1000000);

			cluster.shutdown();
		}
	}
}
