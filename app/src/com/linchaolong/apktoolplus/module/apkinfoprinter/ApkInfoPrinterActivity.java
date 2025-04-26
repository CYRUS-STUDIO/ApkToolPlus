package com.linchaolong.apktoolplus.module.apkinfoprinter;

import axmlprinter.AXMLPrinter;
import com.linchaolong.apktoolplus.utils.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.ui.DialogPlus;
import com.linchaolong.apktoolplus.ui.FileSelecter;
import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.CertificateMeta;
import net.dongliu.apk.parser.exception.ParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.zip.ZipFile;

/**
 *
 */
public class ApkInfoPrinterActivity extends Activity{

    public static final String TAG = ApkInfoPrinterActivity.class.getSimpleName();

    @FXML
    TextArea textAreaContent;
    @FXML
    Button btnOpen;
    @FXML
    Label labelFilePath;
    @FXML
    Button btnClose;

    // xml格式化工具
    private XmlFormatter xmlFormatter = new XmlFormatter();

    /**
     * 浏览Apk或AndroidMenifest.xml
     */
    public void printApk(){
        File lastDir = Config.getDir(Config.kLastOpenApkInfoDir);
        File file = FileSelecter.create(btnOpen.getScene().getWindow())
                .addFilter("apk","xml")
                .setTitle("选择apk或者AndroidManifest.xml")
                .setInitDir(lastDir)
                .showDialog();
        if (file!=null && file.exists()){
            showManifest(file);
        }
    }

    private String buildMetaDataTree(ApkMeta apkMeta){
        StringBuilder treeBuilder = new StringBuilder();
        treeBuilder.append("packageName : ").append(apkMeta.getPackageName()).append("\n");
        treeBuilder.append("label : ").append(apkMeta.getLabel()).append("\n");
        treeBuilder.append("icon : ").append(apkMeta.getIcon()).append("\n");
        treeBuilder.append("versionName : ").append(apkMeta.getVersionName()).append("\n");
        treeBuilder.append("versionCode : ").append(apkMeta.getVersionCode()).append("\n");
        treeBuilder.append("minSdkVersion : ").append(apkMeta.getMinSdkVersion()).append("\n");
        treeBuilder.append("targetSdkVersion : ").append(apkMeta.getTargetSdkVersion()).append("\n");
        treeBuilder.append("maxSdkVersion : ").append(apkMeta.getMaxSdkVersion()).append("\n");
        return treeBuilder.toString();
    }

    private void showManifest(File file){
        // 记录最后打开的目录
        Config.set(Config.kLastOpenApkInfoDir,file.getParentFile().getPath());

        StringBuilder manifestData;
        StringBuilder apkParserData = new StringBuilder();

        // 如果是apk文件
        if(FileHelper.isSuffix(file,"apk")){

            try(ApkParser parser = new ApkParser(file)){
                // Meta Data
//                apkParserData.append("#Meta Data\n").append(parser.getApkMeta()).append("\n\n");
                apkParserData.append("#Meta Data\n").append(buildMetaDataTree(parser.getApkMeta())).append("\n");

                // 签名解析
                StringBuilder signData = new StringBuilder();
                signData.append("#签名信息\n");
                List<CertificateMeta> certList = parser.getCertificateMetaList();
                for (CertificateMeta certificateMeta : certList) {
                    signData.append(certificateMeta.toString());
                }
                signData.append("\n\n");
                apkParserData.append(signData);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserException e) {
                LogUtils.e("apk Meta Data 或 签名信息 解析失败:" + e);
            }

            ZipFile apkFile;
            try {
                apkFile = new ZipFile(file);
                InputStream in = ZipUtils.getEntryInputStream(apkFile, "AndroidManifest.xml");
                manifestData = AXMLPrinter.decode(in);
                IOUtils.close(in);
                IOUtils.close(apkFile);
            } catch (IOException e) {
                LogUtils.e("文件解析失败:"+file.getPath());
                e.printStackTrace();
                DialogPlus.alert("错误提示","","文件解析失败:"+file.getPath());
                return;
            }
        }else{
            // 解析 AndroidMenifest.xml
            manifestData = AXMLPrinter.decode(file);
        }

        textAreaContent.setText(apkParserData.toString());
        textAreaContent.appendText(xmlFormatter.format(manifestData.toString()));

        // 显示文件路径
        labelFilePath.setText(file.getPath());
    }

    public void onDragOverLinkFile(DragEvent event){
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.LINK);
        } else {
            event.consume();
        }
    }

    public void onDragDroppedHandleFiles(DragEvent event){
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            for (File file : db.getFiles()) {
                showManifest(file);
                break;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * 关闭窗口
     */
    public void close(){
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    // 初始化方法
    @FXML
    public void initialize() {
    }
}
