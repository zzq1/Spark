package com.horizon.storm.ack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

public class AckFailSpout extends BaseRichSpout {
	private static final long serialVersionUID = 5028304756439810609L;

    // key:messageId,Data
    private HashMap<String, String> waitAck = new HashMap<String, String>();

    private SpoutOutputCollector collector;

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentence"));
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    public void nextTuple() {
        String sentence = "i am liu yang";
        String messageId = UUID.randomUUID().toString().replaceAll("-", "");
        waitAck.put(messageId, sentence);
        //指定messageId，开启ackfail机制
        collector.emit(new Values(sentence), messageId);
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
        //重发如果不开启ackfail机制，那么spout的map对象中的该数据不会被删除的。
        collector.emit(new Values(waitAck.get(msgId)),msgId);
    }
}
