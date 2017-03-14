package com.linchaolong.apktoolplus.utils;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Created by linchaolong on 2015/10/22.
 */
public class Base64Utils {

    public static final String TAG = Base64Utils.class.getSimpleName();

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static String encode(String src){
        return new String(Base64.getEncoder().encode(src.getBytes()),UTF8);
    }

    public static String decode(String src){
        return new String(Base64.getDecoder().decode(src.getBytes()),UTF8);
    }

//    public static void main(String[] args) {
//        String password = "this is a password. p a s s w o r d";
//        String encode = encode(password);
//        System.out.println("encode="+encode);
//        String decode = decode(encode);
//        System.out.println("decode="+decode);
//    }
}
