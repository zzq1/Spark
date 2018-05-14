package com.neu.zzq;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {
    public List<Vo> getFromDB() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<Vo> list=new ArrayList<Vo>();

        String dirver="com.mysql.jdbc.Driver";
        String user="hive";
        String psd="123456";
        String database="wind_analysis";
        String tablename="ExceptionData";
        String url="jdbc:mysql://172.17.11.156:3306/"+database+"?user="+user+"&password="+psd;
        Class.forName(dirver).newInstance();
        Connection conn= DriverManager.getConnection(url);
        Statement stat=conn.createStatement();
        String sql="select * from "+tablename;
        ResultSet rs=stat.executeQuery(sql);
        while (rs.next()){
            Vo vo=new Vo(rs.getString(2),rs.getString(3),rs.getString(4),rs.getInt(5));
            list.add(vo);
        }
        rs.close();
        stat.close();
        conn.close();
        return list;
    }
}
