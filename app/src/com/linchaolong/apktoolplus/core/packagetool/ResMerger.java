package com.linchaolong.apktoolplus.core.packagetool;

import com.linchaolong.apktoolplus.utils.*;
import org.apache.commons.io.FileUtils;
import org.dom4j.*;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

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

    public static void setString(File stringsXml, File output, Map<String, String> values) {
        try {
            String content = FileUtils.readFileToString(stringsXml);
            Document document = DocumentHelper.parseText(content);

            for (Map.Entry<String, String> entry : values.entrySet()) {
                Node nameNode = document.selectSingleNode("//resources/string[@name='" + entry.getKey() + "']");
                if (nameNode != null) {
                    nameNode.setText(entry.getValue());
                } else {
                    Element resourcesNode = (Element) document.selectSingleNode("//resources");
                    Element element = resourcesNode.addElement("string");
                    element.addAttribute("name", entry.getKey());
                    element.setText(entry.getValue());
                }
            }

            FileWriter fileWriter = new FileWriter(output);
            document.write(fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void mergeRes(File[] dirList, File outDir) {

        FileHelper.cleanDirectory(outDir);

        for (File dir : dirList) {

            String rootName = dir.getName().toLowerCase().replace("-", "_").replace(".", "_");

            new FileWalker() {
                @Override
                public void handle(File file) {
                    // res目录
                    if (file.isDirectory() && file.getName().equals("res")) {

                        Logger.print(file.getPath());

                        // res目录下所有文件
                        new FileWalker() {

                            @Override
                            public void handle(File file) {

                                if (file.isFile() && !file.getName().equals(".gitignore")) {

                                    String parentName = file.getParentFile().getName();

                                    boolean isValues = parentName.contains("values");

                                    String name = parentName + "/" + (isValues ? rootName + "_" : "");

                                    File destFile = new File(outDir, name + file.getName());

                                    if (isValues && destFile.exists()) {

                                        for (int i = 0; i < 10; i++) {
                                            destFile = new File(outDir, name + i + "_" + file.getName());

                                            if (!destFile.exists()) {
                                                break;
                                            }
                                        }
                                    }

                                    if (destFile.exists()) {
                                        Logger.error(destFile.getPath() + " is exist!!!");
                                    }

                                    FileHelper.copyFile(file, destFile);

                                    Logger.print("copy " + file.getPath() + " to " + destFile.getPath());
                                }
                            }
                        }.walk(file.getAbsolutePath());
                    }
                }
            }.walk(dir.getAbsolutePath());
        }
    }

}
