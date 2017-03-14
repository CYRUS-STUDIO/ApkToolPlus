package com.linchaolong.apktoolplus.module.main;

import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.ZipUtils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-Dex Support
 * <p>
 * Created by linch on 2016/3/28.
 */
public abstract class MultDexSupport {

    public static List<FileHeader> getDexFileHeaders(File apkFile) {
        return ZipUtils.listForPattern(apkFile, "[^/]*\\.dex");
    }

    protected List<File> unzipDexList(File apk, File tempDir){
        List<FileHeader> dexFileHeaders = MultDexSupport.getDexFileHeaders(apk);
        if(dexFileHeaders.isEmpty()){
            return new ArrayList<>(0);
        }
        // 解压dex文件
        tempDir.mkdirs();
        List<File> dexFileList = new ArrayList<>(dexFileHeaders.size());
        try {
            for(FileHeader dexFileHeader : dexFileHeaders){
                ZipFile zipFile = new ZipFile(apk);
                boolean unzip = ZipUtils.unzip(zipFile, dexFileHeader, tempDir);
                if(unzip){
                    dexFileList.add(new File(tempDir,dexFileHeader.getFileName()));
                }
            }
            return dexFileList;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    protected void clearDexTemp(List<File> dexFileList){
        for(File dexFile : dexFileList){
            FileHelper.delete(dexFile);
        }
    }

}