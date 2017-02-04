package com.linchaolong.apktoolplus.utils.javafx;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * 动画相关工具
 *
 * Created by linchaolong on 2015/9/23.
 */
public class AnimationHelper {

    public static final String TAG = AnimationHelper.class.getSimpleName();

    /**
     * 淡入
     *
     * @param onFinish
     * @return
     */
    public static FadeTransition fadeIn(EventHandler<ActionEvent> onFinish){
        FadeTransition ft = new FadeTransition(Duration.millis(500));
        ft.setFromValue(0.0);
        ft.setToValue(1.1);
        ft.setCycleCount(0);
        ft.setAutoReverse(false);
        if(onFinish != null){
            ft.setOnFinished(onFinish);
        }
        return ft;
    }

    /**
     * 淡出
     *
     * @param onFinish
     * @return
     */
    public static FadeTransition fadeOut(EventHandler<ActionEvent> onFinish){
        FadeTransition ft = new FadeTransition(Duration.millis(500));
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setCycleCount(0);
        ft.setAutoReverse(false);
        if(onFinish != null){
            ft.setOnFinished(onFinish);
        }
        return ft;
    }

    /**
     * 闪烁
     *
     * @param node
     */
    public static FadeTransition blink(Node node, EventHandler<ActionEvent> onFinish){
        FadeTransition ft = new FadeTransition(Duration.millis(3000), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.1);
        ft.setCycleCount(Timeline.INDEFINITE);
        ft.setAutoReverse(true);
        if(onFinish != null){
            ft.setOnFinished(onFinish);
        }
        return ft;
    }

    public static void translationRight(Parent view){

    }

    /**
     * 抖动窗口
     *
     * @param window 窗口对象
     */
    public static void shake(Window window) {
        shake(window,4);
    }

    /**
     * 抖动窗口
     *
     * @param window 窗口对象
     * @param count 抖动次数
     */
    public static void shake(Window window, int count) {

        final Delta detla = new Delta();
        detla.x = 0;
        detla.y = 0;

        Timeline timelineX = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            if (detla.x == 0) {
                window.setX(window.getX() + 10);
                detla.x = 1;
            } else {
                window.setX(window.getX() - 10);
                detla.x = 0;
            }
        }));

        //timelineX.setCycleCount(Timeline.INDEFINITE);
        timelineX.setCycleCount(count);
        timelineX.setAutoReverse(false);
        timelineX.play();

//        Timeline timelineY = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
//            if (detla.y == 0) {
//                stage.setY(stage.getY() + 10);
//                detla.y = 1;
//            } else {
//                stage.setY(stage.getY() - 10);
//                detla.y = 0;
//            }
//        }));
//
//        timelineY.setCycleCount(count);
//        timelineY.setAutoReverse(false);
//        timelineY.play();
    }
}
