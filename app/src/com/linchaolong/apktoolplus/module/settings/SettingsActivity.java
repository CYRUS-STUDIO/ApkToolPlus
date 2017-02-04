package com.linchaolong.apktoolplus.module.settings;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.core.ActivityManager;
import com.linchaolong.apktoolplus.core.AppManager;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.utils.javafx.AnimationHelper;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by linchaolong on 2015/9/19.
 */
public class SettingsActivity extends Activity implements Initializable {

    public static final String TAG = SettingsActivity.class.getSimpleName();

    @FXML
    Button btnClose;
    @FXML
    AnchorPane paneItems;
    @FXML
    AnchorPane paneContent;
    @FXML
    VBox boxItems;
    @FXML
    Text textVersion;


    // 关闭设置界面
    public void close() {
        FadeTransition fadeTransition = AnimationHelper.fadeOut(event -> {
            ActivityManager.getRootView().getChildren().remove(getRootView());
        });
        fadeTransition.setNode(getRootView());
        fadeTransition.play();
        // 保存配置
        Config.save();
    }

    private void initView() {
        initItems();
        textVersion.setText("Version:"+ AppManager.getVersion());
    }

    /**
     * 初始化选择项
     */
    private void initItems(){
        // 左边的选择项
        ObservableList<Node> items = boxItems.getChildren();
        ToggleGroup group = new ToggleGroup();
        // 关联group和index
        for(int index=0; index<items.size(); ++index){
            ToggleButton item = (ToggleButton) items.get(index);
            item.setToggleGroup(group);
            item.setUserData(index);
        }
        // 切换监听
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) ->{
            if(newValue != null){
                Integer itemIndex = (Integer) newValue.getUserData();
                showSettingContent(itemIndex);
            }else{
                group.selectToggle(oldValue);
            }
        });
        // 默认选择第一个
        group.getToggles().get(0).setSelected(true);
    }

    // ui cache
    Map<String,Parent> uiCache = new HashMap();

    /**
     * 根据设置项的index显示对应的设置项内容
     *
     * @param itemIndex 设置项的index
     */
    private void showSettingContent(int itemIndex){
        String fxml = null;
        switch (itemIndex){
            // 常规设置
            case 0:
                fxml = "settings_common.fxml";
                break;
            // ApkTool
            case 1:
                fxml = "settings_apktool.fxml";
                break;
            // JavaTool
            case 2:
                fxml = "settings_about.fxml";
                break;
        }
        if(fxml != null){
            Parent ui = uiCache.get(fxml);
            if(ui == null){
                try {
                    ui = FXMLLoader.load(getClass().getResource(fxml));
                    uiCache.put(fxml,ui);
                    paneContent.getChildren().add(ui);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            for(Parent node : uiCache.values()){
                node.setVisible(false);
            }
            ui.setVisible(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initRootView(btnClose);
        initView();
    }

}
