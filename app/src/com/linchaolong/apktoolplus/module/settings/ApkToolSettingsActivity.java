package com.linchaolong.apktoolplus.module.settings;

import com.linchaolong.apktoolplus.utils.CmdUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.ui.FileSelecter;
import com.linchaolong.apktoolplus.utils.ViewUtils;
import com.linchaolong.apktoolplus.utils.Base64Utils;
import com.linchaolong.apktoolplus.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by linchaolong on 2015/9/19.
 */
public class ApkToolSettingsActivity extends Activity implements Initializable {

    public static final String TAG = ApkToolSettingsActivity.class.getSimpleName();

    @FXML
    TextField textFieldFilePath;
    @FXML
    Button btnSelect;
    @FXML
    TextField textFieldAlias;
    @FXML
    TextField textFieldAliasPassword;
    @FXML
    TextField textFieldKeystorePassword;
    @FXML
    TextArea textArea;

    /**
     * 选择keystore文件
     */
    public void selectKeystore(){
        File lastDir = Config.getDir(Config.kKeystoreFilePath);
        File keytoreFile = FileSelecter.create(btnSelect.getParent().getScene().getWindow())
                .addFilter("keystore","jks")
                .addFilter("*")
                .setInitDir(lastDir)
                .setTitle("选择keystore文件")
                .showDialog();
        if(keytoreFile != null){
            textFieldFilePath.setText(keytoreFile.getPath());
            Config.set(Config.kKeystoreFilePath,keytoreFile.getPath());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 恢复数据
        initData();
        // 监听输入
        ViewUtils.listenerInputAndSave(textFieldAlias,Config.kKeystoreAlias);
        ViewUtils.listenerInputAndSave(textFieldAliasPassword,Config.kAliasPassword, true);
        ViewUtils.listenerInputAndSave(textFieldKeystorePassword,Config.kKeystorePassword, true);
    }

    /**
     * 视图数据显示恢复
     */
    private void initData() {
        // keystore文件
        String keystorePath = ViewUtils.review(textFieldFilePath,Config.kKeystoreFilePath);
        // keystore password
        String keystorePassword = ViewUtils.review(textFieldKeystorePassword,Config.kKeystorePassword);
        // alias
        ViewUtils.review(textFieldAlias,Config.kKeystoreAlias);
        // alias password
        ViewUtils.review(textFieldAliasPassword,Config.kAliasPassword);
        // 显示keystore信息列表
        showKeystoreList(keystorePath, keystorePassword);
    }

    /**
     * 显示 keystore 信息列表
     *
     * @param keystorePath
     * @param keystorePassword
     */
    private void showKeystoreList(String keystorePath, String keystorePassword){
        if(StringUtils.isEmpty(keystorePassword) || StringUtils.isEmpty(keystorePassword)){
            return;
        }
        keystorePassword = Base64Utils.decode(keystorePassword);
        //keytool -list -v -keystore <keystore-path> -storepass <keystore-password>
        /*
         -certreq            生成证书请求
         -changealias        更改条目的别名
         -delete             删除条目
         -exportcert         导出证书
         -genkeypair         生成密钥对
         -genseckey          生成密钥
         -gencert            根据证书请求生成证书
         -importcert         导入证书或证书链
         -importpass         导入口令
         -importkeystore     从其他密钥库导入一个或所有条目
         -keypasswd          更改条目的密钥口令
         -list               列出密钥库中的条目
         -printcert          打印证书内容
         -printcertreq       打印证书请求的内容
         -printcrl           打印 CRL 文件的内容
         -storepasswd        更改密钥库的存储口令
         */
        String cmd = "keytool -list -v -keystore " + keystorePath + " -storepass " + keystorePassword;
        String output = CmdUtils.execAndGetOutput(cmd);
        textArea.setText(output);
    }
}
