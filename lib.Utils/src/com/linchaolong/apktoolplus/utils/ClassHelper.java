package com.linchaolong.apktoolplus.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by linchaolong on 2015/9/9.
 */
public class ClassHelper {

    public static final String TAG = ClassHelper.class.getSimpleName();

    /**
     * 获取类包名
     *
     * @param clazz 类
     * @return  包名，如java.land.String，则返回java.lang
     */
    public static String getPackageName(Class clazz){
        return clazz.getPackage().getName();
    }

    /**
     * 获取源码路径
     *
     * @return
     */
    public static File getCodeSourcePath(){
        return new File(ClassHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    }

    /**
     * 从类路径下搜素指定资源并返回该资源的URL
     *
     * @param resPath   资源路径
     * @return  资源URL
     */
    public static URL getResourceAsURL(String resPath){
        return ClassLoader.getSystemResource(resPath);
    }

    /**
     * 从类路径下搜素指定资源并返回该资源的File对象
     *
     * @param resPath   资源路径
     * @return
     */
    public static File getResourceAsFile(String resPath){
        URL url = ClassLoader.getSystemResource(resPath);
        if(url == null){
            Debug.d("getResourceAsFile " + resPath + " not found.");
            return null;
        }
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从类路径下搜素指定资源并返回该资源的流对象
     *
     * @param resPath   资源路径
     * @return
     */
    public static InputStream getResourceAsStream(String resPath){
        return ClassLoader.getSystemResourceAsStream(resPath);
    }

    /**
     * 释放指定类路径下资源到指定路径
     *
     * @param clazz
     * @param resName
     * @param outFile
     * @return
     */
    public static boolean releaseResourceToFile(Class<?> clazz, String resName, File outFile){
        return releaseResourceToFile(clazz.getPackage().getName().replaceAll("\\.","/")+"/"+resName, outFile);
    }

    /**
     * 释放类路径下资源到指定路径
     *
     * @param resPath
     * @param outFile
     * @return
     */
    public static boolean releaseResourceToFile(String resPath, File outFile){
        InputStream in = getResourceAsStream(resPath);
        if(in == null){
            return false;
        }
        try {
            FileUtils.copyInputStreamToFile(in,outFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return outFile.exists();
    }
}
