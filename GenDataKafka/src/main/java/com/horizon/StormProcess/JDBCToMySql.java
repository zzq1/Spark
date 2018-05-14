package com.horizon.StormProcess;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zzq on 2018/1/24.
 */
public  class JDBCToMySql extends BaseBasicBolt {
    Connection conn;
    PreparedStatement ps;
    private Statement state;
    String sql = "insert into ExceptionData(id,fan_no,warn_date,warn_desc,s_count,line_count) values (?,?,?,?,?,?)";
    private Long i = 0L;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://172.17.11.156:3306/wind_analysis?useUnicode=true&characterEncoding=utf8",
                    "hive",
                    "123456");
            ps = conn.prepareStatement(sql);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        String fan_no = tuple.getStringByField("fan_no");
        String s_count = tuple.getStringByField("sum");
        java.util.Date now = new java.util.Date();
        String desc = "温度>80";
        try {
            ps.setString(1,null);
            ps.setString(2,fan_no);
            ps.setString(3,dateFormat.format(now));
            ps.setString(4,desc);
            ps.setLong(5,Long.valueOf(s_count));
            ps.setLong(6,i);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void cleanup() {
        try {
            if (conn != null){
                conn.close();
            }
            if (ps != null){
                ps.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {}
}
