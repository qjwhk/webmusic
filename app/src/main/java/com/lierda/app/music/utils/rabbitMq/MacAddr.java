package com.lierda.app.music.utils.rabbitMq;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2017/11/29.
 */

public class MacAddr {

    public String getMacAddr() throws UnknownHostException, SocketException {
        String addr="";
        NetworkInterface networkInterface=NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        byte[] macAddr=networkInterface.getHardwareAddress();
        for (byte b :
                macAddr) {
            addr=addr+toHexString(b).toUpperCase().replace(":","");
        }
        return  addr;
    }

    public String toHexString(int i){
        String str=Integer.toHexString((int)(i&0xff));
        if(str.length()==1){
            str="0"+str;
        }
        return str;
    }

    public static void main(String[] args) {
        try {
            String str=new MacAddr().getMacAddr();
            System.out.print(str);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
