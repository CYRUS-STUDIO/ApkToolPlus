package com.linchaolong.apktoolplus.core;

import com.linchaolong.apktoolplus.utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Jad，java反编译工具
 *
 * Created by linchaolong on 2015/11/15.
 */
public class Jad {

    public static final String TAG = Jad.class.getSimpleName();
    public static File jad;


    private Jad() {
    }

    /**
     * 反编译java字节码
     *
     * @param target    类路径或jar
     * @param out       源码输出路径（注意：jad不支持包含中文的路径）
     * @return          是否反编译成功
     */
    public static boolean decompileByCmd(File target, File out){
        if(target == null || !target.exists() || out == null){
            return false;
        }

        // clean
        FileHelper.delete(out);

        File classes = new File(AppManager.getTempDir(), "jad_classes");
        if(!target.equals(classes)){
            FileHelper.delete(classes);
            if (target.isFile()){
                // 解压文件到缓存目录
                boolean result = ZipHelper.unzip(target, classes);
                if(!result){
                    return false;
                }
            }else{
                if(!FileHelper.copyDir(target,classes)){
                    return false;
                }
            }
        }

        // 检查jad
        checkJad();

        //jad -r -ff -d src -s java classes/**/*.class
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append(jad.getPath()).append(" ")
                  .append("-r -ff -s java ")
                  .append("-d ").append(out.getPath()).append(" ")
                  .append(classes.getPath()).append("/**/*.class");
//        Cmd.exec(cmdBuilder.toString());
        //问题：通过Runtime.exec启动jad，反编译大文件是反编译并不完整（比如8M的jar），会卡在某个地方，但没报错。
        //解决方案：动态生成批处理，通过Desktop.browser打开该批处理。

        String cmdSuffix = ".bat";
        if(!OS.isWindows()){
            cmdSuffix = ".sh";
        }
        File cmdFile = new File(AppManager.getTempDir(),"jad_cmd"+cmdSuffix);
        cmdFile.delete();
        OutputStreamWriter output = null;
        try {
            output = new OutputStreamWriter(new FileOutputStream(cmdFile), OS.getCharset());
            output.write(cmdBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            IO.close(output);
        }
        AppManager.browser(cmdFile);
        return true;
    }

    /**
     * 反编译java字节码
     *
     * @param target    类路径或jar
     * @param out       源码输出路径
     * @param isOutZip  是否输出为zip文件，输出的zip文件在out同级目录下，命名与out相同，格式为zip
     * @return          是否反编译成功
     */
    public static boolean decompile(File target, File out, boolean isOutZip){
        if(target == null || !target.exists() || out == null){
            return false;
        }

        File classes = new File(AppManager.getTempDir(), "jad_classes");
        if(!target.equals(classes)){
            FileHelper.delete(classes);
            if (target.isFile()){
                // 解压文件到缓存目录
                boolean result = ZipHelper.unzip(target, classes);
                if(!result){
                    return false;
                }
            }else{
                if(!FileHelper.copyDir(target,classes)){
                    return false;
                }
            }
        }

        // 检查jad
        checkJad();

        File src = new File(classes.getParentFile(),"jad_src");
        FileHelper.delete(src);

        //jad -r -ff -d src -s java classes/**/*.class
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append(jad.getPath()).append(" ")
                .append("-r -ff -s java ")
                .append("-d ").append(src.getPath()).append(" ")
                .append(classes.getPath()).append("/**/*.class");
        Cmd.exec(cmdBuilder.toString(), jad.getParentFile(), true);

        // clean
        if(!src.equals(out)){
            FileHelper.delete(out);
            src.renameTo(out);
        }

        File finalOut = out;
        // 输出zip文件
        if(isOutZip){
            finalOut = new File(out.getParentFile(),out.getName()+".zip");
            finalOut.delete();
            ZipHelper.zip(out.listFiles(), finalOut);
        }
        return finalOut.exists();
    }

    private static void checkJad(){
        if(jad == null || !jad.exists()){
            String filePath;
            if(OS.isWindows()){
                filePath = "jad/bin/windows/jad.exe";
            }else if(OS.isMacOSX()){
                filePath = "jad/bin/macosx/jad";
            }else if(OS.isUnix()){
                filePath = "jad/bin/linux/jad";
            }else{
                throw new RuntimeException("jad not support this system.");
            }
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            jad = new File(AppManager.getTempDir(),fileName);
            ClassHelper.releaseResourceToFile(filePath,jad);
        }
    }

}
