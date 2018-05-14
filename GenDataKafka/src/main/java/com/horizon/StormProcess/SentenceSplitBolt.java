package com.horizon.StormProcess;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zzq on 2018/1/24.
 */
public class SentenceSplitBolt extends BaseBasicBolt {
    private static int null_value = -902;
    private static int min_wind_speed = 3;
    private static int max_wind_speed = 12;
    private static Double min_power = -0.5*1500;
    private static int max_power = 2*1500;
    Date now = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d");

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String sentence = input.getStringByField("str");
        String[] words = sentence.split(",");
        Double wind_speed = Double.valueOf(words[4].toString());
        Double power = Double.valueOf(words[22].toString());
        //过滤日期
        String data_today = dateFormat.format( now );
        String data = words[2].split(" ")[0].replaceAll("-","/");

        if (!wind_speed.equals(null) && wind_speed != null_value && wind_speed >= min_wind_speed && wind_speed <= max_wind_speed
                && !power.equals(null) && power != null_value && power >= min_power && power <= max_power){
            String key1 = words[2] + "_" + words[1] + "_NORMAL";
            collector.emit(new Values(sentence,key1));
        }else{
            String key2 = words[2] + "_" + words[1] + "_ABNORMAL";
            collector.emit(new Values(sentence,key2));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word","key"));
    }
}
