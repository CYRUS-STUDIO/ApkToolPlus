package com.linchaolong.apktoolplus.module.debug;

import javafx.scene.Parent;
import javafx.stage.Stage;
import com.linchaolong.apktoolplus.utils.ClassHelper;
import com.linchaolong.apktoolplus.utils.javafx.ResizeHelper;
import com.linchaolong.apktoolplus.utils.UIHelper;

/**
 * 日志调试视图
 *
 * Created by linchaolong on 2015/9/7.
 */
public class DebugView {

    public static final String TAG = DebugView.class.getSimpleName();

    private Stage stage = new Stage();
    public DebugView() {
        initView();
    }

    /**
     * 初始化debug view
     */
    public void initView(){
        stage = UIHelper.newWindow(getClass().getResource("debug.fxml"), false);
        stage.setTitle("Debug");
        // 设置应用图标
        UIHelper.setWindowIcon(stage, ClassHelper.getResourceAsURL("res/white_icon/white_icon_Plus.png"));
        // 设置拖拽事件
        Parent root = stage.getScene().getRoot();
        UIHelper.registerDragEvent(stage, root);
        // 可变大小
        stage.setMinWidth(800);
        stage.setMinHeight(450);
        ResizeHelper.addResizeListener(stage, false);
    }

    /**
     * 显示debug view
     */
    public void show(){
        stage.show();
    }

    /**
     * 隐藏debug view
     */
    public void hide(){
        stage.hide();
    }

    /**
     * debug view是否在显示
     *
     * @return
     */
    public boolean isShowing(){
        return stage.isShowing();
    }

    /**
     * 打印日志
     *
     * @param log
     */
    public void print(final String log){
        DebugActivity.print(log);
    }

}
