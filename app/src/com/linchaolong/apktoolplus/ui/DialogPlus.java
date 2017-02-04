package com.linchaolong.apktoolplus.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.linchaolong.apktoolplus.utils.UIHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;

/**
 * Created by linchaolong on 2015/9/6.
 *
 * http://code.makery.ch/blog/javafx-dialogs-official/
 *
 */
public class DialogPlus {

    public static final String TAG = DialogPlus.class.getSimpleName();

    /**
     * 消息弹窗
     *
     * @param title         标题
     * @param headerText    内容标题，可为null
     * @param contentText   提示内容
     */
    public static void alert(String title, String headerText, String contentText){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    /**
     * 消息弹窗
     *
     * @param alertType     消息类型
     * @param title         标题
     * @param headerText    内容标题，可为null
     * @param contentText   提示内容
     */
    public static void alert(Alert.AlertType alertType, String title, String headerText, String contentText){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    /**
     *  显示Exception的Dialog
     *
     * @param ex  异常
     */
    public static void exception(Exception ex){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Look, an Exception Dialog");
        alert.setContentText("Could not find file blabla.txt!");

        //Exception ex = new FileNotFoundException("Could not find file blabla.txt");

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }


    /**
     * 确认对话框
     *
     * @param title         标题
     * @param headerText    内容标题，可为null
     * @param contentText   提示内容
     * @param callback      回调
     */
    public static void confirm(String title, String headerText, String contentText, DialogCallback callback){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            callback.callback(DialogCallback.CODE_CONFIRM,null);
        } else {
            callback.callback(DialogCallback.CODE_CONCEL,null);
        }
    }

    /**
     * 自定义对话框
     *
     * @param view   自定义view
     * @param title  标题
     * @param icon   图标
     * @return
     */
    public static Stage customeDialog(Parent view, String title, URL icon){
        final Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(title);
        if(icon != null){
            UIHelper.setWindowIcon(dialog, icon);
        }
        Scene scene = new Scene(view);
        dialog.setScene(scene);
        return dialog;
    }

    /**
     * 自定义模态对话框
     *
     * @param owner  Stage
     * @param view   自定义view
     * @return
     */
    public static Stage customeModalDialog(Stage owner, Parent view){
        final Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UNDECORATED);
        //dialog.initModality(Modality.WINDOW_MODAL); // 模态对话框
        //dialog.initModality(Modality.NONE);
        dialog.initOwner(owner);
        Scene scene = new Scene(view);
        dialog.setScene(scene);
        return dialog;
    }

}
