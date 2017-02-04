package com.linchaolong.apktoolplus.core.jiagu;

import com.linchaolong.apktoolplus.core.ApkToolPlus;
import com.linchaolong.apktoolplus.core.AppManager;
import com.linchaolong.apktoolplus.core.Callback;
import com.linchaolong.apktoolplus.core.KeystoreConfig;
import com.linchaolong.apktoolplus.utils.DataProtector;
import com.linchaolong.apktoolplus.utils.ClassHelper;
import com.linchaolong.apktoolplus.utils.Debug;
import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.ZipHelper;
import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.CertificateMeta;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * apk加固工具
 * <p>
 * Created by linchaolong on 2015/11/18.
 */
public class JiaGu {

    public static final String TAG = JiaGu.class.getSimpleName();

    public static final String JIAGU_ZIP = "jiagu.zip";
    private static final String JIAGU_DATA_BIN = "jiagu_data.bin";
    private static final String JIAGU_ZIP_PATH;

    static {
        JIAGU_ZIP_PATH = JiaGu.class.getPackage().getName().replaceAll("\\.","/") + "/" + JIAGU_ZIP;
    }

    private static final String PROXY_APPLICATION_NAME = "com.linchaolong.apktoolplus.jiagu.ProxyApplication";
    private static final String METADATA_SRC_APPLICATION = "apktoolplus_jiagu_app";

    // 工作目录
    private static File workDir = new File(AppManager.getTempDir(), "jiagu");
    private static File jiaguZip = new File(workDir, JIAGU_ZIP);

    public enum Event {
        /**
         * 正在反编译
         **/
        DECOMPILEING,
        /**
         * 正在加固
         **/
        ENCRYPTING,
        /**
         * 正在回编译
         **/
        RECOMPILING,
        /**
         * 正在签名
         **/
        SIGNING,
        /**
         * 反编译失败
         **/
        DECOMPILE_FAIL,
        /**
         * 回编译失败
         **/
        RECOMPILE_FAIL,
        /**
         * 加固失败
         **/
        ENCRYPT_FAIL,
        /**
         * 清单文件解析失败
         **/
        MENIFEST_FAIL,
    }

    /**
     * 该apk是否已经加固
     *
     * @param apk
     * @return
     */
    public static boolean isEncrypted(File apk) {
        return ZipHelper.hasFile(apk, "assets/" + JIAGU_DATA_BIN);
    }

    /**
     * apk加固
     *
     * @param apk      apk
     * @param config   keystore配置
     * @param callback 回调，返回码参考FLAG
     * @return 加固后的apk，如果config不会null则返回签名的apk
     */
    public static File encrypt(File apk, KeystoreConfig config, Callback<Event> callback) {

        if (!FileHelper.exists(apk) || isEncrypted(apk)) {
            return null;
        }
        workDir.mkdir();

        // 1.decompile apk
        handleCallback(callback, Event.DECOMPILEING);
        // 反编译目录
        File decompile = new File(workDir, "decompile");
        FileHelper.cleanDirectory(decompile);
        boolean decompileResult = ApkToolPlus.decompile(apk, decompile, new Callback<Exception>() {
            @Override
            public void callback(Exception e) {
                if (callback != null) {
                    callback.callback(Event.DECOMPILE_FAIL);
                }
            }
        });
        if (!decompileResult) {
            return null;
        }

        // 2.加固
        handleCallback(callback, Event.ENCRYPTING);
        //(1).替换jiagu.zip资源
        if (!jiagu(decompile)) {
            handleCallback(callback, Event.ENCRYPT_FAIL);
            return null;
        }
        //(2)加密apk的classes.dex，并拷贝到asset目录
        if (!encryptDex(apk, decompile)) {
            handleCallback(callback, Event.ENCRYPT_FAIL);
            return null;
        }
        //(3)防二次打包，加入签名校验文件
        signatureProtect(apk, decompile);

        //(4)修改Androidmenifest.xml中application配置
        if (!updateMenifest(new File(decompile, "AndroidManifest.xml"))) {
            handleCallback(callback, Event.MENIFEST_FAIL);
            return null;
        }

        // 3.recompile apk
        handleCallback(callback, Event.RECOMPILING);
        File encryptedApk = new File(apk.getParentFile(), FileHelper.getNoSuffixName(apk) + "_encrypted.apk");
        boolean recompileResult = ApkToolPlus.recompile(decompile, encryptedApk, new Callback<Exception>() {
            @Override
            public void callback(Exception e) {
                handleCallback(callback, Event.RECOMPILE_FAIL);
            }
        });
        if (!recompileResult) {
            return null;
        }

        // 4.sign apk
        if (config != null) {
            handleCallback(callback, Event.SIGNING);
            File signedApk = ApkToolPlus.signApk(encryptedApk, config);
            if(FileHelper.exists(signedApk)){
                FileHelper.delete(encryptedApk);
            }
            //FileHelper.cleanDirectory(decompile);
            return signedApk;
        }

        //FileHelper.cleanDirectory(decompile);
        return encryptedApk;
    }

    /**
     * apk签名保护，防止二次打包
     *
     * @param apk
     * @param decompile
     */
    private static void signatureProtect(File apk, File decompile) {
        try(ApkParser parser = new ApkParser(apk)){
            List<CertificateMeta> certList = parser.getCertificateMetaList();
            String certMD5 = certList.get(0).getCertMd5();
            byte[] encryptData = DataProtector.encryptXXTEA(certMD5.getBytes());
            FileUtils.writeByteArrayToFile(new File(decompile,"assets/sign.bin"), encryptData);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleCallback(Callback<Event> callback, Event event) {
        if (callback != null) {
            callback.callback(event);
        }
    }

    /**
     * 修改AndroidMenifest.xml
     *
     * @param menifest
     * @return
     */
    private static boolean updateMenifest(File menifest) {
        XMLWriter writer = null;
        try {
            SAXReader reader = new SAXReader();
            // 读取AndroidManifest.xml
            Document document = reader.read(menifest);
            Element rootElement = document.getRootElement();

            Element applicationElement = rootElement.element("application");
            Attribute appNameAttribute = applicationElement.attribute("name");
            if (appNameAttribute != null) {
                String appName = appNameAttribute.getValue();
                // 修改appName为代理Application
                appNameAttribute.setValue(PROXY_APPLICATION_NAME);
                // 添加meta-data保存原有Application Name
                applicationElement.addElement("meta-data")
                        .addAttribute("android:name", METADATA_SRC_APPLICATION)
                        .addAttribute("android:value", appName);
            } else {
                applicationElement.addAttribute("android:name", PROXY_APPLICATION_NAME);
            }
            // 保存AndroidManifest.xml
            // 创建格式器
            OutputFormat format = OutputFormat.createPrettyPrint();// 整齐的格式
            // OutputFormat format = OutputFormat.createCompactFormat();//紧凑的格式
            format.setEncoding("UTF-8");
            // 获取XML写入//使用字节流，字节写入流会查找format中设置的码表
            writer = new XMLWriter(new FileOutputStream(menifest),
                    format);
            writer.write(document);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 检查dex文件是否存在
     *
     * @return
     */
    private static boolean jiagu(File decompileDir) {
        if (!jiaguZip.exists()) {
            // 释放加固库
            if (!ClassHelper.releaseResourceToFile(JIAGU_ZIP_PATH, jiaguZip)) {
                return false;
            }
        }
        if(FileHelper.exists(jiaguZip)){
            jiaguZip.deleteOnExit();
        }

        File smali = new File(decompileDir, "smali");
        FileHelper.delete(smali);

        // apk是否有lib目录
        File lib = new File(decompileDir, "lib");
        String[] platforms = lib.list();
        boolean isHasLib = lib.exists() && platforms != null && platforms.length > 0;

        ZipHelper.list(jiaguZip, new ZipHelper.FileFilter() {
            @Override
            public void handle(ZipFile zipFile, FileHeader fileHeader) {
                // 1.替换smali目录
                if (fileHeader.getFileName().startsWith("smali")) {
                    if (!ZipHelper.unzip(zipFile, fileHeader, smali.getParentFile())) {
                        Debug.e(fileHeader.getFileName() + " unzip failure from " + zipFile.getFile().getAbsolutePath());
                    }
                    // 2.拷贝lib
                } else if (fileHeader.getFileName().startsWith("libs")) {
                    if (!ZipHelper.unzip(zipFile, fileHeader, decompileDir)) {
                        Debug.e(fileHeader.getFileName() + " unzip failure from " + zipFile.getFile().getAbsolutePath());
                    }
                }
            }
        });

        File libs = new File(decompileDir, "libs");
        if (isHasLib) {
            for (String platform : platforms) {
                File libFile = new File(libs, platform + "/libapktoolplus_jiagu.so");
                if (libFile.exists()) {
                    FileHelper.move(libFile, new File(lib, platform + "/" + libFile.getName()));
                }
            }
        } else {
            // 如果没有lib，则拷贝所有平台lib到lib目录
            FileHelper.move(libs, lib);
        }
        FileHelper.delete(libs);

        return true;
    }

    /**
     * 加密dex
     *
     * @param decompileDir
     * @return
     */
    private static boolean encryptDex(File apk, File decompileDir) {

        File dexFile = new File(decompileDir, "classes.dex");
        if (dexFile.exists()) {
            dexFile.delete();
        }

        try {
            // 解压apk中的classes.dex
            ZipFile zipFile = new ZipFile(apk);
            ZipHelper.unzip(zipFile, "classes.dex", dexFile.getParentFile());
        } catch (ZipException e) {
            e.printStackTrace();
            return false;
        }

        File assets = new File(decompileDir, "assets");
        assets.mkdirs();

        File encryptFile = new File(assets, JIAGU_DATA_BIN);
        encryptFile.delete();

        DataProtector.encrypt(dexFile, encryptFile);
        dexFile.delete();

        return true;
    }

}
