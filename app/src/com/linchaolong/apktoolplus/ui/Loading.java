package com.linchaolong.apktoolplus.ui;

import com.linchaolong.apktoolplus.utils.LogUtils;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 *   // 调用示例
     Loading loading = new Loading(Config.DEFAULT_LOADING_IMAGE);
     loading.setMessage("正在加载,请稍候...");
     root.getChildren().add(loading);
     loading.lauchTimeoutTimer(2000);
 *
 * Created by Administrator on 2015/9/15.
 */
public class Loading extends StackPane {

    public static final String TAG = Loading.class.getSimpleName();

    private ImageView imageView;
    private Text text;
    private VBox contentBox;
    private Button btnCancel;

    public Loading(URL imageUrl) {
        super();
        // 设置背景颜色
        setBackgroundColor(20,20,20,0.5f);
        contentBox = new VBox();

        // 间隔
        Region topRegion = new Region();
        topRegion.setPrefHeight(20);
        contentBox.getChildren().add(topRegion);

        // gif动态图
        imageView = new ImageView();
        imageView.setSmooth(true);
        setImage(imageUrl);
        contentBox.getChildren().add(imageView);

        // 间隔
        Region region = new Region();
        region.setPrefHeight(15);
        contentBox.getChildren().add(region);

        // 提示信息
        text = new Text();
        text.setStyle("-fx-fill: white; -fx-font-size:24;");
        contentBox.getChildren().add(text);

        // 设置内容居中
        contentBox.alignmentProperty().set(Pos.CENTER);
        // 设置布局居中
        setAlignment(contentBox,Pos.CENTER);
        getChildren().add(contentBox);
    }

    public void setImage(URL imageUrl) {
        imageView.setImage(new Image(imageUrl.toString()));
    }

    public void setBackgroundColor(int r, int g, int b, float a){
        StringBuilder colorBuilder = new StringBuilder("-fx-background-color: rgba(");
        colorBuilder.append(r).append(",").append(g).append(",").append(b).append(",").append(a).append(")");
        setStyle(colorBuilder.toString());
    }

    public void setImageSize(double width, double height){
        if (imageView != null){
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
    }

    public void setImageOpacity(double value){
        if (imageView != null){
            imageView.setOpacity(value);
        }
    }

    public void setMessage(String msg){
        text.setText(msg);
    }

    /**
     * 启动超时计时器
     *
     * @param delay   超时时间，毫秒值
     */
    public void lauchTimeoutTimer(long delay){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> showCancelButton());
                timer.cancel();
            }
        },delay);
    }

    /**
     *   显示取消按钮。注意：本节点的父类必须是Pane或其派生类，当点击取消按钮时会把当前节点从父节点中移除。
     */
    public void showCancelButton(){

        if(btnCancel != null){
            btnCancel.setVisible(true);
            return;
        }

        // 间隔
        Region topRegion = new Region();
        topRegion.setPrefHeight(20);
        contentBox.getChildren().add(topRegion);

        // 取消按钮
        btnCancel = new Button();
        btnCancel.getStylesheets().add("css/common.css");
        btnCancel.getStyleClass().add("button_line_white");
        btnCancel.setStyle("-fx-font: 20px \"Microsoft YaHei\";");
        btnCancel.setText("取消");
        btnCancel.setOnAction(event -> {
            LogUtils.d("click btnCancel");
            Parent parent = getParent();
            if(parent == null){
                LogUtils.w("remove loading failure because parent is null");
                return;
            }
            Pane pane = (Pane) parent;
            pane.getChildren().remove(this);
        });

        contentBox.getChildren().add(btnCancel);

        // 间隔
        Region bottomRegion = new Region();
        bottomRegion.setPrefHeight(20);
        contentBox.getChildren().add(bottomRegion);
    }

}
