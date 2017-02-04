package com.linchaolong.apktoolplus.module.jiagu;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.core.Callback;
import com.linchaolong.apktoolplus.core.KeystoreConfig;
import com.linchaolong.apktoolplus.core.SettingHelper;
import com.linchaolong.apktoolplus.core.jiagu.JiaGu;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.ui.FileSelecter;
import com.linchaolong.apktoolplus.utils.UIHelper;
import com.linchaolong.apktoolplus.utils.Debug;
import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.TaskHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class JiaGuActivity extends Activity{

    public static final String TAG = JiaGuActivity.class.getSimpleName();

    @FXML
    ListView listViewApkList;
    @FXML
    Button btnAddApk;
    @FXML
    Button btnStartEncrypt;

    private boolean isWorking = false;

    public void addItem(File apk){
        if(!FileHelper.exists(apk)){
            return;
        }
        if(JiaGu.isEncrypted(apk)){
            showToast(apk.getName()+"已经加固");
            return;
        }
        // create item view and add to listview
        try {
            Parent node = FXMLLoader.load(JiaGuActivity.class.getResource("jiagu_item.fxml"));
            ApkItemView item = new ApkItemView(apk,node);
            UIHelper.setController(node,item);
            node.setUserData(item);
            // 移除监听
            item.btnRemove.setOnAction(event -> {
                if(!item.isEncrypting()){
                    listViewApkList.getItems().remove(item.getNode());
                }
            });
            listViewApkList.getItems().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItems(List<File> list){
        list.forEach(this::addItem);
    }

    @FXML
    public void addApk(){
        File lastDir = Config.getDir(Config.kLastJiaGuAddApkDir);
        List<File> fileList = FileSelecter.create(btnAddApk.getScene().getWindow())
                .setTitle("添加apk（可多选）")
                .addFilter("apk")
                .setInitDir(lastDir)
                .showMultiDialog();

        if(fileList != null && !fileList.isEmpty()){
            Config.set(Config.kLastJiaGuAddApkDir,fileList.get(0).getParent());
            addItems(fileList);
        }
    }
    @FXML
    public void clear(){
        List<Object> deleteList = new ArrayList<>();
        listViewApkList.getItems().forEach((item)->{
            Parent node = (Parent) item;
            ApkItemView controller = (ApkItemView) node.getUserData();
            if(!controller.isEncrypting()){
                deleteList.add(item);
            }
        });
        listViewApkList.getItems().removeAll(deleteList);
    }
    @FXML
    public void clearFinish(){
        List<Object> deleteList = new ArrayList<>();
        listViewApkList.getItems().forEach((item)->{
            Parent node = (Parent) item;
            ApkItemView controller = (ApkItemView) node.getUserData();
            if(controller.isFinished()){
                deleteList.add(item);
            }
        });
        listViewApkList.getItems().removeAll(deleteList);
    }

    private Timeline progressAnimation;
    @FXML
    public void startEncrypt(){
        if(isWorking){
            showToast("正在加固apk，请稍候...");
            return ;
        }

        isWorking = true;
        btnAddApk.setDisable(true);
        btnStartEncrypt.setDisable(true);

        // 迭代apk列表，加固apk
        iteratorEncryptApkList(listViewApkList.getItems().iterator());
    }

    private void iteratorEncryptApkList(Iterator<Parent> iterator){

        if(!iterator.hasNext()){
            Platform.runLater(() -> {
                btnAddApk.setDisable(false);
                btnStartEncrypt.setDisable(false);
                isWorking = false;
            });
            return;
        }

        Parent node = iterator.next();
        ApkItemView apkItemView = (ApkItemView) node.getUserData();
        KeystoreConfig keystoreConfig = SettingHelper.getKeystoreConfig();

        if(!apkItemView.isWaiting()){
            iteratorEncryptApkList(iterator);
            return;
        }

        // 进度动画
        progressAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(apkItemView.progressBar.progressProperty(), 0)
                ),
                /** 正在反编译 **/
                new KeyFrame(Duration.seconds(8),
                        event -> progressAnimation.pause(),
                        new KeyValue(apkItemView.progressBar.progressProperty(), 0.6)),
                /** 正在加固 **/
                new KeyFrame(Duration.seconds(10),
                        event -> progressAnimation.pause(),
                        new KeyValue(apkItemView.progressBar.progressProperty(),0.75)),
                /** 正在回编译 **/
                new KeyFrame(Duration.seconds(13),
                        event -> {
                            if(keystoreConfig != null){
                                progressAnimation.pause();
                            }
                        },
                        new KeyValue(apkItemView.progressBar.progressProperty(),1)),
                /** 正在签名 **/
                new KeyFrame(Duration.seconds(15),
                        event -> {
                          apkItemView.setSigning();
                        },
                        new KeyValue(apkItemView.progressBar.progressProperty(),1))
        );
        progressAnimation.setAutoReverse(false);
        // 指定从哪里开始播放
        //progressAnimation.playFrom(Duration.millis(1000));
        // 从开头播放
        //progressAnimation.playFromStart();
        apkItemView.setEncrypting();
        TaskHandler.get().queue(() -> {
            encryptApk(apkItemView.getApk(), keystoreConfig);
            apkItemView.setFinished();
            // 加固下一个
            iteratorEncryptApkList(iterator);
        });
    }

    private File encryptApk(File apk, KeystoreConfig keystoreConfig){

        return JiaGu.encrypt(apk, keystoreConfig, new Callback<JiaGu.Event>() {
            @Override
            public void callback(JiaGu.Event event) {
                switch (event){
                    /** 正在反编译 **/
                    case DECOMPILEING:
                        Debug.d("正在反编译");
                        Platform.runLater(() -> progressAnimation.playFrom(Duration.ZERO));
                        break;
                    /** 正在加固 **/
                    case ENCRYPTING:
                        Debug.d("正在加固");
                        Platform.runLater(() -> progressAnimation.playFrom(Duration.seconds(8.1)));
                        break;
                    /** 正在回编译 **/
                    case RECOMPILING:
                        Debug.d("正在回编译");
                        Platform.runLater(() -> progressAnimation.playFrom(Duration.seconds(10.1)));
                        break;
                    /** 正在签名 **/
                    case SIGNING:
                        Debug.d("正在签名");
                        Platform.runLater(() -> progressAnimation.playFrom(Duration.seconds(13.1)));
                        break;
                    /** 反编译失败 **/
                    case DECOMPILE_FAIL:
                        Debug.e("反编译失败");
                        break;
                    /** 回编译失败 **/
                    case RECOMPILE_FAIL:
                        Debug.e("回编译失败");
                        break;
                    /** 加固失败 **/
                    case ENCRYPT_FAIL:
                        Debug.e("加固失败");
                        break;
                    /** 清单文件解析失败 **/
                    case MENIFEST_FAIL:
                        Debug.e("清单文件解析失败");
                        break;
                }
            }
        });
    }

    // 初始化方法
    @FXML
    public void initialize() {
    }
}
