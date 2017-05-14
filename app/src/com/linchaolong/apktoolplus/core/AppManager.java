package com.linchaolong.apktoolplus.core;

import com.linchaolong.apktoolplus.utils.*;
import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import com.linchaolong.apktoolplus.Config;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;

/**
 *  应用管理类
 *
 * Created by linchaolong on 2015/11/14.
 */
public class AppManager {

    public static final String TAG = AppManager.class.getSimpleName();


    /** 用于当前判断是否在 Release 环境 **/
    public static boolean isReleased = false;

    public static final String APKTOOL_FILE = "apktool.jar";
    public static final String APKTOOL_PATH = "apktool/" + APKTOOL_FILE;
    public static final String FRAMEWORK_FILE = "framework-res.apk";
    public static final String FRAMEWORK_PATH = "apktool/" + FRAMEWORK_FILE;

    private static File apkTool = new File(getTempDir(), APKTOOL_FILE);

    public static File getApkTool(){
        initApkTool();
        return apkTool;
    }

    public static void initApkTool(){
        if(FileHelper.exists(apkTool)){
            // 检测 temp 目录下的 apktool 是否已经过期
            if(AppManager.isReleased){
                File jarFile = JarUtils.getJarFile(AppManager.class);
                Long lastUpdatedTime = JarUtils.getLastUpdatedTime(jarFile, APKTOOL_PATH);
//                LogUtils.d("apktool lastUpdatedTime="+lastUpdatedTime + ", temp="+apkTool.lastModified());
                if(lastUpdatedTime != null && lastUpdatedTime > apkTool.lastModified()){
                    updateApkTool();
                }
            }else{
                File apkToolFile = new File(getProjectDir(), "lib.Res/src/" + APKTOOL_PATH);
                if(apkToolFile.lastModified() > apkTool.lastModified()){
                    updateApkTool();
                }
            }
        }else{
            updateApkTool();
        }
    }

    /**
     * 更新 temp 目录下的 apktool
     */
    private static void updateApkTool() {
        LogUtils.d("updateApkTool...");
        File frameworkRes = new File(apkTool.getParentFile(), FRAMEWORK_FILE);
        FileHelper.delete(apkTool);
        if(AppManager.isReleased){
            // 已发布
            ClassUtils.releaseResourceToFile(APKTOOL_PATH, apkTool);
            ClassUtils.releaseResourceToFile(FRAMEWORK_PATH, frameworkRes);
        }else{
            // 开发中
            // 拷贝工程 src 目录下的apktool.jar
            File apkToolFile = new File(getProjectDir(), "lib.Res/src/" + APKTOOL_PATH);
            FileHelper.copyFile(apkToolFile, apkTool);
            File frameworkResFile = new File(getProjectDir(), "lib.Res/src/" + FRAMEWORK_PATH);
            FileHelper.copyFile(frameworkResFile, frameworkRes);
        }
        ApkToolPlus.installFramework(apkTool, frameworkRes);
        FileHelper.delete(frameworkRes);
    }

    public static void init() {
        // 开发环境：out\production\ApkToolPlus
        // Release环境：ApkToolPlus.jar
        isReleased = !getRoot().isDirectory();
        // 初始化配置
        Config.init();
        // 初始化ApkToolPlus
        initApkTool();
    }

    /**
     * 退出程序
     */
    public static void exit (){
        if(TaskManager.get().queueSize() > 0){
            Global.dialog("当前有任务未完成，是否确认退出程序?",new Callback<Integer>(){
                @Override
                public void callback(Integer integer) {
                    quit();
                }
            },null);
        }else{
            quit();
        }
    }

    /**
     * 退出程序
     */
    private static void quit(){
        // 退出程序
        Platform.runLater(() -> {
            // 退出监听
            PlatformImpl.addListener(new PlatformImpl.FinishListener() {
                @Override
                public void idle(boolean implicitExit) {
                    LogUtils.d( "FinishListener idle");
                }
                @Override
                public void exitCalled() {
                    LogUtils.d( "FinishListener exitCalled");
                    System.exit(0); //kill process
                }
            });
            Platform.exit();
        });
    }


    /**
     * 打开一个url
     *
     * @param uri
     */
    public static void browser(URI uri){
        try {
            if(uri == null){
                return;
            }
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开一个url
     *
     * @param uri
     */
    public static void browser(String uri){
        try {
            browser(new URI(uri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开一个文件，或者可用于运行一个cmd文件
     *
     * @param file
     */
    public static void browser(File file){
        if(file == null){
            return;
        }
        if(!file.exists()){
            return;
        }
        browser(file.toURI());
    }

    /**
     * 获取应用版本
     *
     * @return
     */
    public static String getVersion(){
        return Config.getVersion();
    }

    /**
     * 获取根路径，如果released返回的是该jar的File对象，否则是工程的根目录
     *
     * @return
     */
    public static File getRoot(){
        return new File(ClassUtils.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    }

    /**
     * 获取运行时目录
     *
     * @return
     */
    public static File getRuntimeDir(){
        return getRoot().getParentFile();
    }

    /**
     * 获取工程目录
     *
     * @return
     */
    public static File getProjectDir(){
        return getRuntimeDir().getParentFile().getParentFile();
    }

    /**
     * 获取缓存目录
     *
     * @return
     */
    public static File getTempDir(){
        File tempDir = new File(getRuntimeDir(), "Temp");
        if(!tempDir.exists()){
            tempDir.mkdirs();
        }
        return tempDir;
    }

    /**
     * 获取文件输出目录
     *
     * @return
     */
    public static File getOutputDir(){
        String dirPath = Config.get(Config.kAppOutputDir, null);
        File dir;
        if(!StringUtils.isEmpty(dirPath)){
            dir = new File(dirPath);
            dir.mkdirs();
            if(dir.isDirectory()){
                return dir;
            }
            FileHelper.delete(dir);
        }
        dir = new File(getRuntimeDir(),"ApkToolPlus_Files");
        dir.mkdirs();
        return dir;
    }

    /**
     * 获取日志目录
     *
     * @return
     */
    public static File getLogDir(){
        File logDir = new File(getOutputDir(), "Log");
        if(!logDir.exists()){
            logDir.mkdirs();
        }
        return logDir;
    }

    /**
     * 拷贝一个文件或目录到缓存目录
     *
     * @param file      要拷贝的文件
     * @param outName   输出文件名
     * @return  返回输出的文件对象
     */
    public static File copyToTemp(File file, String outName){
        if(file == null || !file.exists() || StringUtils.isEmpty(outName)){
            return null;
        }
        File outFile = new File(getTempDir(), outName);
        if(file.isFile()){
            FileHelper.copyFile(file, outFile);
        }else{
            FileHelper.copyDir(file,outFile);
        }
        return outFile;
    }

    /**
     * 在sublime中打开文件
     *
     * @param file
     * @return  是否打开成功
     */
    public static boolean showInSublime(File file){
        return showInSublime(file,false);
    }

    public static boolean showInSublime(File file, boolean isAdd){
        if(!FileHelper.exists(file)){
            LogUtils.e("file must exist");
            return false;
        }
        String sublimePath = Config.get(Config.kSublimePath);
        if(sublimePath != null){
            File sublime = new File(sublimePath);
            if(sublime.exists()){
                String sublimeCmdParams = Config.get(Config.kSublimeCmdParams);
                String cmd;
                if(!StringUtils.isEmpty(sublimeCmdParams)){
                    // %target% : 标识目标文件或目录
                    String finalCmdParams = sublimeCmdParams.replaceAll("(%target%)+", Matcher.quoteReplacement(file.getAbsolutePath()));
                    cmd = sublimePath + " "  + finalCmdParams;
                }else{
                    cmd = sublimePath + " "  + file.getAbsolutePath();
                }
                if(isAdd){
                    cmd = cmd + " --add";
                }
                CmdUtils.exec(cmd,false);
                return true;
            }
        }else{
            LogUtils.e("sublime path can'n be null");
        }
        return false;
    }

    public static void showInSublime(List<File> fileList){
        if(fileList == null || fileList.isEmpty()){
            return;
        }
        for(File file : fileList){
            showInSublime(file);
        }
    }
}
