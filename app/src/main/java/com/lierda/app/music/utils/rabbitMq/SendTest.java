package com.lierda.app.music.utils.rabbitMq;

import com.alibaba.fastjson.JSON;
import com.lierda.app.music.model.DeviceInfo;
import com.lierda.app.music.model.MusicControl;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2017/12/1.
 */

public class SendTest {
    public static void main(String[] args) {
        MusicControl musicControl=new MusicControl();
        musicControl.setRequestType("heart");
        musicControl.setSourceId("jky");
        try {
            musicControl.setId(new MacAddr().getMacAddr());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        musicControl.setAttributes(new DeviceInfo("24","1","TC-PC"));
        String music= JSON.toJSONString(musicControl);
        try {
            new Msg().sendMsg(music,"AC83F35DAC50.www");
            System.out.print("send");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
