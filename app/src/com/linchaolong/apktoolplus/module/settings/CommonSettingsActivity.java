package com.linchaolong.apktoolplus.module.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.ui.DirectorySelecter;
import com.linchaolong.apktoolplus.ui.FileSelecter;
import com.linchaolong.apktoolplus.utils.UIHelper;
import com.linchaolong.apktoolplus.utils.FileHelper;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by linchaolong on 2016/3/29.
 */
public class CommonSettingsActivity extends Activity implements Initializable{

    public static final String TAG = CommonSettingsActivity.class.getSimpleName();

    @FXML
    TextField textFieldSublimePath;
    @FXML
    Button btnSublimeSelect;
    @FXML
    TextField textFieldAppOutPath;
    @FXML
    TextField textFieldCmdParams;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 恢复数据
        review();
        // 监听输入
        UIHelper.listenerInputAndSave(textFieldCmdParams,Config.kSublimeCmdParams);
    }

    public void selectSublime(){
        File lastDir = Config.getDir(Config.kSublimePath);
        File sublimeFile = FileSelecter.create(btnSublimeSelect.getParent().getScene().getWindow())
                .addFilter("exe")
                .addFilter("*")
                .setInitDir(lastDir)
                .setTitle("请选择sublime的启动程序文件")
                .showDialog();
        if(sublimeFile != null){
            textFieldSublimePath.setText(sublimeFile.getPath());
            Config.set(Config.kSublimePath,sublimeFile.getPath());
        }
    }

    /**
     * 选择EasySDK文件输出目录
     */
    public void selectAppOut(){
        File lastDir = Config.getDir(Config.kAppOutputDir);
        File dir = DirectorySelecter.create(btnSublimeSelect.getParent().getScene().getWindow())
                .setInitDir(lastDir)
                .setTitle("请选择输出目录")
                .showDialog();
        if(FileHelper.exists(dir)){
            textFieldAppOutPath.setText(dir.getPath());
            Config.set(Config.kAppOutputDir,dir.getPath());
        }
    }

    /**
     * 视图数据显示恢复
     */
    private void review() {
        // sublime文件
        UIHelper.review(textFieldSublimePath,Config.kSublimePath);
        // 自定义命令参数
        UIHelper.review(textFieldCmdParams,Config.kSublimeCmdParams);
        // ApkToolPlus文件输出目录
        UIHelper.review(textFieldAppOutPath,Config.kAppOutputDir);
    }
}
