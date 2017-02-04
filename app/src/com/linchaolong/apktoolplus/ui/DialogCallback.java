package com.linchaolong.apktoolplus.ui;

/**
 * Created by linchaolong on 2015/9/6.
 */
interface DialogCallback {

    public static final int CODE_CONFIRM = 1;
    public static final int CODE_CONCEL = 1;

    /**
     * 回调方法
     *
     * @param code  返回码，参考DialogCallback.CODE_xxx
     * @param msg   回调消息
     */
    void callback(int code, String msg);
}
