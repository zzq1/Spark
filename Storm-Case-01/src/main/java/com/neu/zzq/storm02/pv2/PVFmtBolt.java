package com.neu.zzq.storm02.pv2;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;


public class PVFmtBolt extends BaseRichBolt {

	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	private TopologyContext context;

	/**
	 * 实例初始化的时候调用一次
	 * 
	 * @param stormConf
	 *            The Storm configuration for this bolt.
	 * @param context
	 *            This object can be used to get information about this task's
	 *            place within the topology, including the task id and component
	 *            id of this task, input and output information, etc.
	 * @param collector
	 *            The collector is used to emit tuples from this bolt
	 */
	@Override
	public void prepare(Map stormConf, TopologyContext context,OutputCollector collector) {
		this.context = context;
		this.collector = collector;
	}

	/**
	 * Process a single tuple of input.
	 * 
	 * @param input
	 *            The input tuple to be processed.
	 */
	@Override
	public void execute(Tuple input) {

		try {

			String line = input.getStringByField("line");
			String data[] = line.split(" ");
			
			this.collector.emit(new Values(data[6]));
			this.collector.ack(input);

		} catch (Exception e) {
			e.printStackTrace();
			this.collector.fail(input);
		}
	}

	/**
	 * Declare the output schema for all the streams of this topology.
	 * 
	 * @param declarer
	 *            this is used to declare output stream ids, output fields, and
	 *            whether or not each output stream is a direct stream
	 */
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("url"));
	}
}
