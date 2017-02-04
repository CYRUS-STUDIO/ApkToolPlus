package com.linchaolong.apktoolplus.module.main;

import com.linchaolong.apktoolplus.core.ApkToolPlus;
import com.linchaolong.apktoolplus.core.AppManager;
import com.linchaolong.apktoolplus.utils.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * dex2jar multi-dex support
 *
 * Created by linch on 2016/3/28.
 */
public abstract class Dex2JarMultDexSupport extends MultDexSupport{

    public Dex2JarMultDexSupport(File apk) {

        // 如果不是apk
        if(!FileHelper.isSuffix(apk,"apk")){
            onStart();
            File dexFile = apk;
            File jarFile = new File(dexFile.getParentFile(), dexFile.getName() + ".jar");
            boolean isSuccess;
            if(!jarFile.exists()){
                // 如果文件已经存在，跳过转换环节
                isSuccess = ApkToolPlus.dex2jar(dexFile, jarFile);
            }else{
                isSuccess = true;
            }
            if (isSuccess) {
                ArrayList<File> jarFileList = new ArrayList<>(1);
                jarFileList.add(jarFile);
                onEnd(jarFileList);
            } else {
                onFailure(dexFile);
            }
            return;
        }

        // start
        onStart();

        // 解压dex文件
        File tempDir = new File(AppManager.getTempDir(),"dex2smali"); // 缓存目录
        List<File> dexFileList = unzipDexList(apk, tempDir);
        if(dexFileList.isEmpty()){
            onEnd(null);
            return;
        }

        // dex2jar
        List<File> jarFileList = new ArrayList<>(dexFileList.size());
        boolean isSuccess;
        for(File dexFile : dexFileList){
            File jarFile = new File(apk.getParentFile(), FileHelper.getNoSuffixName(apk)+"_"+FileHelper.getNoSuffixName(dexFile) + ".jar");
            if(!jarFile.exists()){
                // 如果文件已经存在，跳过转换环节
                isSuccess = ApkToolPlus.dex2jar(dexFile, jarFile);
            }else{
                isSuccess = true;
            }
            if(isSuccess){
                jarFileList.add(jarFile);
            }else{
                onFailure(dexFile);
            }
        }

        // clear dex file temp
        clearDexTemp(dexFileList);

        // end
        onEnd(jarFileList);
    }


    public abstract void onStart();
    public abstract void onEnd(List<File> jarFileList);
    public abstract void onError(Exception e);
    public abstract void onFailure(File dexFile);

}
