package com.linchaolong.apktoolplus.core;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.ui.Loading;
import com.linchaolong.apktoolplus.ui.Popup;
import com.linchaolong.apktoolplus.ui.Toast;

/**
 * 全局类
 *
 * Created by linchaolong on 2015/10/20.
 */
public class Global {

    private static StackPane mRoot;
    private static Toast mToast;
    private static Loading mLoading;

    /**
     * 设置根节点
     *
     * @param root
     */
    public static void setRoot(StackPane root){
        Global.mRoot = root;
    }

    public static StackPane getRoot(){
        return Global.mRoot;
    }

    /**
     * 显示一个全局loading
     */
    public static void showLoading(){
        if(mLoading != null){
            return;
        }
        Platform.runLater(() -> {
            mLoading = new Loading(Config.DEFAULT_LOADING_IMAGE);
            mLoading.setMessage("loading...");
            StackPane.setAlignment(mLoading, Pos.CENTER);
            mRoot.getChildren().add(mLoading);
        });
    }

    /**
     * 隐藏全局loading
     */
    public static void hideLoading(){
        Platform.runLater(() -> {
            if(mLoading != null){
                mRoot.getChildren().remove(mLoading);
                mLoading = null;
            }
        });
    }

    /**
     * Toast
     *
     * @param msg
     */
    public static void toast(final String msg){
        Platform.runLater(() -> {
            if(mToast != null){
                mToast.remove();
            }
            mToast = Toast.make(msg).show(mRoot);
        });
    }

    /**
     * popup一条消息
     *
     * @param msg
     */
    public static void popup(final String msg){
        Platform.runLater(() -> Popup.create().setMessage(msg).setStyle(Popup.STYLE_MODAL).show(mRoot));
    }

    /**
     * 对话框
     *
     * @param msg
     * @param confirmCallback
     * @param cancelCallback
     */
    public static void dialog(final String msg, Callback<Integer> confirmCallback, Callback<Integer> cancelCallback){
        Platform.runLater(() -> Popup.create()
                .setMessage(msg)
                .setStyle(Popup.STYLE_CONFIRM_CANCEL)
                .setConfirmCallback(confirmCallback)
                .setCancelCallback(cancelCallback)
                .show(mRoot));
    }
}
