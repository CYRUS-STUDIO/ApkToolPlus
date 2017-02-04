package com.linchaolong.apktoolplus.core;

import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.utils.Base64Tool;
import com.linchaolong.apktoolplus.utils.StringUtils;

import java.io.File;

/**
 * 设置
 *
 * Created by linchaolong on 2015/11/28.
 */
public class SettingHelper {

    public static final String TAG = SettingHelper.class.getSimpleName();

    public static KeystoreConfig getKeystoreConfig() {

        // keystore文件设置检查
        String keystoreFilePath = Config.get(Config.kKeystoreFilePath, null);
        if(keystoreFilePath == null){
//            Global.toast("请先设置keytore文件路径");
            return null;
        }

        File keystoreFile = new File(keystoreFilePath);
        if(!keystoreFile.exists()){
//            Global.toast("keytore文件不存在");
            return null;
        }

        // alias
        String alias = Config.get(Config.kKeystoreAlias, null);
        if(StringUtils.isEmpty(alias)){
//            Global.toast("keytore文件别名未设置");
            return null;
        }
        // alias password
        String aliasPassword = Config.get(Config.kAliasPassword,null);
        if(StringUtils.isEmpty(aliasPassword)){
//            Global.toast("Alias密码未设置");
            return null;
        }
        // keystore password
        String keystorePassword = Config.get(Config.kKeystorePassword,null);
        if(StringUtils.isEmpty(keystorePassword)){
//            Global.toast("keytore文件密码未设置");
            return null;
        }

        KeystoreConfig config = new KeystoreConfig();
        config.keystorePath = keystoreFilePath;
        config.alias = alias;
        config.aliasPassword = Base64Tool.decode(aliasPassword);
        config.keystorePassword = Base64Tool.decode(keystorePassword);

        return config;
    }
}
