package com.neu.zzq.storm02.pv;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.topology.TopologyBuilder;

/**
 * 实时统计PV拓扑
 * 
 * @version V1.0
 */
public class PVTopology {
	public final static String SPOUT_ID = PVSpout.class.getSimpleName();
	public final static String PVBOLT_ID = PVBolt.class.getSimpleName();
	public final static String PVTOPOLOGY_ID = PVTopology.class.getSimpleName();
	public final static String PVSUMBOLT_ID = PVSumBolt.class.getSimpleName();

	public static void main(String[] args) {
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout(SPOUT_ID, new PVSpout(), 1);
		builder.setBolt(PVBOLT_ID, new PVBolt(), 4).setNumTasks(8)
				.shuffleGrouping(SPOUT_ID);// 4个线程，8个task实例，每1个线程2个task
		builder.setBolt(PVSUMBOLT_ID, new PVSumBolt(), 1).shuffleGrouping(PVBOLT_ID);// 单线程汇总

		Map<String, Object> conf = new HashMap<String, Object>();
//		conf.put(Config.TOPOLOGY_DEBUG, true);

		if (args != null && args.length > 0) {
			try {
				StormSubmitter.submitTopology(PVTOPOLOGY_ID, conf,
						builder.createTopology());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(PVTOPOLOGY_ID, conf,
					builder.createTopology());
		}

	}
}
