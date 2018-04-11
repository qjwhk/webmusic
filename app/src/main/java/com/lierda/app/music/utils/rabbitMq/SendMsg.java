package com.lierda.app.music.utils.rabbitMq;

import android.util.Log;

import com.aps.c;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Created by Administrator on 2017/11/29.
 */

public class SendMsg {
    private static final String host="202.107.200.162";
    private static final String user="admin";
    private static final String psw="admin";
    private static final int port=8010;
    private static ConnectionFactory connectionFactory=new ConnectionFactory();
    private  static Channel channel=null;

    static {
        connectionFactory.setHost(host);
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(psw);
        connectionFactory.setPort(port);
        try {
            channel=connectionFactory.newConnection().createChannel();
        } catch (IOException e) {
            Log.e("createChannel","failed");
        }
    }

    public SendMsg() {
        super();
    }

    public void sendMsg(String msg){

    }

}
