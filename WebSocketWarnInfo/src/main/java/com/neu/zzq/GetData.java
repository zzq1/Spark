package com.neu.zzq;

import javax.websocket.Session;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class GetData extends Thread{

    private Session session;
    private List<Vo> currentMessage;
    private DBUtil dbUtil;
    private int currentIndex;

    public GetData(Session session) {
        this.session = session;
        currentMessage = new ArrayList<Vo>();
        dbUtil = new DBUtil();
        currentIndex = 0;//此时是0条消息
    }
    @Override
    public void run() {
        while (true) {
            List<Vo> list = null;
            try {
                try {
                    list = dbUtil.getFromDB();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (list != null && currentIndex < list.size()) {
                for (int i = currentIndex; i < list.size(); i++) {
                    try {
                        session.getBasicRemote().sendText(list.get(i).getFanNo()
                                + "," + list.get(i).getTime()
                                + "," + list.get(i).getDescription()
                                + "," + list.get(i).getS_count());
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                }
                currentIndex = list.size();
            }
            try {
                //一秒刷新一次
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

