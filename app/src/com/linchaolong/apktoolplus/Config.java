package com.linchaolong.apktoolplus;

import com.linchaolong.apktoolplus.core.AppManager;
import com.linchaolong.apktoolplus.utils.ClassUtils;
import com.linchaolong.apktoolplus.utils.IOUtils;
import com.linchaolong.apktoolplus.utils.LogUtils;
import com.linchaolong.apktoolplus.utils.TaskManager;
import com.linchaolong.apktoolplus.core.debug.LogManager;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.jar.Manifest;

/**
 * Created by linchaolong on 2015/9/5.
 */
public class Config {

    public static final String TAG = Config.class.getSimpleName();

    /** 屏幕宽度 **/
    public static final int WINDOW_WIDTH = 960;
    /** 屏幕高度 **/
    public static final int WINDOW_HEIGHT = 500;

    /** 应用名称 **/
    public static final String APP_NAME = "ApkToolPlus";

    private static String version;

    /** properties **/
    private static Properties config;

    /** 配置文件 **/
    private static File configFile = new File(AppManager.getRuntimeDir(), "config.properties");;

    /** boolean值标识 **/
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    /** 相关配置的key **/
    public static final String kAppOutputDir = "appOutputDir";
    public static final String kLogLevel  = "logLevel";
    public static final String kIsLogOutputFile = "isLogOutputFile";
    public static final String kLastPageIndex = "lastPageIndex";
    public static final String kLastOpenDecompileDir = "lastOpenDecompileDir";
    public static final String kLastOpenRecompileDir = "lastOpenRecompileDir";
    public static final String kLastOpenApkSignDir = "lastOpenApkSignDir";
    public static final String kLastOpenApkInfoDir = "lastOpenApkInfoDir";
    public static final String kLastOpenJar2SmaliDir = "lastOpenJar2SmaliDir";
    public static final String kLastOpenClass2SmaliDir = "lastOpenClass2SmaliDir";
    public static final String kLastOpenDex2SmaliDir = "lastOpenDex2SmaliDir";
    public static final String kLastOpenSmali2DexDir = "lastOpenSmali2DexDir";
    public static final String kLastOpenClass2DexDir = "lastOpenClass2DexDir";
    public static final String kLastOpenDex2JarDir = "lastOpenDex2JarDir";
    public static final String kLastOpenJadDir = "lastOpenJadJarDir";
    public static final String kLastOpenJadJarDir = "lastOpenJadJarDir";

    public static final String kLastOpenIconDir = "lastOpenIconDir";
    public static final String kLastOpenJiaoBiaoDir = "lastOpenJiaoBiaoDir";
    public static final String kLastSaveIconDir = "lastSaveIconDir";
    public static final String kIconShowBorder = "iconShowBorder";

    public static final String kLastJiaGuAddApkDir = "lastJiaGuAddApkDir";
    public static final String kLastOpenApkParserDir = "lastOpenApkParserDir";
    public static final String kSublimePath = "sublimePath";
    public static final String kSublimeCmdParams = "sublimeCmdParams";

    public static final String kFrameworkFilePath = "frameworkFilePath";

    public static final String kKeystoreFilePath = "keystoreFilePath";
    public static final String kKeystoreAlias = "alias";
    public static final String kAliasPassword = "aliasPassword";
    public static final String kKeystorePassword = "keystorePassword";

    public static final String kLastEasySDKOpenDir = "lastEasySDKOpenDir";

    /** 默认loading图 **/
    public static final URL DEFAULT_LOADING_IMAGE = ClassUtils.getResourceAsURL("res/gif/loading.gif");

    /** 博客地址 **/
    public static final String JIANSHU_URL = "http://www.jianshu.com/u/149dc6683cc7";
    public static final String BLOG_URL = "http://blog.csdn.net/linchaolong";
    public static final String GITHUB_URL = "https://github.com/linchaolong/ApkToolPlus";

    static{
        try {
            config = new Properties();
            // 恢复默认配置
            if(configFile.exists()){
                FileInputStream in = new FileInputStream(configFile);
                config.load(in);
                in.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }
    }

    /**
     * 初始化配置
     */
    public static void init(){
        // 应用版本
        try {
            InputStream in = ClassUtils.getResourceAsStream("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(in);
            version = manifest.getMainAttributes().getValue("Manifest-Version");
            LogUtils.d("Manifest-Version="+version);
            IOUtils.close(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 恢复日志级别
        LogUtils.setLogLevel(Config.getInt(kLogLevel, LogUtils.DEBUG));
        // 恢复配置
        LogManager.getInstance().setIsLogFileOutput(TRUE.equals(config.getProperty(kIsLogOutputFile, TRUE)));
        // 默认输出目录
        if (getDir(kAppOutputDir) == null){
            Config.set(Config.kAppOutputDir,AppManager.getOutputDir().getPath());
        }
    }

    /**
     * 获取应用版本
     *
     * @return
     */
    public static String getVersion(){
        return version;
    }

    /**
     * 保存配置
     */
    public static void save() {
        save(null);
    }

    /**
     * 保存配置
     * @param callback  保存成功后的回调
     */
    public static void save(final Runnable callback){
        TaskManager.get().submit(() -> {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(configFile);
                config.store(out, APP_NAME + " Config");
                // 回调
                if (callback != null) {
                    callback.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(out);
            }
        });
    }

    /**
     * 根据key获取对应的目录，如果是一个文件则返回该文件所在目录
     *
     * @param key
     * @return
     */
    public static File getDir(String key){
        String dirPath = Config.get(key, null);
        File dir = null;
        if(dirPath != null){
            dir = new File(dirPath);
            if(dir.exists()){
                if(dir.isFile() ){
                    File parentFile = dir.getParentFile();
                    if(parentFile != null && parentFile.exists()){
                        dir = parentFile;
                    }
                }
            }else{
                dir = null;
            }
        }
        return dir;
    }

    public static boolean getBoolean(String key, boolean defaultVal){
        String result = get(key, null);
        if(result != null){
            return TRUE.equalsIgnoreCase(result);
        }
        return defaultVal;
    }

    public static int getInt(String key, int defaultVal) {
        String result = get(key, null);
        if(result != null){
            return Integer.parseInt(result);
        }
        return defaultVal;
    }

    public static String get(String key){
        return get(key,null);
    }
    public static String get(String key, String defaultVal){
        return config.getProperty(key,defaultVal);
    }

    public static void set(String key, String val){
        config.setProperty(key,val);
    }

    public static void set(String key, Boolean val){
        if(val){
            Config.set(key, Config.TRUE);
        }else{
            Config.set(key,Config.FALSE);
        }
    }

    public static void set(String key, Number value) {
        set(key,String.valueOf(value));
    }
}
