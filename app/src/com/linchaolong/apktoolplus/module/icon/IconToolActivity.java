package com.linchaolong.apktoolplus.module.icon;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.ui.DialogPlus;
import com.linchaolong.apktoolplus.ui.DirectorySelecter;
import com.linchaolong.apktoolplus.ui.FileSelecter;
import com.linchaolong.apktoolplus.utils.ViewUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 *  icon编辑工具
 */
public class IconToolActivity extends Activity{

    public static final String TAG = IconToolActivity.class.getSimpleName();

    @FXML
    AnchorPane paneIcon;
    @FXML
    AnchorPane paneBorder;
    @FXML
    CheckBox checkBoxShowBorder;
    @FXML
    Button btnSelectIcon;
    @FXML
    Button btnSelectJiaoBiao;
    @FXML
    Button btnExport;
    @FXML
    Button btnExportMulti;

    @FXML
    ImageView imageViewIcon;
    @FXML
    ImageView imageViewJiaoBiao;

    @FXML
    Slider sliderX;
    @FXML
    Slider sliderY;

    // 多分辨率icon size
    private static Map<String,Double> iconSizeMap = new HashMap<>();
    static{
//        drawable-mdpi        48*48
//        drawable-hdpi         72*72
//        drawable-xhdpi       96*96
//        drawable-xxhdpi     144*144
//        drawable-xxhdpi     192*192
        iconSizeMap.put("drawable-mdpi",48.0);
        iconSizeMap.put("drawable-hdpi",72.0);
        iconSizeMap.put("drawable-xhdpi",96.0);
        iconSizeMap.put("drawable-xxhdpi",144.0);
        iconSizeMap.put("drawable-xxxhdpi",192.0);
    }

    /**
     * 选择Icon
     */
    public void selectIcon(){
        // 恢复上次打开的目录
        File lastDir = Config.getDir(Config.kLastOpenIconDir);
        // 选择Icon
        File iconFile = FileSelecter.create(btnSelectIcon.getScene().getWindow())
                .addFilter("png","jpg","jpeg")
                .setTitle("选择icon")
                .setInitDir(lastDir)
                .showDialog();
        if(iconFile != null){

            Image icon = null;
            try {
                icon = new Image(iconFile.toURI().toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            imageViewIcon.setImage(icon);

            // 保存最后打开的目录
            Config.set(Config.kLastOpenIconDir,iconFile.getParentFile().getPath());
        }
    }

    /**
     * 选择角标
     */
    public void selectJiaoBiao(){

        if(imageViewIcon == null){
            DialogPlus.alert("选择角标",null,"请先选择Icon");
            return;
        }

        // 恢复上次打开的目录
        File lastDir = Config.getDir(Config.kLastOpenJiaoBiaoDir);

        File jiaoBiaoFile = FileSelecter.create(btnSelectIcon.getScene().getWindow())
                .addFilter("png","jpg","jpeg")
                .setTitle("选择角标")
                .setInitDir(lastDir)
                .showDialog();
        if(jiaoBiaoFile != null){
            Image jiaoBiao = null;
            try {
                jiaoBiao = new Image(jiaoBiaoFile.toURI().toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            imageViewJiaoBiao.setImage(jiaoBiao);

            // 重置角标
            resetJiaoBiao();

            // 保存最后打开的目录
            Config.set(Config.kLastOpenJiaoBiaoDir, jiaoBiaoFile.getParentFile().getPath());
        }
    }

    /**
     * 导出安全检查
     *
     * @return  是否可导出
     */
    private boolean exportCheck(){

        if(imageViewIcon.getImage() == null){
            //DialogPlus.alert("提示",null,"请先选择Icon");
            showToast("请先选择Icon");
            return false;
        }
        // 可不选择角标
//        if(imageViewJiaoBiao.getImage() == null){
//            DialogPlus.alert("提示",null,"请先选择角标");
//            return false;
//        }
        return true;
    }

    /**
     * 导出
     */
    public void export(){

        // 导出安全检查
        if(!exportCheck()){
            return;
        }

        // 恢复上次打开的目录
        File lastDir = Config.getDir(Config.kLastSaveIconDir);

        File saveFile = FileSelecter.create(btnExport.getScene().getWindow())
                .addFilter("png")
                .setTitle("保存Icon")
                .setInitDir(lastDir)
                .showSaveDialog();

        if (saveFile == null) {
            return;
        }

        ViewUtils.node2Png(paneIcon, saveFile);

        // 保存最后打开的目录
        Config.set(Config.kLastSaveIconDir, saveFile.getParentFile().getPath());
    }

    /**
     * 导出多分辨率icon
     */
    public void exportMulti(){
        // 导出安全检查
        if(!exportCheck()){
            return;
        }

        // 恢复上次打开的目录
        File lastDir = Config.getDir(Config.kLastSaveIconDir);

        File saveDir = DirectorySelecter.create(btnExportMulti.getScene().getWindow())
                .setTitle("请选择导出目录")
                .setInitDir(lastDir)
                .showDialog();

        if (saveDir == null) {
            return;
        }

        File exportDir;
        for(Map.Entry<String,Double> sizeEntry : iconSizeMap.entrySet()){
            exportDir = new File(saveDir,sizeEntry.getKey());
            exportDir.mkdirs();
            ViewUtils.node2Png(paneIcon, new File(exportDir, "icon.png"),sizeEntry.getValue(),sizeEntry.getValue());
        }

        // 保存最后打开的目录
        Config.set(Config.kLastSaveIconDir, saveDir.getPath());
    }

    /**
     * 重置角标
     */
    public void resetJiaoBiao(){
        // reset slider
        // X轴
        sliderX.setDisable(false);
        // Y轴
        sliderY.setDisable(false);
        // 默认值
        sliderX.setValue(sliderX.getMax()/2);
        sliderY.setValue(sliderY.getMax()/2);

        // reset jiaobiao
        imageViewJiaoBiao.setX(0);
        imageViewJiaoBiao.setY(0);
    }

    // 初始化方法
    @FXML
    public void initialize() {
        // 边框显示切换
        checkBoxShowBorder.selectedProperty().addListener((observable, oldValue, newValue) -> {
            paneBorder.setVisible(newValue);
            Config.set(Config.kIconShowBorder, newValue);
        });

        // X轴
        sliderX.setDisable(true);
        // Y轴
        sliderY.setDisable(true);
        // 进度监听
        sliderX.valueProperty().addListener((observable, oldValue, newValue) -> {
//            LogUtils.d( "sliderX value=" + newValue);
            imageViewJiaoBiao.setLayoutX(newValue.intValue()-50);
        });
        sliderY.valueProperty().addListener((observable, oldValue, newValue) -> {
//            LogUtils.d( "sliderY value=" + newValue);
            imageViewJiaoBiao.setLayoutY(-(newValue.intValue()-50));
        });

        // 默认显示边框
        checkBoxShowBorder.setSelected(Config.TRUE.equals(Config.get(Config.kIconShowBorder,Config.TRUE)));
    }
}
