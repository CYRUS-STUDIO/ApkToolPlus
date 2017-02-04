package com.linchaolong.apktoolplus.module.debug;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.core.debug.LogManager;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.core.AppManager;
import com.linchaolong.apktoolplus.utils.Debug;

/**
 * Created by linchaolong on 2015/9/8.
 */
public class DebugActivity extends Activity{

    public static final String TAG = DebugActivity.class.getSimpleName();

    @FXML
    TextArea output;

    @FXML
    Button btnClose;
    @FXML
    Button btnOpenLogLocation;
    @FXML
    Button btnClear;
    @FXML
    CheckBox checkboxOutputLog;
    @FXML
    ChoiceBox choiceBoxLogLevel;

    private static TextArea outputRef;

    /**
     * 打印日志
     *
     * @param log
     */
    public static void print(String log){
        if(outputRef != null){
            Platform.runLater(() -> {
                outputRef.appendText(log);
//                ScrollPane scrollPane = (ScrollPane) outputRef.lookup(".scroll-pane");
//                if(scrollPane.vvalueProperty().get() >= 0.9){
//                    outputRef.end();
//                }
            });
        }
    }

    /**
     * 关闭窗口
     */
    public void close(){
        btnClose.getScene().getWindow().hide();
    }

    /**
     * 打开日志文件输出目录
     */
    public void openLogLocation(){
        AppManager.browser(AppManager.getLogDir());
    }

    /**
     * 清除日志
     */
    public void clear(){
        output.setText("");
    }

    // 初始化方法
    @FXML
    public void initialize() {
        output.setEditable(false);
        outputRef = output;
        // 恢复设置
        checkboxOutputLog.setSelected(LogManager.getInstance().isLogFileOutput());
        // 设置日志文件输出切换监听
        checkboxOutputLog.selectedProperty().addListener((observable, oldValue, newValue) -> {
            LogManager.getInstance().setIsLogFileOutput(newValue);
            // 更新配置
            Config.set(Config.kIsLogOutputFile, newValue);
        });

        // log level
        choiceBoxLogLevel.setItems(
                FXCollections.observableArrayList("VERBOSE", "INFO ", "DEBUG", "WARN", "ERROR")
        );
        choiceBoxLogLevel.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Config.set(Config.kLogLevel,newValue);
            Debug.setLogLevel((Integer) newValue);
        });
        choiceBoxLogLevel.setTooltip(new Tooltip("设置日志输出级别"));
        // 恢复默认设置
        choiceBoxLogLevel.getSelectionModel().select(Config.getInt(Config.kLogLevel, Debug.DEBUG));
    }
}
