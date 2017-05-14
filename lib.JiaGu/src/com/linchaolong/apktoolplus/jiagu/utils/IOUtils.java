package com.linchaolong.apktoolplus.jiagu.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by linchaolong on 2015/10/28.
 */
public class IOUtils {

    public static final String TAG = IOUtils.class.getSimpleName();

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
