package com.linchaolong.apktoolplus.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by linchaolong on 2015/10/28.
 */
public class IO {

    public static final String TAG = IO.class.getSimpleName();

    public static final void close(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
