package com.linchaolong.apktoolplus.core;

import brut.androlib.AndrolibException;
import brut.androlib.src.SmaliBuilder;
import brut.apktool.Main;
import brut.common.BrutException;
import brut.directory.ExtFile;
import com.googlecode.dex2jar.tools.Dex2jarCmd;
import com.googlecode.dex2jar.tools.Jar2Dex;
import com.googlecode.dex2jar.tools.StdApkCmd;
import com.linchaolong.apktoolplus.utils.CmdUtils;
import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.LogUtils;
import com.linchaolong.apktoolplus.utils.ZipUtils;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.InlineMethodResolver;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by linchaolong on 2015/8/30.
 */
public class ApkToolPlus {

    public static final String TAG = ApkToolPlus.class.getSimpleName();

    /**
     * 该方法会创建一个ClassLoader，并把classpaths设置为默认搜索路径，然后设置为当前线程的上下文ClassLoader
     * ，原上下文ClassLoader为父ClassLoader
     *
     * @param classpaths 类路径数组
     * @return ClassLoader
     */
    public static ClassLoader initClassPath(String[] classpaths) {
        if (classpaths == null || classpaths.length == 0)
            return null;
        //这里假定任何以 '/' 结束的 URL 都是指向目录的。如果不是以该字符结束，则认为该 URL 指向一个将根据需要下载和打开的 JAR 文件。
        // Add the conf dir to the classpath
        // Chain the current thread classloader
        try {
            List<URL> urls = new ArrayList<>(classpaths.length);
            for (String path : classpaths) {
                urls.add(new File(path).toURI().toURL());
            }
            ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader urlClassLoader = new URLClassLoader((URL[]) urls.toArray(), currentThreadClassLoader);
            // Replace the thread classloader - assumes
            // you have permissions to do so
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            return urlClassLoader;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反编译apk
     *
     * @param apk          apk文件
     * @param outDir       输出目录
     * @param onExceptioin 异常处理
     * @return 是否正常
     */
    public static boolean decompile(File apk, File outDir, Callback<Exception> onExceptioin) {
        try {
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            if (outDir == null) {
                runApkTool(new String[]{"d", apk.getPath()});
            } else {
                runApkTool(new String[]{"d", apk.getPath(), "-o", outDir.getPath(), "-f"});
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (onExceptioin != null) {
                onExceptioin.callback(e);
            }
            return false;
        }
        return true;
    }

    /**
     * 回编译apk
     *
     * @param folder       apk反编译目录
     * @param outApk       apk输出路径，如果为null，则输出到默认路径
     * @param onExceptioin 异常处理，可为null
     * @return 是否正常
     */
    public static boolean recompile(File folder, File outApk, Callback<Exception> onExceptioin) {
        try {
            if (outApk == null) {
                runApkTool(new String[]{"b", folder.getPath()});
            } else {
                runApkTool(new String[]{"b", folder.getPath(), "-o", outApk.getPath()});
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (onExceptioin != null) {
                onExceptioin.callback(e);
            }
            return false;
        }
        return true;
    }

    /**
     * .class转换为.dex
     *
     * @param jarFile       jar文件
     * @param outputDexPath dex文件输出路径
     * @return 是否转换成功
     */
    public static boolean jar2dex(File jarFile, String outputDexPath) {
        return class2dex(jarFile, outputDexPath);
    }

    /**
     * .class转换为.dex
     *
     * @param classesDir    类路径
     * @param outputDexPath dex文件输出路径
     * @return 是否转换成功
     */
    public static boolean class2dex(File classesDir, String outputDexPath) {

        // 检查类路径
        if (!classesDir.exists()) {
            LogUtils.w("class2dex error : classPath is not exists.");
            return false;
        }

        // 创建输出路径
        if (!FileHelper.makePath(outputDexPath)) {
            LogUtils.w("makePath error : outputDexPath '" + outputDexPath + "' make fail");
            return false;
        }

        // class -> dex
        com.android.dx.command.dexer.Main.Arguments arguments = new com.android.dx.command.dexer.Main.Arguments();
        arguments.outName = outputDexPath;
        arguments.strictNameCheck = false;
        arguments.fileNames = new String[]{classesDir.getPath()};
        try {
            com.android.dx.command.dexer.Main.run(arguments);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * .dex转换为smali
     *
     * @param dexFile dex/apk文件
     * @param outDir  smali文件输出目录
     * @return 是否转换成功
     */
    public static boolean dex2smali(File dexFile, File outDir) {

        if (dexFile == null || !dexFile.exists()) {
            LogUtils.w("dex2smali dexFile is null or not exists : " + dexFile.getPath());
            return false;
        }

        // dex文件的处理
        BaksmaliOptions options = new BaksmaliOptions();

        // options
        options.deodex = false;
        options.implicitReferences = false;
        options.parameterRegisters = true;
        options.localsDirective = true;
        options.sequentialLabels = true;
        options.debugInfo = true;
        options.codeOffsets = false;
        options.accessorComments = false;
        options.registerInfo = 0;
        options.inlineResolver = null;

        // set jobs automatically
        int jobs = Runtime.getRuntime().availableProcessors();
        if (jobs > 6) {
            jobs = 6;
        }

        try {
            //brut/androlib/ApkDecoder.mApi default value is 15
            // create the dex
            DexBackedDexFile dexBackedDexFile = DexFileFactory.loadDexFile(dexFile, Opcodes.forApi(15));

            if (dexBackedDexFile.isOdexFile()) {
                LogUtils.w("Warning: You are disassembling an odex file without deodexing it.");
            }

            if (dexBackedDexFile instanceof DexBackedOdexFile) {
                options.inlineResolver =
                        InlineMethodResolver.createInlineMethodResolver(((DexBackedOdexFile) dexBackedDexFile).getOdexVersion());
            }

            Baksmali.disassembleDexFile(dexBackedDexFile, outDir, jobs, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * .jar转换为.smali
     *
     * @param jarFile Jar文件
     * @param outDir  smali文件输出目录
     * @return 是否转换成功
     */
    public static boolean jar2smali(File jarFile, File outDir) {

        if (!jarFile.exists() || jarFile.isDirectory()) {
            LogUtils.w("jar2smali error : jar file '" + jarFile.getPath() + "' is not exists or is a directory.");
            return false;
        }
        return class2smali(jarFile, outDir);
    }

    /**
     * .class转换为.smali
     *
     * @param classesDir 类路径
     * @param outDir     smali文件输出目录
     * @return 是否转换成功
     */
    public static boolean class2smali(File classesDir, File outDir) {
        if (!classesDir.exists()) {
            LogUtils.w("class2smali error : classpath '" + classesDir.getPath() + "' is not exists.");
            return false;
        }

        // clean temp
        File dexFile = new File(classesDir.getParentFile(), "temp.dex");
        dexFile.delete();

        // class -> dex
        if (class2dex(classesDir, dexFile.getPath())) {
            // dex -> smali
            if (dex2smali(dexFile, outDir)) {
                LogUtils.d("class2smali succcess");
            } else {
                LogUtils.e("class2smali error : dex2smali error");
            }

            // clean temp
            dexFile.delete();
            return true;
        } else {
            LogUtils.e("class2smali error : class2dex error");
            return false;
        }
    }

    /**
     * .smali转换.dex
     *
     * @param smaliDirPath  smali文件目录或zip文件路径
     * @param dexOutputPath dex文件输出路径
     * @return 是否转换成功
     */
    public static boolean smali2dex(String smaliDirPath, String dexOutputPath) {
        ExtFile smaliDir = new ExtFile(new File(smaliDirPath));
        if (!smaliDir.exists()) {
            LogUtils.w("smali2dex error : smali dir '" + smaliDirPath + "' is not exists");
            return false;
        }

        // 创建输出路径
        if (!FileHelper.makePath(dexOutputPath)) {
            LogUtils.w("makePath error : dexOutputPath '" + dexOutputPath + "' make fail");
            return false;
        }

        File dexFile = new File(dexOutputPath);
        dexFile.delete();

        try {
            // smali -> dex
            SmaliBuilder.build(smaliDir, dexFile);
            return true;
        } catch (AndrolibException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * dex2jar
     *
     * @param file    dex文件或者apk文件，如果是apk会直接读取apk中的classes.dex
     * @param jarFile
     */
    public static boolean dex2jar(File file, File jarFile) {
        //d2j-dex2jar classes.dex --output output.jar
        if (file == null || !file.exists() || jarFile == null) {
            return false;
        }
        // dex2jar会判断如果是apk则读取apk中的classes.dex文件
        Dex2jarCmd.main(file.getPath(), "--output", jarFile.getPath(), "--force"); //--force覆盖存在文件
        return jarFile.exists();
    }

    /**
     * jar2dex
     *
     * @param jarFile
     * @param dexFile
     * @return
     */
    public static boolean jar2dex(File jarFile, File dexFile) {
        if (jarFile == null || !jarFile.exists() || dexFile == null) {
            return false;
        }
        Jar2Dex.main(jarFile.getPath(), "--output", dexFile.getPath());
        return dexFile.exists();
    }


    /**
     * 将一个apk转换为标准zip文件
     *
     * @param apkFile
     * @param zipFile
     * @return
     */
    public static boolean apk2zip(File apkFile, File zipFile) {
        //d2j-std-apk hqg.apk -o hqg.zip
        if (apkFile == null || !apkFile.exists() || zipFile == null) {
            return false;
        }
        StdApkCmd.main(apkFile.getPath(), "-o", zipFile.getPath());
        return zipFile.exists();
    }

    /**
     * 对apk进行签名，签名apk将被输出到该apk相同目录下，名称格式为APK_NAME_signed.apk。
     *
     * @param apk    apk文件
     * @param config keystore文件配置
     * @return 返回签名apk
     */
    public static File signApk(File apk, KeystoreConfig config) {

        if (!apk.exists() || !apk.isFile()) {
            throw new RuntimeException("sign apk error : file '" + apk.getPath() + "' is no exits or not a file.");
        }

        File apkCopy = new File(apk.getParentFile(), "copy_" + apk.getName());
        FileHelper.delete(apkCopy);

        FileHelper.copyFile(apk, apkCopy);
        //删除META-INF目录，防止包含多个签名问题
        ZipUtils.removeFileFromZip(apkCopy, "META-INF");

        File signedApk = new File(apk.getParentFile(), FileHelper.getNoSuffixName(apk) + "_signed.apk");
        FileHelper.delete(signedApk);

        //jarsigner -digestalg SHA1 -sigalg MD5withRSA -keystore keystore路径 -storepass 密码 -keypass 别名密码 -signedjar signed_xxx.apk xxx.apk 别名
        StringBuilder cmdBuilder = new StringBuilder("jarsigner -digestalg SHA1 -sigalg MD5withRSA");
        cmdBuilder.append(" -keystore ").append(config.keystorePath);
        cmdBuilder.append(" -storepass ").append(config.keystorePassword);
        cmdBuilder.append(" -keypass ").append(config.aliasPassword);
        cmdBuilder.append(" -signedjar ").append(signedApk.getPath()).append(" ").append(apkCopy.getPath()).append(" ");
        cmdBuilder.append(" ").append(config.alias);
        String cmd = cmdBuilder.toString();

        // 执行命令
        CmdUtils.exec(cmd);

        // clean
        FileHelper.delete(apkCopy);

        return signedApk;
    }

    /**
     * 对apk进行v2签名，签名apk将被输出到该apk相同目录下，名称格式为APK_NAME_signed.apk。
     *
     * @param apksigner apk文件
     * @param apk       apk文件
     * @param output    签名后的apk文件输出路径
     * @param config    keystore文件配置
     * @return 返回签名apk
     */
    public static File signApkV2(File apksigner, File apk, File output, KeystoreConfig config) {

        if (!apk.exists() || !apk.isFile()) {
            throw new RuntimeException("sign apk error : file '" + apk.getPath() + "' is no exits or not a file.");
        }

        File apkCopy = new File(apk.getParentFile(), "copy_" + apk.getName());
        FileHelper.delete(apkCopy);

        FileHelper.copyFile(apk, apkCopy);
        //删除META-INF目录，防止包含多个签名问题
        ZipUtils.removeFileFromZip(apkCopy, "META-INF");

        File signedApk = output == null ? new File(apk.getParentFile(), FileHelper.getNoSuffixName(apk) + "_signed.apk") : output;
        FileHelper.delete(signedApk);

        //apksigner sign --ks [jks path] --ks-key-alias [alias] --ks-pass pass:[key store password] --key-pass pass:[key password] --out [output path] [apk path]
        StringBuilder cmdBuilder = new StringBuilder(apksigner.getPath() + " sign");
        cmdBuilder.append(" --ks ").append(config.keystorePath);
        cmdBuilder.append(" --ks-pass pass:").append(config.keystorePassword);
        cmdBuilder.append(" --ks-key-alias ").append(config.alias);
        cmdBuilder.append(" --key-pass pass:").append(config.aliasPassword);
        cmdBuilder.append(" --out ").append(signedApk.getPath()).append(" ").append(apkCopy.getPath());
        String cmd = cmdBuilder.toString();

        // 执行命令
        CmdUtils.exec(cmd);

        // clean
        FileHelper.delete(apkCopy);

        return signedApk;
    }


    private static void safeRunApkTool(String[] args) {
        try {
            Main.main(args);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrutException e) {
            e.printStackTrace();
        }
    }

    public static void installFramework(File apkToolFile, File frameworkFile) {
        CmdUtils.exec("java -jar " + apkToolFile.getAbsolutePath() + " if " + frameworkFile.getAbsolutePath());
    }

    public static void installApk(File apkFile) {
        CmdUtils.exec("adb install " + apkFile.getAbsolutePath());
    }

    private static void runApkTool(String[] args) throws InterruptedException, BrutException, IOException {
        AppManager.initApkTool();
        //java -jar apktool.jar d test.apk -f
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("java -jar ")
                .append(AppManager.getApkTool().getPath());
        for (String arg : args) {
            cmdBuilder.append(" ").append(arg);
        }
        // 执行命令
        CmdUtils.exec(cmdBuilder.toString());
    }
}


