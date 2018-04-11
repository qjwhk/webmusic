package com.lierda.app.music.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/11/29.
 */

public class DeviceInfo {

    private String USER; //用户ID
    private String SWI;//当前音乐功能开关状态 ：1/0,1为开，0为关，2为停
    private String TYP;//设备类型，10寸平板为TC-PC

    @JSONField(name = "USER")
    public String getUSER() {
        return USER;
    }

    public void setUSER(String USER) {
        this.USER = USER;
    }

    @JSONField(name = "SWI")
    public String getSWI() {
        return SWI;
    }

    public void setSWI(String SWI) {
        this.SWI = SWI;
    }

    @JSONField(name = "TYP")
    public String getTYP() {
        return TYP;
    }

    public void setTYP(String TYP) {
        this.TYP = TYP;
    }

    public DeviceInfo() {
        super();
    }

    public DeviceInfo(String USER, String SWI, String TYP) {
        this.USER = USER;
        this.SWI = SWI;
        this.TYP = TYP;
    }
}
