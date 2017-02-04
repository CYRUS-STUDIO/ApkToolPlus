package com.linchaolong.apktoolplus.module.jiagu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import com.linchaolong.apktoolplus.core.Callback;
import com.linchaolong.apktoolplus.utils.FileHelper;

import java.io.File;

/**
 * Created by linchaolong on 2015/11/27.
 */
public class ApkItemView {

    public static final String TAG = ApkItemView.class.getSimpleName();

    @FXML
    Label label;
    @FXML
    ProgressBar progressBar;
    @FXML
    Button btnRemove;
    @FXML
    Button btnOpenDir;
    @FXML
    Text textState;

    private File apk;
    private Parent node;

    private int FINISHED = 1;
    private int WAITING = 2;
    private int ENCRYPTING = 3;
    private int SIGNING = 4;
    private int state = WAITING;

    public ApkItemView(File apk, Parent node) {
        this.apk = apk;
        this.node = node;
    }

    public File getApk() {
        return apk;
    }
    public Parent getNode() {
        return node;
    }

    public boolean isFinished() {
        return state == FINISHED;
    }

    public void setFinished() {
        state = FINISHED;
        Platform.runLater(() -> {
            textState.setText("加固完成");
            btnRemove.setVisible(true);
            textState.getStyleClass().removeAll();
            textState.getStyleClass().add("text_apk_state_finished");
            if(onFinish != null){
                onFinish.callback(null);
            }
        });
    }

    private Callback onFinish;
    public void setOnFinish(Callback callback){
        onFinish = callback;
    }

    public boolean isWaiting() {
        return state == WAITING;
    }

    public void setWaiting() {
        state = WAITING;
        Platform.runLater(() -> {
            textState.setText("等待中");
            btnRemove.setVisible(true);
            textState.getStyleClass().removeAll();
            textState.getStyleClass().add("text_apk_state_waiting");
        });
    }

    public boolean isEncrypting() {
        return state == ENCRYPTING;
    }

    public void setEncrypting() {
        state = ENCRYPTING;
        Platform.runLater(() -> {
            textState.setText("正在加固..");
            btnRemove.setVisible(false);
            textState.getStyleClass().removeAll();
            textState.getStyleClass().add("text_apk_state_encrypting");
        });
    }

    public void setSigning() {
        if(state == FINISHED){
            return;
        }
        state = SIGNING;
        Platform.runLater(() -> {
            textState.setText("正在签名..");
            btnRemove.setVisible(false);
            textState.getStyleClass().removeAll();
            textState.getStyleClass().add("text_apk_state_encrypting");
        });
    }

    public void initialize() {
        label.setText(apk.getName());
        progressBar.setProgress(0);
        btnRemove.setTooltip(new Tooltip(apk.getAbsolutePath()));
        btnOpenDir.setTooltip(new Tooltip(apk.getParent()));
        btnOpenDir.setOnAction(event -> FileHelper.showInExplorer(apk));
        setWaiting();
    }

}
