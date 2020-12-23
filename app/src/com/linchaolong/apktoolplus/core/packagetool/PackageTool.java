package com.linchaolong.apktoolplus.core.packagetool;

import com.linchaolong.apktoolplus.core.ApkToolPlus;
import com.linchaolong.apktoolplus.core.ApkToolYml;
import com.linchaolong.apktoolplus.core.KeystoreConfig;
import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.Logger;
import com.linchaolong.apktoolplus.utils.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PackageTool {

    private final String appIconName;

    public PackageTool(String appIconName) {
        this.appIconName = appIconName;
    }

    private void jar2smali(File jarDir, File smaliDir) {
        FileHelper.delete(smaliDir);
        if (jarDir.exists()) {
            File[] files = jarDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        Logger.print("jar2smali " + file.getPath() + " to " + smaliDir.getPath());
                        ApkToolPlus.jar2smali(file, smaliDir);
                    }
                }
            }
        }
    }

    private void mergeSDK(SDKConfig sdk, BuildConfig buildConfig, File decompileDir) {

        File smaliDir = new File(sdk.path, "smali");

        // jar2smali
        jar2smali(new File(sdk.path, "jar"), smaliDir);

        File manifest;
        if (buildConfig.landscape) {
            manifest = new File(sdk.path, "AndroidManifest.xml");
        } else {
            manifest = new File(sdk.path, "AndroidManifest-port.xml");
            if (!manifest.exists()) {
                manifest = new File(sdk.path, "AndroidManifest.xml");
            }
        }

        if (!manifest.exists()) {
            Logger.print(manifest.getPath() + " not exist!!!");
        } else {
            ManifestCombiner manifestCombiner = new ManifestCombiner(
                    new File(decompileDir, "AndroidManifest.xml"),
                    new File[]{manifest},
                    new File(decompileDir, "AndroidManifest.xml"))
                    .setSmaliDir(new File(decompileDir, "smali"))
                    .setApplicationId(buildConfig.packageName)
                    .setLabel(buildConfig.label);

            if (!StringUtils.isEmpty(buildConfig.versionCode)) {
                manifestCombiner.setVersionCode(buildConfig.versionCode);
            }

            if (!StringUtils.isEmpty(buildConfig.versionName)) {
                manifestCombiner.setVersionName(buildConfig.versionName);
            }

            if (!StringUtils.isEmpty(buildConfig.applicationName)) {
                manifestCombiner.setApplicationName(buildConfig.applicationName);
            }

            if (sdk.metaData != null && !sdk.metaData.isEmpty()) {
                manifestCombiner.setMetadata(sdk.metaData);
            }

            if (sdk.placeHolderValues != null && !sdk.placeHolderValues.isEmpty()) {
                manifestCombiner.setPlaceHolderValues(sdk.placeHolderValues);
            }

            // copy icon
            if (buildConfig.icon != null) {
                FileHelper.copyFile(buildConfig.icon, new File(decompileDir, "res\\mipmap-xxxhdpi-v4\\" + appIconName + ".png"));
                manifestCombiner.setIcon("@mipmap/" + appIconName);
            }

            // 合并manifest
            manifestCombiner.combine();
        }

        // copy smali
        FileHelper.copyDir(smaliDir, new File(decompileDir, "smali"), false);

        // copy assets
        FileHelper.copyDir(new File(sdk.path, "assets"), new File(decompileDir, "assets"), false);

        if (sdk.assetsFileList != null && !sdk.assetsFileList.isEmpty()) {
            for (Map.Entry<File, String> entry : sdk.assetsFileList.entrySet()) {
                File destFile = new File(decompileDir, "assets\\" + entry.getValue());
                FileHelper.copyFile(entry.getKey(), destFile);
                Logger.print("copy %s to %s", entry.getKey().getPath(), destFile.getPath());
            }
        }

        // copy res
//        FileHelper.copyDir(new File(sdk, "res"), new File(decompileDir, "res"), false);
        ResMerger.copyRes(new File(sdk.path, "res"), new File(decompileDir, "res"));

        // 修改游戏名
        File stringsXml = new File(decompileDir, "res\\values\\strings.xml");
        if (!StringUtils.isEmpty(buildConfig.label) && stringsXml.exists()) {
            Map<String, String> stringMap = new LinkedHashMap<>();
            stringMap.put("app_name", buildConfig.label);
            ResMerger.setString(stringsXml, stringsXml, stringMap);
        }

        // copy appcompat_res
        if (!hasSupportV7(decompileDir)) {
            ResMerger.copyRes(new File(sdk.path, "appcompat-v7_res"), new File(decompileDir, "res"));
        }else{
            Logger.print("has supportV7");
        }

        // copy so
        ResMerger.copySo(new File(sdk.path, "lib"), new File(decompileDir, "lib"));

        // file config
        if (sdk.fileConfig != null) {
            new FileConfigTool(new File(decompileDir, sdk.fileConfig.path))
                    .setParams(sdk.fileConfig.config)
                    .save();
        }
    }

    private boolean hasSupportV7(File decompileDir){
        try {
            String str = FileUtils.readFileToString(new File(decompileDir, "res/values/styles.xml"));
            return str.contains("Theme.AppCompat.Light.NoActionBar");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void copyFile(File decompileDir, Map<File, String> copyFile) {
        if (copyFile != null && !copyFile.isEmpty()) {
            for (Map.Entry<File, String> entry : copyFile.entrySet()) {
                File file = new File(decompileDir, entry.getValue());
                FileHelper.copyFile(entry.getKey(), file);
                Logger.print("copy %s to %s.", entry.getKey().getPath(), file.getPath());
            }
        }
    }

    public void build(BuildConfig buildConfig) {

        String name = FileHelper.getNoSuffixName(buildConfig.apk);
        File dir = buildConfig.apk.getParentFile();

        File decompileDir = new File(dir, name);
        File recompileApk = new File(dir, name + "_recompile.apk");
        String suffix = StringUtils.isEmpty(buildConfig.suffix) ? "" : "_" + buildConfig.suffix;
        File signedApk = new File(dir, name + "_signed" + suffix + ".apk");

        // 清理目录
        FileHelper.delete(decompileDir);

        // 解包
        ApkToolPlus.decompile(new File(dir, name + ".apk"), decompileDir, null);

        // 合并sdk
        for (SDKConfig sdk : buildConfig.sdkList) {
            mergeSDK(sdk, buildConfig, decompileDir);
        }

        // copy file
        copyFile(decompileDir, buildConfig.copyFile);

        // targetSdkVersion
        if (!StringUtils.isEmpty(buildConfig.targetSdkVersion)) {
            new ApkToolYml(new File(decompileDir, "apktool.yml")).setTargetVersion(buildConfig.targetSdkVersion).save();
        }

        // 回编译
        FileHelper.delete(recompileApk);
        ApkToolPlus.recompile(decompileDir, recompileApk, null);

        // 签名
        if (buildConfig.keystoreConfig != null) {
            FileHelper.delete(signedApk);
            ApkToolPlus.signApkV2(buildConfig.apkSigner, recompileApk, signedApk, buildConfig.keystoreConfig);

            FileHelper.delete(recompileApk);
        }
    }

    public static class SDKConfig {
        public File path;
        public Map<String, String> metaData;
        public Map<String, String> placeHolderValues;
        public Map<File, String> assetsFileList;
        public FileConfig fileConfig;
    }

    public static class FileConfig {
        public String path;
        public Map<String, String> config;
    }

    public static class BuildConfig {
        public File apk;
        public SDKConfig[] sdkList;
        public String packageName;
        public File icon;
        public String label;
        public KeystoreConfig keystoreConfig;
        public Map<File, String> copyFile;
        public boolean landscape;
        public File apkSigner;
        public String targetSdkVersion;
        public String versionCode;
        public String versionName;
        public String applicationName;
        public String suffix;
    }

}
