package com.neu.zzq.storm02.uv2;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class UVTopology {

	public static final String SPOUT_ID = SourceSpout.class.getSimpleName();
	public static final String UVFORMAT_ID = UVFormatBolt.class.getSimpleName();
	public static final String UVFMT_ID = UVFmtBolt.class.getSimpleName();
	public static final String UVDEEPVISITBOLT_ID = UVDeepVisitBolt.class.getSimpleName();
	public static final String UVSUMBOLT_ID = UVSumBolt.class.getSimpleName();

	public static void main(String[] args) {

		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(SPOUT_ID, new SourceSpout(),1);
		//builder.setBolt(UVFORMAT_ID, new UVFormatBolt(),1).shuffleGrouping(SPOUT_ID);
		builder.setBolt(UVFMT_ID, new UVFmtBolt(),4).shuffleGrouping(SPOUT_ID);

		builder.setBolt(UVDEEPVISITBOLT_ID, new UVDeepVisitBolt(),4).fieldsGrouping(UVFMT_ID, new Fields("url"));

		builder.setBolt(UVSUMBOLT_ID, new UVSumBolt(),1).shuffleGrouping(UVDEEPVISITBOLT_ID);

		Map<String, Object> conf = new HashMap<String, Object>();
//		conf.put(Config.TOPOLOGY_DEBUG, true);

		if (args != null && args.length > 0) {
			try {
				StormSubmitter.submitTopology(UVTopology.class.getSimpleName(),
						conf, builder.createTopology());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(UVTopology.class.getSimpleName(), conf,
					builder.createTopology());
		}
	}
}