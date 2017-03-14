package com.linchaolong.apktoolplus;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.linchaolong.apktoolplus.core.AppManager;
import com.linchaolong.apktoolplus.core.Global;
import com.linchaolong.apktoolplus.module.main.MainActivity;
import com.linchaolong.apktoolplus.utils.ClassUtils;
import com.linchaolong.apktoolplus.utils.ViewUtils;

public class Main extends Application {

    public static final String TAG = Main.class.getSimpleName();

    @Override
    public void start(Stage stage) throws Exception{
        // 设置应用图标
        ViewUtils.setWindowIcon(stage, ClassUtils.getResourceAsURL("res/white_icon/white_icon_Plus.png"));
        // 无边框
        ViewUtils.setNoBroder(stage);
        // 设置标题
        stage.setTitle("ApkToolPlus");
        // 背景透明
        stage.initStyle(StageStyle.TRANSPARENT);
        // 设置透明度
        //stage.setOpacity(0.8);
        // 大小不可变
        stage.setResizable(false);

        // main ui
        StackPane root = new StackPane();
        AnchorPane layout = FXMLLoader.load(MainActivity.class.getResource("main.fxml"));
        layout.setBackground(Background.EMPTY);
        root.getChildren().add(layout);

        // 设置根节点
        Global.setRoot(root);

        Scene scene = new Scene(root, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT, Color.TRANSPARENT);
        stage.setScene(scene);

        // test
//        Loading loading = new Loading(ClassUtils.getResourceAsURL("res/gif/loading.gif"));
//        loading.setMessage("正在加载,请稍候...");
//        root.getChildren().add(loading);
//        loading.lauchTimeoutTimer(2000);

        // 在屏幕中间
        stage.centerOnScreen();

        // 设置拖拽事件
        ViewUtils.registerDragEvent(stage,root);

        stage.show();

        // 恢复上次打开页面
        Integer lastPageIndex = Integer.parseInt(Config.get(Config.kLastPageIndex, "0"));
        MainActivity.getInstance().pages.setCurrentPageIndex(lastPageIndex);
    }


    // 初始化方法
    @FXML
    public void initialize() {
    }

    public static void main(String[] args) {
        // 初始化
        AppManager.init();
        // 启动应用
        launch(args);
    }
}
