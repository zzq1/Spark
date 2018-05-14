package JDBCOracle;

import java.sql.*;

public class OracleHelper {
    public static void main(String args[]){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            //1.加载驱动
            Class.forName("oracle.jdbc.OracleDriver");
            //2.获取连接
            conn = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:ORCL","scott","tiger");
            //3.获取执行sql语句对象
            ps = conn.prepareStatement("select * from emp");
            //4.执行sql语句
            rs = ps.executeQuery();
            //5.处理结果集
            while(rs.next()){
                int empno = rs.getInt("empno");
                String ename = rs.getString("ENAME");
                String job = rs.getString("job");
                int mgr = rs.getInt("mgr");
                Date hiredate = rs.getDate("HIREDATE");
                Double sal = rs.getDouble("sal");
                Double comm = rs.getDouble("COMM");
                int deptno = rs.getInt("DEPTNO");

                System.out.println(empno + " " + ename + " " + job + " " + mgr + " " + hiredate + " " + sal + " " + comm + " " +deptno);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //6.关闭连接
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }
}
