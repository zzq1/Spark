package com.neu.zzq.storm02.pv2;

import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.HashMap;
import java.util.Map;

/**
 * 实时统计PV拓扑--优化
 * 
 * @version V2.0
 */
public class PVTopology {
	public final static String SPOUT_ID = PVSpout.class.getSimpleName();
	public final static String PVFMTBOLT_ID = PVFmtBolt.class.getSimpleName();
	public final static String PVBOLT_ID = PVBolt.class.getSimpleName();
	public final static String PVTOPOLOGY_ID = PVTopology.class.getSimpleName();

	public static void main(String[] args) {
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout(SPOUT_ID, new PVSpout(), 1);
		builder.setBolt(PVFMTBOLT_ID, new PVFmtBolt(), 4).setNumTasks(8).shuffleGrouping(SPOUT_ID);// 4个线程，8个task实例，每1个线程2个task
		builder.setBolt(PVBOLT_ID, new PVBolt(), 1).fieldsGrouping(PVFMTBOLT_ID, new Fields("url"));// 单线程汇总

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
