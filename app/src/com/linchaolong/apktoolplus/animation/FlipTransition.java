package com.linchaolong.apktoolplus.animation;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ListIterator;

public class FlipTransition {

    private static final double DURATION = 1_000;
    private double width;
    private double height;

    public FlipTransition(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void play(Group group) {
        Node present = group.getChildren().get(group.getChildren().size() - 1);

        // 現在のページのクリップ
        Rectangle presentClip = new Rectangle(0, 0, width, height);
        present.setClip(presentClip);

        // 現在のページのクリップの移動アニメーション
        TranslateTransition presentClipAnim = new TranslateTransition(Duration.millis(DURATION), presentClip);
        presentClipAnim.setToX(-width);
        presentClipAnim.setInterpolator(Interpolator.EASE_IN);

        // ページ裏
        Group flip = new Group();

        // ページ裏の下地
        Rectangle flipRect = new Rectangle(0, 0, width, height);
        flipRect.setFill(Color.WHITE);
        flip.getChildren().add(flipRect);

        // ページを反転したイメージ
        ImageView flipImage = new ImageView(present.snapshot(new SnapshotParameters(), null));
        flipImage.setRotationAxis(new Point3D(0.0, 1.0, 0.0));
        flipImage.setRotate(180.0);
        flipImage.setOpacity(0.4);
        flip.getChildren().add(flipImage);

        // ページ裏用のクリップ
        Rectangle flipClip = new Rectangle(0, 0, width, height);
        flip.setClip(flipClip);

        group.getChildren().add(flip);

        // ページ裏のアニメーション
        TranslateTransition flipAnim = new TranslateTransition(Duration.millis(DURATION), flip);
        flipAnim.setFromX(width);
        flipAnim.setToX(-width);
        flipAnim.setInterpolator(Interpolator.EASE_IN);

        // ページ裏のクリップのアニメーション
        TranslateTransition clipAnim = new TranslateTransition(Duration.millis(DURATION), flipClip);
        clipAnim.setFromX(-width);
        clipAnim.setToX(0);
        clipAnim.setInterpolator(Interpolator.EASE_IN);

        // ページの間にできる影
        Rectangle shadow = new Rectangle(width - 30, 0, 60, height);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(0, 0, 0, 0.0)),
                new Stop(0.5, Color.rgb(0, 0, 0, 0.5)),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.0)));
        shadow.setFill(gradient);
        group.getChildren().add(shadow);

        // 影が太るアニメーション
        ScaleTransition shadowAnim1 = new ScaleTransition(Duration.millis(DURATION / 5), shadow);
        shadowAnim1.setFromX(0.0);
        shadowAnim1.setToX(1.0);
        shadowAnim1.setInterpolator(Interpolator.EASE_IN);

        // 影の移動アニメーション
        TranslateTransition shadowAnim2 = new TranslateTransition(Duration.millis(DURATION), shadow);
        shadowAnim2.setToX(-width);
        shadowAnim2.setInterpolator(Interpolator.EASE_IN);

        // 影が痩せるアニメーション
        ScaleTransition shadowAnim3 = new ScaleTransition(Duration.millis(DURATION / 5), shadow);
        shadowAnim3.setFromX(1.0);
        shadowAnim3.setToX(0.0);
        shadowAnim3.setInterpolator(Interpolator.EASE_OUT);
        SequentialTransition seqAnim
                = new SequentialTransition(new PauseTransition(Duration.millis(DURATION * 4 / 5)),
                        shadowAnim3);

        ParallelTransition animation = new ParallelTransition(
                presentClipAnim,
                flipAnim,
                clipAnim,
                shadowAnim1,
                shadowAnim2,
                seqAnim);
        
        animation.setOnFinished(e -> {
            group.getChildren().remove(flip);
            group.getChildren().remove(shadow);

            // 只显示当前页
            ListIterator<Node> nodeListIterator = group.getChildren().listIterator();
            while(nodeListIterator.hasNext()){
                Node node = nodeListIterator.next();
                //if(!node.equals(present)){
                node.setVisible(false);
                //}
            }
            present.setVisible(true);

            present.setTranslateX(0);
            present.setClip(null);
            group.getChildren().remove(present);
            group.getChildren().add(0, present);
        });

        animation.play();
    }
}