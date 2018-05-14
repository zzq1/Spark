package com.neu.zzq.storm02.uv2;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import com.neu.zzq.homework.Storm02.Kpi;
/**
 * Created by zzq on 2018/1/12.
 */

public class UVFormatBolt extends BaseBasicBolt {
    private Kpi k;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public void execute(Tuple input, BasicOutputCollector collector) {

        //2014-01-07 08:40:50     ABYH6Y4V4SCV00     http://www.jd.com/1.html
//        String line = input.getStringByField("line");
//        String date = line.split("\t")[0];
//        String sid = line.split("\t")[1];
//        String url = line.split("\t")[2];
        String line = input.getStringByField("line");
        k = Kpi.parse(line);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (k.getIs_validate() && !k.getHttp_referrer().toString().contains("-")){
            collector.emit(new Values(sdf.format(new Date()) + "\t" + k.getRemote_addr() + "\t" + k.getHttp_referrer()));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
//          declarer.declare(new Fields("date","sid"));
        declarer.declare(new Fields("ll"));
    }
}