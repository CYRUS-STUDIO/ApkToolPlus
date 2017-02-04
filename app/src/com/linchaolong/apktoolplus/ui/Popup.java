package com.linchaolong.apktoolplus.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import com.linchaolong.apktoolplus.core.Callback;
import com.linchaolong.apktoolplus.utils.Debug;

/**
 *  popup ui
 *
 * 注意：本节点的父类必须是Pane或其派生类，当点击取消按钮时会把当前节点从父节点中移除。
 *
 * Created by linchaolong on 2015/9/15.
 */
public class Popup extends StackPane {

    public static final String TAG = Popup.class.getSimpleName();

    private Text text;
    private VBox contentBox;

    private Callback<Integer> confirmCallback;
    private Callback<Integer> cancelCallback;


    // 样式
    /** 模态对话框 **/
    public static final int STYLE_MODAL = 0;
    /** 确认对话框 **/
    public static final int STYLE_CONFIRM = 1;
    /** 常规对话框 **/
    public static final int STYLE_CONFIRM_CANCEL = 2;
    // 默认模态对话框
    private int style = STYLE_MODAL;

    private Popup() {
        super();
        // 设置背景颜色
        setBackgroundColor(20,20,20,0.5f);
        contentBox = new VBox();

        // 间隔
        Region topRegion = new Region();
        topRegion.setPrefHeight(30);
        contentBox.getChildren().add(topRegion);

        // 提示信息
        text = new Text();
        text.setStyle("-fx-fill: white; -fx-font-size:24;-fx-font-family: \"Microsoft YaHei\";-fx-text-alignment: CENTER;");
        contentBox.getChildren().add(text);

        // 设置内容居中
        contentBox.alignmentProperty().set(Pos.CENTER);
        // 设置布局居中
        setAlignment(contentBox,Pos.CENTER);
        getChildren().add(contentBox);
    }

    /**
     * 设置确认回调
     *
     * @param callback
     */
    public Popup setConfirmCallback(Callback callback){
        confirmCallback = callback;
        return this;
    }

    /**
     * 设置取消回调
     *
     * @param callback
     */
    public Popup setCancelCallback(Callback callback){
        cancelCallback = callback;
        return this;
    }

    /**
     *   初始化按钮。
     */
    private void initBtn(){
        if(style == STYLE_MODAL){
            return;
        }

        if(style == STYLE_CONFIRM || style == STYLE_CONFIRM_CANCEL){
            // 上间隔
            Region topRegion = new Region();
            topRegion.setPrefHeight(50);
            contentBox.getChildren().add(topRegion);

            HBox hBox = new HBox();
            // 确认按钮
            Button btnConfirm = makeButton("确认", event -> {
                Debug.d( "click btnConfirm");
                if(confirmCallback != null){
                    confirmCallback.callback(1);;
                }
                remove();
            });
            hBox.getChildren().add(btnConfirm);

            if(style == STYLE_CONFIRM_CANCEL){
                // 中间间隔
                Region centerRegion = new Region();
                centerRegion.setPrefWidth(50);
                hBox.getChildren().add(centerRegion);
                // 取消按钮
                Button btnCancel = makeButton("取消", event -> {
                    Debug.d( "click btnCancel");
                    if(cancelCallback != null){
                        cancelCallback.callback(1);
                    }
                    remove();
                });
                hBox.getChildren().add(btnCancel);
            }
            hBox.alignmentProperty().set(Pos.CENTER);
            contentBox.getChildren().add(hBox);

            // 下间隔
            Region bottomRegion = new Region();
            bottomRegion.setPrefHeight(20);
            contentBox.getChildren().add(bottomRegion);
        }
    }

    public void remove(){
        Parent parent = Popup.this.getParent();
        if (parent == null) {
            Debug.w( "remove popup failure because parent is null");
            return;
        }
        Pane pane = (Pane) parent;
        pane.getChildren().remove(Popup.this);
    }

    public Button makeButton(String text, EventHandler<ActionEvent> handler){
        Button button = new Button();
        button.getStylesheets().add("css/common.css");
        button.getStyleClass().add("button_line_white");
        button.setStyle("-fx-font: 16px \"Microsoft YaHei\";");
        button.setText(text);
        button.setOnAction(handler);
        return button;
    }

    /**
     * 创建一个popup ui
     *
     * @return
     */
    public static Popup create(){
        return new Popup();
    }

    /**
     * 设置背景颜色
     *
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public Popup setBackgroundColor(int r, int g, int b, float a){
        StringBuilder colorBuilder = new StringBuilder("-fx-background-color: rgba(");
        colorBuilder.append(r).append(",").append(g).append(",").append(b).append(",").append(a).append(")");
        setStyle(colorBuilder.toString());
        return this;
    }

    /**
     * 设置提示信息
     * @param msg
     */
    public Popup setMessage(String msg){
        text.setText(msg);
        return this;
    }

    public Popup setStyle(int style){
        this.style = style;
        return this;
    }

    /**
     *  显示popup ui
     */
    public void show(StackPane parent){
        if(parent == null){
            Debug.e("parent is nll , can't show popup ui.");
            return;
        }

        // 模态对话框
        if(style == STYLE_MODAL){
            setOnMouseReleased(event -> {
                remove();
            });
        }
        // 初始化按钮
        initBtn();
        parent.getChildren().add(this);
    }

}
