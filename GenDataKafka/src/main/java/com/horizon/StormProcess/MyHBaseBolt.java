package com.horizon.StormProcess;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zzq on 2018/1/24.
 */
public class MyHBaseBolt extends BaseBasicBolt {
    private Connection connection1;
    private Connection connection2;
    private Table table1;
    private Table table2;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        Configuration confh = HBaseConfiguration.create();
        confh.set("hbase.rootdir", "hdfs://172.17.11.156:9000/hbase");
        confh.set("hbase.zookeeper.quorum", "172.17.11.156:2181");
        try {
            connection1 = ConnectionFactory.createConnection(confh);
            table1 = connection1.getTable(TableName.valueOf("normal"));
            connection2 = ConnectionFactory.createConnection(confh);
            table2 = connection2.getTable(TableName.valueOf("abnormal"));
        } catch (IOException e) {
            //do something to handle exception
        }
    }
    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        //从tuple中获取单词
        String word = tuple.getStringByField("word");
        String key = tuple.getStringByField("key");
        String key1 = key.split("_")[0] + "_"+ key.split("_")[1];
        System.out.println(key1);

        if (key.split("_")[2].equals("NORMAL")){
            try {
                Put put1 = new Put(Bytes.toBytes(key1));
                //列族cf  列 Value
                put1.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("Value"), Bytes.toBytes(word));
                table1.put(put1);
                basicOutputCollector.emit(new Values(word));
            } catch (IOException e) {
                //do something to handle exception
            }
        }else {
            try {
                Put put2 = new Put(Bytes.toBytes(key1));
                put2.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("Value"), Bytes.toBytes(word));
                table2.put(put2);
            } catch (IOException e) {
                //do something to handle exception
            }
        }
    }
    @Override
    public void cleanup() {
        //关闭table
        try {
            if(table1 != null ) table1.close();
            if(table2 != null ) table2.close();
        } catch (Exception e){
            //do something to handle exception
        } finally {
            //在finally中关闭connection
            try {
                connection1.close();
                connection2.close();
            } catch (IOException e) {
                //do something to handle exception
            }
        }
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("normal_data"));
    }
}
