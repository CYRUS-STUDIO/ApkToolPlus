package com.linchaolong.apktoolplus.core.debug;

/**
 * 控制台输出监听
 *
 * Created by linchaolong on 2015/9/7.
 */
public interface OutputListener {

    void write(int b);

    void write(byte[] buf, int off, int len);
}
