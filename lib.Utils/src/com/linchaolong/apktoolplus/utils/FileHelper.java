package com.linchaolong.apktoolplus.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linchaolong on 2015/9/5.
 */
public class FileHelper {

    public static final String TAG = FileHelper.class.getSimpleName();

    public static final int KB = 1024;
    public static final int MB = KB * 1024;
    public static final int GB = MB * 1024;

    public static String getNoSuffixName(File file){
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if(dotIndex == -1){
            return name;
        }
        return name.substring(0, dotIndex);
    }

    /**
     * 判断文件格式
     *
     * @param file      文件
     * @param suffix    后缀，不带“.”
     * @return  true:格式匹配成功; false:格式匹配不成功。
     */
    public static boolean isSuffix(File file, String suffix){
        if (file == null || !file.exists() || suffix == null) return false;
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if(dotIndex == -1){
            return false;
        }
        String fileSuffix = name.substring(dotIndex);
        return fileSuffix.equalsIgnoreCase("."+suffix);
    }

    /**
     * 根据路径的字符串创建路径
     *
     * @param path  文件或目录路径
     * @return 创建路径是否出错
     */
    public static boolean makePath(String path){

        File file = new File(path);

        if(file.exists()){
            return true;
        }

        if (path.lastIndexOf('.')!=-1){

            int lastIndexOf = path.lastIndexOf('/');
            if(lastIndexOf == -1){
                lastIndexOf = path.lastIndexOf('\\');
            }
            if (lastIndexOf == -1){
                Debug.w("makePath error : path '" + path + "' is not legal");
                return false;
            }

            File parentDir = new File(path.substring(0,lastIndexOf));
            if (!parentDir.exists()){
                parentDir.mkdirs();
            }
            return true;
        }else{

            file.mkdir();
            return true;
        }
    }

    /**
     * 递归文件
     *
     * @param file 根目录或文件
     * @param fileHandler 文件处理器，如果handle方法返回true表示递归子目录与文件，否则不递归
     */
    public static void recusive(File file, FileHandler fileHandler){

        if (file == null || !file.exists() || fileHandler == null) {
            return;
        }

        if (fileHandler.handle(file)) {
            // 递归
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for(File tmp : files){
                    recusive(tmp, fileHandler);
                }
            }
        }
    }

    /**
     * 拷贝一个目录
     *
     * @param dir
     * @param toDir
     */
    public static boolean copyDir(File dir, File toDir) {
        return copyDir(dir,toDir,true);
    }

    /**
     * 拷贝一个目录
     *
     * @param dir
     * @param toDir
     * @param includeDir    是否包含dir目录
     * @return
     */
    public static boolean copyDir(File dir, File toDir, boolean includeDir) {
        try {
            if(includeDir){
                FileUtils.copyDirectoryToDirectory(dir,toDir);
            }else{
                FileUtils.copyDirectory(dir,toDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return toDir.exists();
    }

    /**
     * 拷贝一个文件
     *
     * @param file
     * @param toFile
     */
    public static boolean copyFile(File file, File toFile){
        try {
            FileUtils.copyFile(file,toFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return toFile.exists();
    }

    /**
     * 拷贝一个文件或目录
     *
     * @param file
     * @param toFile
     * @return
     */
    public static boolean copy(File file, File toFile){
        if(file.isFile()){
            copyFile(file,toFile);
        }else{
            copyDir(file,toFile);
        }
        return toFile.exists();
    }

    /**
     * 删除一个文件或目录
     *
     * @param file
     * @return
     */
    public static boolean delete(File file){
        if(!file.exists()){
            return false;
        }
        if(file.isFile()){
            return file.delete();
        }
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 清理一个目录
     *
     * @param dir
     * @return
     */
    public static boolean cleanDirectory(File dir){
        try {
            if(!exists(dir) || dir.isFile()){
                return false;
            }
            FileUtils.cleanDirectory(dir);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 文件是否存在
     *
     * @param file
     *
     * @return
     */
    public static boolean exists(File file){
        return file != null && file.exists();
    }

    /**
     * 移动一个文件或目录
     *
     * @param file    文件
     * @param dest    目标路径
     * @return
     */
    public static boolean move(File file, File dest){
        if(!exists(file)){
            return false;
        }
        if(file.isDirectory()){
            try {
                FileUtils.moveDirectory(file,dest);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                FileUtils.moveFile(file,dest);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取所有文件和目录
     *
     * @param file
     * @return
     */
    public static List<File> listAllFiles(File file){
        ArrayList<File> files = new ArrayList<>();
        recusive(file, f -> {
            files.add(f);
            return true;
        });
        return files;
    }

    /**
     * 打开文件所在目录
     *
     * @param file
     */
    public static void showInExplorer(File file){
        if(FileHelper.exists(file)){
            showInExplorer(file.getAbsolutePath());
        }
    }

    /**
     * 打开文件所在目录
     *
     * @param path
     */
    public static void showInExplorer(String path){
        if (StringUtils.isEmpty(path)) return;
        if(OS.isWindows()){
            Cmd.exec("explorer.exe /select," + path);
        }else{
            // linux/mac
            Cmd.exec("open " + path);
        }
    }

}
