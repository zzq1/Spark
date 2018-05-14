package com.horizon.StormProcess;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zzq on 2018/1/24.
 */
public  class SlidingWindowSumBolt extends BaseWindowedBolt {
    private int sum = 0;
    private int line_count = 0;
    private String line;
    private OutputCollector collector;
    private static Double engine_temperature = 80.0;
    private static int over_time = 5;
    private Map<String,Integer> map;
//        List<String> temp_fan_no;

//        temp_fan_no = new List<String>();

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.map = new HashMap<>();
    }

    @Override
    public void execute(TupleWindow inputWindow) {
        List<Tuple> tuplesInWindow = inputWindow.get();
        List<Tuple> newTuples = inputWindow.getNew();
        List<Tuple> expiredTuples = inputWindow.getExpired();

        for (Tuple tuple : newTuples) {
            line =tuple.getStringByField("normal_data");
//                line_count += 1;
            String t = line.split(",")[13];
            String fan_no = line.split(",")[1];
            if (Double.valueOf(t) > engine_temperature) {
                if(map.containsKey(fan_no)){
                    int num=map.get(fan_no);
                    map.put(fan_no,num+1);
                }else {
                    map.put(fan_no,1);
                }
            }
        }
        for (Map.Entry<String,Integer> e:map.entrySet()){
            if (e.getValue() > over_time){
                collector.emit(new Values(e.getKey().toString(),e.getValue().toString()));
            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("fan_no","sum"));
    }
}
