package com.linchaolong.apktoolplus.module.main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.core.Global;
import com.linchaolong.apktoolplus.module.debug.DebugView;
import com.linchaolong.apktoolplus.ui.DirectorySelecter;
import com.linchaolong.apktoolplus.ui.FileSelecter;
import com.linchaolong.apktoolplus.utils.Debug;
import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.TaskHandler;
import org.jd.gui.controller.MainController;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 界面逻辑
 * <p>
 * Created by linchaolong on 2016/3/25.
 */
public class MainView extends Activity {

    public static final String TAG = MainView.class.getSimpleName();

    @FXML
    Button btnMinimized;
    @FXML
    Button btnClose;
    @FXML
    Button btnHome;
    @FXML
    Button btnSetting;

    @FXML
    Text textTitle;
    @FXML
    Text textTaskQueueSize;

    @FXML
    AnchorPane paneUIContainer;
    @FXML
    Group groupPages;
    @FXML
    public Pagination pages;

    @FXML
    Button btnApkTool;
    @FXML
    Button btnApkEncrypted;
    @FXML
    Button btnJBE;

    @FXML
    Button btnDebug;
    @FXML
    Button btnBack;

    // 打开输出目录按钮
    @FXML
    Button btnJar2SmaliOut;
    @FXML
    Button btnClass2SmaliOut;
    @FXML
    Button btnDex2SmaliOut;
    @FXML
    Button btnSmali2DexOut;
    @FXML
    Button btnClass2DexOut;
    @FXML
    Button btnDex2JarOut;
    @FXML
    Button btnDex2JarOpenFile;

    @FXML
    protected Button btnJar2Smali;
    @FXML
    protected Button btnClass2Smali;
    @FXML
    protected Button btnDex2Smali;
    @FXML
    protected Button btnSmali2Dex;
    @FXML
    protected Button btnDex2Jar;
    @FXML
    protected Button btnClass2Dex;

    // Jad
    @FXML
    protected Button btnJadDir;
    @FXML
    protected Button btnJadJar;
    @FXML
    protected Button btnJadBrowser;

    // debug视图
    protected DebugView debugView;
    // null
    protected Region nullRegion = new Region();

    protected Map<String, File> outDirMap = new HashMap<>();

    /**
     * 设置打开输出目录监听
     *
     * @param btn
     * @param lastKey
     */
    protected void codeTemplateSetOpenOutAction(Button btn, String lastKey) {
        btn.setOnAction(event -> {
            File dir = outDirMap.get(lastKey);
            if (dir != null && dir.exists()) {
                FileHelper.showInExplorer(dir);
            } else {
                showToast(dir.getPath() + "不存在");
            }
        });
    }

    /**
     * 打开jar文件在jd
     *
     * @param file
     */
    protected void openFileOnJD(File file) {
        if (file != null && file.exists()) {
            Debug.d( "open file on jd :" + file.getPath());
            TaskHandler.get().submit(() -> {
                Global.showLoading();
                org.jd.gui.App.main();
                MainController controller = org.jd.gui.App.getController();
                List<File> fileList = new ArrayList<>();
                fileList.add(file);
                controller.openFiles(fileList);
                Global.hideLoading();
            });
        } else {
            showToast(file.getName() + "不存在");
        }
    }

    protected void openFileOnJD(List<File> fileList) {
        if (fileList != null && !fileList.isEmpty()) {
            TaskHandler.get().submit(() -> {
                Global.showLoading();
                org.jd.gui.App.main();
                MainController controller = org.jd.gui.App.getController();
                controller.openFiles(fileList);
                Global.hideLoading();
            });
        }
    }

    protected void initBtnOut() {
        // 默认不显示打开输出目录按钮
        btnJar2SmaliOut.setVisible(false);
        codeTemplateSetOpenOutAction(btnJar2SmaliOut, Config.kLastOpenJar2SmaliDir);
        btnClass2SmaliOut.setVisible(false);
        codeTemplateSetOpenOutAction(btnClass2SmaliOut, Config.kLastOpenClass2SmaliDir);
        btnDex2SmaliOut.setVisible(false);
        codeTemplateSetOpenOutAction(btnDex2SmaliOut, Config.kLastOpenDex2SmaliDir);
        btnSmali2DexOut.setVisible(false);
        codeTemplateSetOpenOutAction(btnSmali2DexOut, Config.kLastOpenSmali2DexDir);
        btnClass2DexOut.setVisible(false);
        codeTemplateSetOpenOutAction(btnClass2DexOut, Config.kLastOpenClass2DexDir);
        btnDex2JarOut.setVisible(false);
        codeTemplateSetOpenOutAction(btnDex2JarOut, Config.kLastOpenDex2JarDir);
    }

    protected interface SelectorCallback {
        void uiOnSelected(File file);
        void onSelected(File file);
        void uiOnEnd();
    }

    protected void openFileSelector(String lastDirKey, String title, SelectorCallback callback, String... filters) {
        File lastDir = Config.getDir(lastDirKey);
        File file = FileSelecter.create(btnClose.getScene().getWindow())
                .addFilter(filters)
                .setTitle(title)
                .setInitDir(lastDir)
                .showDialog();

        if (file != null && file.exists()) {
            Config.set(lastDirKey, file.getParent());
            callback.uiOnSelected(file);
            TaskHandler.get().queue(() -> {
                callback.onSelected(file);
                runOnUiThread(() -> callback.uiOnEnd());
            });
        }
    }

    protected void openDirSelector(String lastDirKey, String title, SelectorCallback callback) {
        File lastDir = Config.getDir(lastDirKey);
        File file = DirectorySelecter.create(btnClose.getScene().getWindow())
                .setTitle(title)
                .setInitDir(lastDir)
                .showDialog();

        if (file != null && file.exists()) {
            Config.set(lastDirKey, file.getPath());
            callback.uiOnSelected(file);
            TaskHandler.get().queue(() -> {
                callback.onSelected(file);
                Platform.runLater(() -> {
                    callback.uiOnEnd();
                });
            });
        }
    }
}
