package com.linchaolong.apktoolplus.utils;

import sun.misc.Launcher;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Jar文件相关的操作的帮助类
 *
 * Created by linchaolong on 2016/1/26.
 */
public class JarHelper {

    public static final String TAG = JarHelper.class.getSimpleName();

    /**
     * 获取jar文件对象
     *
     * @param clazz    如果传null，默认使用JarHelper.class
     * @return
     */
    public static File getJarFile(Class clazz){
        if(clazz == null){
            return new File(JarHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        }
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    /**
     * 遍历jar文件指定路径下的实体，如果是在IDE环境下则返回运行路径下指定文件路径集合
     *
     * @param clazz     如果传null，默认使用JarHelper.class
     * @param path      指定路径
     * @return  出现异常或指定路径下没有实体则返回空ArrayList
     */
    public static ArrayList<String> listJarEntries(Class clazz, String path){
        return listJarEntries(getJarFile(clazz), path);
    }

    /**
     * 遍历jar文件指定路径下的实体，如果是在IDE环境下则返回运行路径下指定文件路径集合
     *
     * @param jarFile   jar文件对象
     * @param path      指定路径
     * @return 出现异常或指定路径下没有实体则返回空ArrayList
     */
    public static ArrayList<String> listJarEntries(File jarFile, String path){
        if(jarFile.isFile()) {  // Run with JAR file
            try {
                final JarFile jar = new JarFile(jarFile);
                return listJarEntries(jar,path,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ArrayList<>(0);// 出错返回一个空ArrayList
        } else { // Run with IDE
            ArrayList<String> entryList = new ArrayList<>();
            final URL url = Launcher.class.getResource("/" + path);
            if (url != null) {
                try {
                    final File apps = new File(url.toURI());
                    for (File app : apps.listFiles()) {
                        Debug.d(app.toString());
                        entryList.add(app.getAbsolutePath());
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            return entryList;
        }
    }

    /**
     * 遍历jar文件指定路径下的实体
     *
     * @param jar
     * @param path
     * @param isCloseJar
     * @return 出现异常或指定路径下没有实体则返回空ArrayList
     */
    public static ArrayList<String> listJarEntries(JarFile jar, String path, boolean isCloseJar){
        ArrayList<String> entryList = new ArrayList<>();
        final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
        while(entries.hasMoreElements()) {
            final String name = entries.nextElement().getName();
            if (name.startsWith(path + "/")) { //filter according to the path
                Debug.d(name);
                entryList.add(name);
            }
        }
        if (isCloseJar){
            try {
                jar.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return entryList;
    }

    /**
     * 合并两个zip/jar/目录，注意：后面的文件会覆盖前面相同的文件
     *
     * @param f1
     * @param f2
     * @param outFile   输出文件
     */
    public static void merge(File f1, File f2, File outFile){
        // init
        File temp = new File(outFile.getParentFile(), FileHelper.getNoSuffixName(outFile));
        FileHelper.cleanDirectory(temp);
        if(f1.isDirectory()){
            FileHelper.copyDir(f1,temp, false);
        }else{
            ZipHelper.unzip(f1,temp);
        }
        // merge
        if(f2.isDirectory()){
            FileHelper.copyDir(f2, temp, false);
        }else{
            // unzip
            ZipHelper.unzip(f2,temp, false);
        }

        File manifestFile = new File(temp,"META-INF/MANIFEST.MF");
        FileHelper.delete(outFile);

        // jar -cvfm atp.jar META-INF/MANIFEST.MF *
        String cmd;
        if(FileHelper.exists(manifestFile)){
            cmd = "jar -cvfm "+outFile.getAbsolutePath()+" "+manifestFile.getAbsolutePath()+" *";
        }else{
            cmd = "jar -cvf "+outFile.getAbsolutePath()+" *";
        }
        // 关闭输出就会出现这个问题：Can't read [*] (Unexpected end of ZLIB input stream))
        Cmd.exec(cmd, temp, true);

        // clean
        FileHelper.delete(temp);
    }

    public static void main(String[] args) {
        try {
            new JarHelper().test("linchaolong/apktoolplus");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test(String path) throws Exception {
        final File jarFile = getJarFile(null);

        if(jarFile.isFile()) {  // Run with JAR file
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            while(entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith(path + "/")) { //filter according to the path
                    System.out.println(name);
                }
            }
            jar.close();
        } else { // Run with IDE
            final URL url = Launcher.class.getResource("/" + path);
            if (url != null) {
                try {
                    final File apps = new File(url.toURI());
                    for (File app : apps.listFiles()) {
                        System.out.println(app);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
