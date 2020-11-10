package com.linchaolong.apktoolplus.core.packagetool;

import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.Logger;

import java.io.File;

public class ResMerger {

    public static void copySo(File soDir, File destDir) {
        File[] files = destDir.listFiles();
        if (destDir.exists() && destDir.isDirectory() && files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File srcDir = new File(soDir, file.getName());
                    if (srcDir.exists() && srcDir.isDirectory()) {
                        FileHelper.copyDir(srcDir, file, false);
                    }
                }
            }
        } else {
            FileHelper.copyDir(soDir, destDir, false);
        }
    }

    public static void copyRes(File srcDir, File destDir) {
        if (!srcDir.exists()) {
            return;
        }
        File[] files = srcDir.listFiles();
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File dir = new File(destDir, file.getName());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    boolean isValues = file.getName().startsWith("values");
                    File[] childFiles = file.listFiles();
                    if (childFiles != null && childFiles.length > 0) {
                        for (File childFile : childFiles) {
                            File destFile;
                            if (isValues) {
                                // values文件加个前缀防止和原工程values冲突
                                destFile = new File(dir, srcDir.getName() + "_" + childFile.getName());
                            } else {
                                destFile = new File(dir, childFile.getName());
                            }
                            Logger.print("copy res %s to %s.", childFile.getPath(), destFile.getPath());
                            FileHelper.copyFile(childFile, destFile);
                        }
                    }
                }
            }
        } else {
            FileHelper.copyDir(srcDir, destDir, false);
        }
    }

}
