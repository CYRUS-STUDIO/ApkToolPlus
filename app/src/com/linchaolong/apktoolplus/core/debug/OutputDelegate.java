package com.linchaolong.apktoolplus.core.debug;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * 日志输出代理类
 */
class OutputDelegate extends PrintStream {

    private OutputListener outputListener;

    public OutputDelegate(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) {
        super.write(b);
        if(outputListener != null){
            outputListener.write(b);
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        if(outputListener != null){
            outputListener.write(buf,off,len);
        }
    }

    public OutputListener getOutputListener() {
        return outputListener;
    }

    public void setOutputListener(OutputListener outputListener) {
        this.outputListener = outputListener;
    }

}