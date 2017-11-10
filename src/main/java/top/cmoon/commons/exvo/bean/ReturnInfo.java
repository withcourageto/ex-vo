package top.cmoon.commons.exvo.bean;


import top.cmoon.commons.exvo.constant.ReturnCode;

/**
 * Created by Administrator on 2017/8/18.
 */
public class ReturnInfo {


    public static final ReturnInfo SUCCESS = new ReturnInfo(ReturnCode.SUCCESS);


    private int code;
    private String msg;
    private Object data;

    public ReturnInfo(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ReturnInfo(Object data) {
        this.code = 0;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
