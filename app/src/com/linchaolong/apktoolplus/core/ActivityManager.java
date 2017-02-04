package com.linchaolong.apktoolplus.core;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

/**
 * Created by linchaolong on 2016/3/28.
 */
public class ActivityManager {

    public static StackPane getRootView(){
        return Global.getRoot();
    }

    /**
     * 启动一个Activity
     *
     * @param url   界面布局文件url
     * @param pos   界面位置
     */
    public static void startActivity(URL url, Pos pos){
        try {
            Parent view = FXMLLoader.load(url);
            StackPane.setAlignment(view, pos);
            getRootView().getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动一个Activity
     *
     * @param url       界面布局文件url
     * @param pos       界面位置
     * @param animation 切换动画
     */
    public static void startActivity(URL url, Pos pos, FadeTransition animation){
        try {
            Parent view = FXMLLoader.load(url);
            StackPane.setAlignment(view, pos);
            getRootView().getChildren().add(view);
            animation.setNode(view);
            animation.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
