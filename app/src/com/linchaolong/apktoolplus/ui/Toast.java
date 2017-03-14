package com.linchaolong.apktoolplus.ui;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by linchaolong on 2015/11/13.
 */
public class Toast {

    public static final String TAG = Toast.class.getSimpleName();

    private Label label;

    public Toast(final String msg) {
        label = new Label(msg);
        String style =  "-fx-background-color:black;" +
                "-fx-background-radius:10;" +
                "-fx-font: 16px \"Microsoft YaHei\";" +
                "-fx-text-fill:white;-fx-padding:10;";
        label.setStyle(style);
        DropShadow dropShadow = new DropShadow();
        dropShadow.setBlurType(BlurType.THREE_PASS_BOX);
        dropShadow.setWidth(40);
        dropShadow.setHeight(40);
        dropShadow.setRadius(19.5);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(00);
        dropShadow.setColor(Color.color(0, 0, 0));
        label.setEffect(dropShadow);
    }

    public static Toast make(final String msg){
        return new Toast(msg);
    }

    public void remove(){
        Parent parent = label.getParent();
        if(parent != null && label != null){
            StackPane ui = (StackPane) parent;
            ui.getChildren().remove(label);
        }
    }

    public Toast show(StackPane ui, long duration){
        Platform.runLater(() -> {
            ui.getChildren().add(label);
            StackPane.setAlignment(label, Pos.BOTTOM_CENTER); // 从下方居中往中间移动
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), label);
            tt.setByY(-(ui.getHeight()/2));
            tt.setCycleCount(1);
            tt.setAutoReverse(false);
            tt.setOnFinished(event -> {
//                LogUtils.d("toast animation finish.");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(label.getParent() != null){
                            Platform.runLater(() -> ui.getChildren().remove(label));
                            timer.cancel();
                        }
                    }
                },duration);
            });
            tt.play();
        });
        return this;
    }


    public Toast show(StackPane ui){
        return show(ui,2500);
    }

}
