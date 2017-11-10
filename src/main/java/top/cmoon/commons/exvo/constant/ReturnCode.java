package top.cmoon.commons.exvo.constant;

/**
 * 错误码常量： 请参考文档 <code>global-code.md</code>
 * Created by Administrator on 2017/8/18.
 */
public class ReturnCode {

    public static final int SUCCESS = 0;
    public static final int SYSTEM_BUSY = -1;


    //*********************************************
    //********* 操作失败代码，10?? *****************|
    //*********************************************
    public static final int OP_FAILURE = 1001;


    //*********************************************
    //********* 登陆错误代码 : 40?? ****************|
    //*********************************************
    public static final int NOT_LOGIN = 4001;
    public static final int LOGIN_EXPIRED = 4002;
    public static final int LOGIN_USER_NAME_NOT_EXIST = 4003;
    public static final int LOGIN_PASSWORD_MISTAKE = 4004;
    public static final int LOGIN_USER_OR_PWD_ERROR = 4005;


    //*********************************************
    //********* 请求参数错误代码： 41?? *************|
    //*********************************************
    public static final int PARAMETER_MISS = 4101;
    public static final int PARAMETER_TYPE_ERROR = 4102;
    public static final int PARAMETER_VALIDATE_FAILED = 4103;


    //**********************************************
    //********** 接口调用失败代码：42?? **************|
    //**********************************************

    public static final int WORK_FLOW_SUBMIT_FAILED = 4201;
    public static final int WORK_FLOW_STOPPED = 4202;
    public static final int WORK_FLOW_NO_NEXT_REVIEWER = 4203;
    public static final int APPLICATION_WORK_FLOW_HAS_STATRTED = 4204;

    public static final int CAREER_INFO_OBTAIN_FAILED = 4301;


}
