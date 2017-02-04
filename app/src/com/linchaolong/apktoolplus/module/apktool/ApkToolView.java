package com.linchaolong.apktoolplus.module.apktool;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import com.linchaolong.apktoolplus.base.Activity;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by linchaolong on 2016/4/5.
 */
public class ApkToolView extends Activity implements Initializable {

    public static final String TAG = ApkToolView.class.getSimpleName();

    //////////////////////////// ApkTool //////////////////////////////
    @FXML
    ToggleButton toggleDecompile;
    @FXML
    ToggleButton toggleRecompile;
    @FXML
    ToggleButton toggleApkSign;

    //////////////////////////// 反编译 //////////////////////////////
    @FXML
    StackPane paneDecompile;
    @FXML
    Button btnSelectDecompileApk;
    @FXML
    Button btnDecompile;
    @FXML
    TextField textFieldDecompileApkList;
    @FXML
    Button btnOpenDecompileOut;

    //////////////////////////// 回编译 //////////////////////////////
    @FXML
    StackPane paneRecompile;
    @FXML
    Button btnSelectRecompileApk;
    @FXML
    Button btnRecompile;
    @FXML
    TextField textFieldRecompileApkDir;
    @FXML
    Button btnOpenRecompileOut;


    //////////////////////////// apk签名 //////////////////////////////
    @FXML
    StackPane paneApkSign;
    @FXML
    Button btnSelectSignApk;
    @FXML
    Button btnApkSign;
    @FXML
    TextField textFieldSignApkList;
    @FXML
    Button btnOpenApkSignOut;

    /**
     * 页面切换
     *
     * @param flag
     */
    protected void togglePage(Integer flag){
        paneDecompile.setVisible(false);
        paneRecompile.setVisible(false);
        paneApkSign.setVisible(false);
        switch(flag){
            // 反编译
            case 1:
                paneDecompile.setVisible(true);
                break;
            // 回编译
            case 2:
                paneRecompile.setVisible(true);
                break;
            // apk签名
            case 3:
                paneApkSign.setVisible(true);
                break;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化toggle button
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Integer flag = (Integer) newValue.getUserData();
                togglePage(flag);
            } else {
                toggleGroup.selectToggle(oldValue);
            }
        });
        toggleDecompile.setToggleGroup(toggleGroup);
        toggleDecompile.setUserData(1);
        toggleRecompile.setToggleGroup(toggleGroup);
        toggleRecompile.setUserData(2);
        toggleApkSign.setToggleGroup(toggleGroup);
        toggleApkSign.setUserData(3);

        // 默认选择反编译界面
        toggleGroup.selectToggle(toggleDecompile);

        // 默认不显示打开输出目录按钮
        btnOpenDecompileOut.setVisible(false);
        btnOpenRecompileOut.setVisible(false);
        btnOpenApkSignOut.setVisible(false);
    }

}
