package com.horizon.storm.pv;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

/**
 * 汇总PVBolt多个线程的结果
 * 
 */
public class PVSumBolt extends BaseRichBolt {
	/** 
     * 
     */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	private Map<Integer, Long> map = new HashMap<Integer, Long>();// <日期,PV数>

	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		try {
			Integer taskId = input.getIntegerByField("taskId");
			Long pv = input.getLongByField("pv");
			map.put(taskId, pv);// map个数为task实例数

			System.out.println("PVSumBolt=taskId:" + taskId + ",pv:" + pv
					+ ",time:" + System.currentTimeMillis());
			long sum = 0;// 获取总数，遍历map 的values，进行sum
			for (Entry<Integer, Long> e : map.entrySet()) {
				sum += e.getValue();
			}
			System.out.println("当前时间：" + System.currentTimeMillis() + ",pv汇总结果："
					+ "->" + sum);
			this.collector.ack(input);
		} catch (Exception e) {
			e.printStackTrace();
			this.collector.fail(input);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}
}
