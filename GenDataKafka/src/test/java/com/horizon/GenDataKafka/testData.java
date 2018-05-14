package com.horizon.GenDataKafka;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by zzq on 2018/1/22.
 */
public class testData {
    public static  void main(String[] args){
       /* Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d");//可以方便地修改日期格式


        String hehe = dateFormat.format( now );
        System.out.println(hehe);*/
        Connection conn = null;
        PreparedStatement ps = null;
        Statement state;
        String sql = "insert into ExceptionData(id,fan_no,warn_date,warn_desc,s_count,line_count) values (?,?,?,?,?,?)";
        Long i = 0L;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://172.17.11.156:3306/wind_analysis?useUnicode=true&characterEncoding=utf8", "hive", "123456");
            ps = conn.prepareStatement(sql);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Date now = new Date();
        String desc = "温度>80";
        try {
            ps.setLong(1,12);
            ps.setString(2,"fan_no");
            ps.setString(3,dateFormat.format(now));
            ps.setString(4,"温度>80");
            ps.setLong(5,12);
            ps.setLong(6,12);
            System.out.println("1111");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            if (conn != null) {
                conn.close();
                ps.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
