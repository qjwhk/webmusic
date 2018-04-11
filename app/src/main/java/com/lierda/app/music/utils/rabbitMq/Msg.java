package com.lierda.app.music.utils.rabbitMq;

import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lierda.app.music.activity.MainActivity;
import com.lierda.app.music.application.AppCache;
import com.lierda.app.music.model.DeviceInfo;
import com.lierda.app.music.model.MusicControl;
import com.lierda.app.music.service.PlayService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

import static com.lierda.app.music.application.AppCache.getPlayService;

/**
 * Created by Administrator on 2017/11/29.
 */

public class Msg {
    private  String host="202.107.200.162";
    private  String user="admin";
    private  String psw="admin";
    private  int port=8010;
    private   int  f=1;
    private static final String EXCHANGE_NAME = "otherdev";
    private Connection connection=null;
    private Channel channel;

    public Msg(String host, String user, String psw, int port) {
        this.host = host;
        this.user = user;
        this.psw = psw;
        this.port = port;
    }

    public Msg() {
        super();
    }

    public void close() throws IOException {
        if(connection!=null){
            connection.close();
            connection=null;
        }
        if(channel!=null){
            channel.close();
            channel=null;
        }
    }

    public void setF(int f){
        this.f=f;
    }

    private Channel initChannel() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setPort(this.port);
        factory.setUsername(this.user);
        factory.setPassword(this.psw);
        connection=factory.newConnection();
        channel=connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic",true);
        this.channel=channel;
        return  channel;
    }


    public void sendMsg(String msg,String binding) throws IOException {
        Channel channel=initChannel();
        channel.basicPublish(EXCHANGE_NAME,binding,null,msg.getBytes());
        channel.close();
        connection.close();
    }

    public QueueingConsumer getQueueingConsumer(String binding) throws IOException {
        initChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName,EXCHANGE_NAME,binding);
        QueueingConsumer queueingConsumer=new QueueingConsumer(channel);
        channel.basicConsume(queueName,true,queueingConsumer);
        return  queueingConsumer;
    }

    public void recvMsg(String binding,Handler handler) throws IOException, InterruptedException {
            String msg="";
            initChannel();
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName,EXCHANGE_NAME,binding);
            QueueingConsumer queueingConsumer=new QueueingConsumer(channel);
            channel.basicConsume(queueName,true,queueingConsumer);
            while (f==1){
                QueueingConsumer.Delivery delivery=queueingConsumer.nextDelivery();
                msg=new String(delivery.getBody());
                if(msg!=null){
                    Message msg1 = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", msg);
                    msg1.setData(bundle);
                    handler.sendMessage(msg1);//
                }
            }
    }

    public static void main(String[] args) {
//        new Msg().recvMsg();
        MusicControl musicControl=new MusicControl();
        musicControl.setId("abcd");
        musicControl.setRequestType("send");
        musicControl.setAttributes(new DeviceInfo("qjw","open","cp"));
        String music=JSON.toJSONString(musicControl);
//        try {
//            new Msg().sendMsg(music);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
//            new Msg().recvMsg();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.print(music);
    }
}
