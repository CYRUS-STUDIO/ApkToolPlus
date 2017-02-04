package com.linchaolong.apktoolplus.jiagu.utils;

/**
 * 数据加密解密工具
 *
 * @author linchaolong
 */
public class ApkToolPlus {

    public static void loadLibrary() {
        // 加载动态库，数据的加密解密算法实现在动态库中
        System.loadLibrary("apktoolplus_jiagu");
    }

    /**
     * 加密数据
     *
     * @param buff 数据
     * @return 加密后的数据
     */
    public native static byte[] encrypt(byte[] buff);

    /**
     * 解密数据
     *
     * @param buff 数据
     * @return 解密后的数据
     */
    public native static byte[] decrypt(byte[] buff);

}
