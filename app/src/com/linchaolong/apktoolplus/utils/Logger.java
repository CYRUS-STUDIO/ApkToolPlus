package com.linchaolong.apktoolplus.utils;

/**
 * Created by linchaolong on 2020/10/29.
 */
public class Logger {

    public static final String TAG = "ApkToolPlus";

    public static void print(String msg){
        System.out.println(msg);
    }

    public static void print(String format, String... msg){
        System.out.printf((format) + "%n", msg);
    }

    public static void error(String msg){
        System.err.println(msg);
    }

    public static void error(String format, String... msg){
        System.err.printf((format) + "%n", msg);
    }

}
