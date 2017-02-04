package com.linchaolong.apktoolplus.core;

/**
 * keystore文件配置对象
 *
 * Created by Administrator on 2015/8/30.
 */
public class KeystoreConfig {

    public static final String TAG = KeystoreConfig.class.getSimpleName();

    /** keystore文件路径 **/
    public String keystorePath;
    /** keystore文件密码 **/
    public String keystorePassword;
    /** 别名 **/
    public String alias;
    /** alias密码 **/
    public String aliasPassword;
}


