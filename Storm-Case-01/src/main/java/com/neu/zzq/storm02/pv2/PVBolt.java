package com.neu.zzq.storm02.pv2;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * 获取PVSpout发送的数据，PVTopology开启多线程。 给出每个线程处理的PV数
 * 
 * 在多线程情况下，对PV数据只能局部汇总，不能整体汇总，可以把局部汇总的结果给一个单线程的BOLT进行整体汇总（PVSumBolt）
 * 
 * @version V1.0
 */
public class PVBolt extends BaseRichBolt {

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
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.context = context;
		this.collector = collector;
	}

    Map<String, Integer> counts = new HashMap<String, Integer>();

	/**
	 * Process a single tuple of input.
	 * 
	 * @param input
	 *            The input tuple to be processed.
	 */
	@Override
	public void execute(Tuple input) {

		try {
			String url = input.getStringByField("url");
            Integer count = counts.get(url);
            if (count == null)
                count = 0;
            count++;
            counts.put(url, count);
            
            for (Map.Entry<String, Integer> e : counts.entrySet()) {  
            	System.out.println(e.getKey()+"="+e.getValue()); 
           } 
            System.out.println("============");
            this.collector.emit(new Values(url, count));
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
		declarer.declare(new Fields("url", "count"));
	}
}
