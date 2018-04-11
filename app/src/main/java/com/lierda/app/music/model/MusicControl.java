package com.lierda.app.music.model;

/**
 * Created by Administrator on 2017/11/29.
 */

public class MusicControl {
    private String sourceId;//服务器地址
    private String requestType;
    private String id; //设备MAC
    private DeviceInfo attributes;

    public MusicControl(String sourceId, String requestType, String id, DeviceInfo attributes) {
        this.sourceId = sourceId;
        this.requestType = requestType;
        this.id = id;
        this.attributes = attributes;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeviceInfo getAttributes() {
        return attributes;
    }

    public void setAttributes(DeviceInfo attributes) {
        this.attributes = attributes;
    }

    public MusicControl() {
        super();
    }
}
