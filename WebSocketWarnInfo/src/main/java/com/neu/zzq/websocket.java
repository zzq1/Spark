package com.neu.zzq;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket")

public class websocket {

    @OnOpen
    public void onOpen(Session session){
        GetData thread =null;
        thread=new GetData(session);
        thread.start();
    }
}
