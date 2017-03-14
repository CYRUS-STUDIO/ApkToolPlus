package com.linchaolong.apktoolplus.module.main;

import com.linchaolong.apktoolplus.utils.*;
import com.linchaolong.apktoolplus.utils.javafx.AnimationHelper;
import com.linchaolong.apktoolplus.utils.javafx.ResizeHelper;
import ee.ioc.cs.jbe.browser.BrowserApplication;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import com.linchaolong.apktoolplus.core.*;
import com.linchaolong.apktoolplus.core.debug.LogManager;
import com.linchaolong.apktoolplus.core.debug.OutputListener;
import com.linchaolong.apktoolplus.Config;
import strings.Strings;
import com.linchaolong.apktoolplus.module.apkinfoprinter.ApkInfoPrinterActivity;
import com.linchaolong.apktoolplus.module.jiagu.JiaGuActivity;
import com.linchaolong.apktoolplus.module.apktool.ApkToolActivity;
import com.linchaolong.apktoolplus.module.debug.DebugView;
import com.linchaolong.apktoolplus.module.icon.IconToolActivity;
import com.linchaolong.apktoolplus.module.settings.SettingsActivity;
import com.linchaolong.apktoolplus.ui.DirectorySelecter;
import com.linchaolong.apktoolplus.ui.FileSelecter;
import com.linchaolong.apktoolplus.ui.UIStack;
import com.linchaolong.apktoolplus.utils.ViewUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

/**
 *
 */
public class MainActivity extends MainView implements Initializable {

    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * 打开调试模式
     */
    public void debug() {
        if (debugView.isShowing()) {
            debugView.hide();
        } else {
            debugView.show();
        }
    }

    /**
     * 最小化
     */
    public void minimized() {
        Stage stage = (Stage) btnMinimized.getScene().getWindow();
        if (stage.isMaximized()) {
            // 重置窗口大小
            stage.setMaximized(false);
            stage.setWidth(Config.WINDOW_WIDTH);
            stage.setHeight(Config.WINDOW_HEIGHT);
            stage.centerOnScreen();
            // 后台运行
            Platform.runLater(() -> {
                stage.setIconified(true);
            });
        } else {
            stage.setIconified(true);
        }
    }


    /**
     * 关闭窗口
     */
    public void close() {
        Config.set(Config.kLastPageIndex, String.valueOf(pages.getCurrentPageIndex()));
        Config.save(() -> AppManager.exit());
    }

    /**
     * ui stack
     **/
    private UIStack uiStack;

    /**
     * 把一个ui加入到ui stack
     *
     * @param fxmlUrl ui路径
     * @param isCache 是否缓存该ui
     */
    public void pushPane(URL fxmlUrl, boolean isCache) {
        uiStack.push(fxmlUrl,isCache);
        // 不显示main ui
        groupPages.setVisible(false);
        pages.setVisible(false);
        // 显示back按钮
        btnBack.setVisible(true);
    }

    /**
     * 从ui stack中弹出一个ui
     */
    public void popPane() {
        uiStack.pop();
        // 回退到了main ui
        if (uiStack.size() == 0) {
            // appName
            textTitle.setText(Config.APP_NAME);
            // 显示main ui
            groupPages.setVisible(true);
            pages.setVisible(true);
            // 不显示back按钮
            btnBack.setVisible(false);
        }
    }

    /**
     * 显示反编译界面
     */
    public void actionApkTool() {
        textTitle.setText("ApkTool");
        pushPane(ApkToolActivity.class.getResource("apktool.fxml"), true);
    }

    /**
     * 显示apk加固界面
     */
    public void actionApkEncrypted() {
        textTitle.setText("Apk加固");
        pushPane(JiaGuActivity.class.getResource("jiagu.fxml"), true);
    }

    /**
     * 角标生成工具
     */
    public void actionIconTool() {
        textTitle.setText("角标生成工具");
        pushPane(IconToolActivity.class.getResource("icon_tool.fxml"), true);
    }

    private boolean isOpeningProguard = false;
    /**
     * Proguard，Java代码混淆工具
     */
    public void actionProguard() {
        if(isOpeningProguard){
            showToast("Proguard已打开");
            return;
        }
        isOpeningProguard = true;
        TaskManager.get().submit(()->{
            Proguard.proguardGUI();
            isOpeningProguard = false;
        });
    }

    public void actionProguardHelp(){
        //http://proguard.sourceforge.net/manual/usage.html
        AppManager.browser("http://proguard.sourceforge.net/manual/usage.html");
    }


    private boolean isJar2Smaling = false;
    /**
     * 显示jar转换smali界面
     */
    public void actionJar2Smali() {
        if (isJar2Smaling) {
            return;
        }
        String srcText = btnJar2Smali.getText();
        openFileSelector(Config.kLastOpenJar2SmaliDir, Strings.get("title_jar2smali"), new SelectorCallback() {
            @Override
            public void uiOnSelected(File file) {
                btnJar2SmaliOut.setVisible(false);
                btnJar2Smali.setText(Strings.get("parsing_please_waiting"));
            }
            @Override
            public void onSelected(File jarFile) {
                isJar2Smaling = true;
                File outDir = new File(jarFile.getParentFile(), jarFile.getName() + "_smali");
                boolean isSuccess = ApkToolPlus.jar2smali(jarFile, outDir);
                if (isSuccess) {
                    // 在sublime中打开
                    AppManager.showInSublime(outDir);
                } else {
                    showToast(jarFile.getName() + "转换失败");
                }
                outDirMap.put(Config.kLastOpenJar2SmaliDir, outDir);
                LogUtils.d( "Jar2Smaling isSuccess=" + isSuccess);
                btnDex2JarOpenFile.setVisible(true);
                isJar2Smaling = false;
            }
            @Override
            public void uiOnEnd() {
                btnJar2SmaliOut.setVisible(true);
                btnJar2Smali.setText(srcText);
            }
        },"jar");
    }

    private boolean isClass2Smaling = false;
    /**
     * 显示class转换smali界面
     */
    public void actionClass2Smali() {
        if (isClass2Smaling) {
            return;
        }
        String srcText = btnClass2Smali.getText();
        openDirSelector(Config.kLastOpenClass2SmaliDir, Strings.get("title_class2smali"), new SelectorCallback() {
            @Override
            public void uiOnSelected(File file) {
                btnClass2SmaliOut.setVisible(false);
                btnClass2Smali.setText(Strings.get("parsing_please_waiting"));
            }
            @Override
            public void onSelected(File classFile) {
                isClass2Smaling = true;
                File outDir = new File(classFile.getParentFile(), classFile.getName() + "_smali");
                boolean isSuccess = ApkToolPlus.class2smali(classFile, outDir);
                if (isSuccess) {
                    // 在sublime中打开
                    AppManager.showInSublime(outDir);
                } else {
                    showToast(classFile.getName() + "转换失败");
                }
                outDirMap.put(Config.kLastOpenClass2SmaliDir, outDir);
                LogUtils.d( "Class2Smali isSuccess=" + isSuccess);
                isClass2Smaling = false;
            }
            @Override
            public void uiOnEnd() {
                btnClass2SmaliOut.setVisible(true);
                btnClass2Smali.setText(srcText);
            }
        });
    }

    private boolean isDex2Smaling = false;
    /**
     * 显示dex转换smali界面
     */
    public void actionDex2Smali() {
        if (isDex2Smaling) {
            return;
        }
        String srcText = btnDex2Smali.getText();
        openFileSelector(Config.kLastOpenDex2SmaliDir, Strings.get("title_dex2smali"), new SelectorCallback() {
            @Override
            public void uiOnSelected(File file) {
                btnDex2SmaliOut.setVisible(false);
                btnDex2Smali.setText(Strings.get("parsing_please_waiting"));
            }
            @Override
            public void onSelected(File dexFile) {
                new Dex2SmaliMultDexSupport(dexFile){
                    @Override
                    public void onStart() {
                        isDex2Smaling = true;
                    }
                    @Override
                    public void onEnd(List<File> smaliDirList) {
                        if(smaliDirList != null && !smaliDirList.isEmpty()){
                            File outDir = smaliDirList.get(0).getParentFile();
                            outDirMap.put(Config.kLastOpenDex2SmaliDir, outDir);
                            // 在sublime中打开
                            AppManager.showInSublime(outDir);
                        }
                        isDex2Smaling = false;
                    }
                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onFailure(File dexFile) {
                        showToast(dexFile.getName()+"转换失败");
                    }
                };
            }
            @Override
            public void uiOnEnd() {
                btnDex2SmaliOut.setVisible(true);
                btnDex2Smali.setText(srcText);
            }
        },"dex","apk");
    }

    private boolean isSmali2Dexing = false;
    /**
     * 显示smali转换dex界面
     */
    public void actionSmali2Dex() {
        if (isSmali2Dexing) {
            return;
        }
        String srcText = btnSmali2Dex.getText();
        openDirSelector(Config.kLastOpenSmali2DexDir, Strings.get("title_smali2dex"), new SelectorCallback() {
            @Override
            public void uiOnSelected(File file) {
                btnSmali2DexOut.setVisible(false);
                btnSmali2Dex.setText(Strings.get("parsing_please_waiting"));
            }
            @Override
            public void onSelected(File smaliDir) {
                isSmali2Dexing = true;
                File dexFile = new File(smaliDir.getParentFile(), smaliDir.getName() + ".dex");
                boolean isSuccess = ApkToolPlus.smali2dex(smaliDir.getPath(), dexFile.getPath());
                if (isSuccess) {
                    outDirMap.put(Config.kLastOpenSmali2DexDir, dexFile);
                }else{
                    showToast(smaliDir.getName() + "转换失败");
                }
                LogUtils.d( "Smali2Dex isSuccess=" + isSuccess);
                isSmali2Dexing = false;
            }
            @Override
            public void uiOnEnd() {
                btnSmali2DexOut.setVisible(true);
                btnSmali2Dex.setText(srcText);
            }
        });
    }

    private boolean isClass2Dexing = false;

    /**
     * 显示class转换dex界面
     */
    public void actionClass2Dex() {
        if (isClass2Dexing) {
            return;
        }
        String srcText = btnClass2Dex.getText();
        openDirSelector(Config.kLastOpenClass2DexDir, Strings.get("title_class2dex"), new SelectorCallback() {
            @Override
            public void uiOnSelected(File file) {
                btnClass2DexOut.setVisible(false);
                btnClass2Dex.setText(Strings.get("parsing_please_waiting"));
            }
            @Override
            public void onSelected(File classDir) {
                isClass2Dexing = true;
                File dexFile = new File(classDir.getParentFile(), classDir.getName() + ".dex");
                boolean isSuccess = ApkToolPlus.class2dex(classDir, dexFile.getPath());
                if (isSuccess) {
                    outDirMap.put(Config.kLastOpenClass2DexDir, dexFile);
                }else{
                    showToast(classDir.getName() + "转换失败");
                }
                LogUtils.d( "Class2Dex isSuccess=" + isSuccess);
                isClass2Dexing = false;
            }
            @Override
            public void uiOnEnd() {
                btnClass2DexOut.setVisible(true);
                btnClass2Dex.setText(srcText);
            }
        });
    }

    private boolean isDex2Jaring = false;
    private File lastDex2JarFile;

    /**
     * Dex2Jar
     */
    public void actionDex2Jar() {
        if (isDex2Jaring) {
            return;
        }
        String srcText = btnDex2Jar.getText();
        openFileSelector(Config.kLastOpenDex2SmaliDir, Strings.get("title_dex2jar"), new SelectorCallback() {
            @Override
            public void uiOnSelected(File file) {
                btnDex2JarOut.setVisible(false);
                btnDex2Jar.setText(Strings.get("parsing_please_waiting"));
            }
            @Override
            public void onSelected(File dexFile) {
                isDex2Jaring = true;
                new Dex2JarMultDexSupport(dexFile) {
                    @Override
                    public void onStart() {

                    }
                    @Override
                    public void onEnd(List<File> jarFileList) {
                        btnDex2JarOpenFile.setVisible(true);
                        isDex2Jaring = false;
                        if(!jarFileList.isEmpty()){
                            File jarFile = jarFileList.get(0);
                            lastDex2JarFile = jarFile;
                            // 显示源码
                            openFileOnJD(jarFileList);
                            // 记录输出文件目录
                            outDirMap.put(Config.kLastOpenDex2JarDir, jarFile);
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        isDex2Jaring = false;
                    }
                    @Override
                    public void onFailure(File dexFile) {
                        showToast(dexFile.getName() + "转换失败");
                    }
                };
            }
            @Override
            public void uiOnEnd() {
                btnDex2JarOut.setVisible(true);
                btnDex2Jar.setText(srcText);
            }
        },"dex","apk");
    }

    /**
     * JBE（Java Byte Editor）Java字节码编辑器
     */
    public void actionJBE() {
        TaskManager.get().submit(()->{
            Global.showLoading();
            BrowserApplication.main(new String[]{});
            Global.hideLoading();
        });
    }

    /**
     * JD（Java Decompiler）Java字节码反编译工具
     */
    public void actionJD() {
        TaskManager.get().submit(()->{
            Global.showLoading();
            org.jd.gui.App.main();
            Global.hideLoading();
        });
    }

    private Stage uiApkInfoPrinter;

    /**
     * ApkInfoPrinter，apk、AndroidManifest.xml解析工具，集成了ApkParser和AXMLPrinter2
     */
    public void actionApkInfoPrinter() {
        if (uiApkInfoPrinter == null) {
            uiApkInfoPrinter = ViewUtils.newWindow(ApkInfoPrinterActivity.class.getResource("apkinfoprinter.fxml"), false);
            uiApkInfoPrinter.setTitle("ApkInfoPrinter");
            // 设置应用图标
            ViewUtils.setWindowIcon(uiApkInfoPrinter, ClassUtils.getResourceAsURL("res/white_icon/white_icon_Plus.png"));
            // 设置拖拽事件
            Parent root = uiApkInfoPrinter.getScene().getRoot();
            ViewUtils.registerDragEvent(uiApkInfoPrinter, root);
            // 最少宽高
            uiApkInfoPrinter.setMinWidth(800);
            uiApkInfoPrinter.setMinHeight(600);
            // 可变大小
            ResizeHelper.addResizeListener(uiApkInfoPrinter, false);
        }

        if (!uiApkInfoPrinter.isShowing()) {
            uiApkInfoPrinter.show();
        }

        uiApkInfoPrinter.toFront();
    }


    private boolean isJadDecomping = false;
    private File lastJadFile;

    private void showJadLoading(){
        btnJadDir.setVisible(false);
        btnJadJar.setVisible(false);
        btnJadBrowser.setVisible(false);
        // 显示loading图片
    }
    private void hideJadLoading(){
        btnJadDir.setVisible(true);
        btnJadJar.setVisible(true);
        btnJadBrowser.setVisible(true);
    }

    /**
     * Jad，选择一个目录
     */
    public void actionJadDir(){
        if(isJadDecomping){
            showToast("正在反编译，请稍候...");
            return;
        }
        File lastDir = Config.getDir(Config.kLastOpenJadDir);
        File file = DirectorySelecter.create(btnClose.getScene().getWindow())
                .setTitle("反编译选择目录下Java字节码")
                .setInitDir(lastDir)
                .showDialog();
        if(file != null && file.exists()){
            TaskManager.get().submit(()->{
                isJadDecomping = true;
                Config.set(Config.kLastOpenJadDir,file.getParent());
                File srcFile = new File(file.getParentFile(), PinyinUtils.shortPinyin(file.getName()) + "_src");
                boolean result = Jad.decompileByCmd(file, srcFile);
                if(result){
                    lastJadFile = srcFile;
                    showToast("正在反编译"+file.getName());
                }else{
                    showToast(file.getName()+"反编译失败");
                }
                isJadDecomping = false;
            });
        }
    }

    /**
     * Jad，选择一个jar
     */
    public void actionJadJar(){
        if(isJadDecomping){
            showToast("正在反编译，请稍候...");
            return;
        }
        File lastDir = Config.getDir(Config.kLastOpenJadJarDir);
        File file = FileSelecter.create(btnClose.getScene().getWindow())
                .setInitDir(lastDir)
                .setTitle("反编译jar文件")
                .addFilter("jar", "zip")
                .showDialog();
        if(file != null && file.exists()){
            TaskManager.get().submit(()-> {
                isJadDecomping = true;
                Config.set(Config.kLastOpenJadJarDir, file.getParent());
                File srcFile = new File(file.getParentFile(), PinyinUtils.shortPinyin(file.getName()) + "_src");
                boolean result = Jad.decompileByCmd(file, srcFile);
                if (result) {
                    lastJadFile = srcFile;
                    showToast("'"+file.getName()+"'反编译成功");
                    AppManager.showInSublime(srcFile);
                } else {
                    showToast("'"+file.getName() + "'反编译失败");
                }
                isJadDecomping = false;
            });
        }
    }

    /**
     * Jad，浏览反编译的源码
     */
    public void actionJadBrowser(){
        if(lastJadFile == null || !lastJadFile.exists()){
            showToast("未反编译任何文件");
            return;
        }
        // 优先在sublime中打开
        if(!AppManager.showInSublime(lastJadFile)){
            FileHelper.showInExplorer(lastJadFile);
        }
    }

    private Stage settingDialog = null;
    /**
     * 设置
     */
    public void actionSetting() {
        ActivityManager.startActivity(
                SettingsActivity.class.getResource("settings.fxml"),
                Pos.CENTER_RIGHT,
                AnimationHelper.fadeIn(null));
    }

    /**
     * 返回main界面
     */
    public void actionHome() {
        while (uiStack.size() > 0) {
            popPane();
        }
    }

    /**
     * 打开缓存目录
     */
    public void actionOpenAppOutDir(){
        AppManager.browser(AppManager.getOutputDir());
    }

    /**
     * 跳到指定页面
     *
     * @param pageIndex 页面索引，从0开始
     */
    private void toPage(Integer pageIndex) {
        ListIterator<Node> nodeListIterator = groupPages.getChildren().listIterator();
        for (Integer i = 0; nodeListIterator.hasNext(); ++i) {
            Node node = nodeListIterator.next();
            node.setVisible(i.equals(pageIndex));
        }
    }

    // 初始化方法
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sInstance = this;
        // 初始化UIStack
        uiStack = new UIStack(paneUIContainer);
        // 显示appName
        textTitle.setText(Config.APP_NAME);
        // 默认不显示回退按钮
        btnBack.setVisible(false);
        // 显示任务队列大小
        TaskManager.get().setTaskQueueListener((task, code) -> {
            textTaskQueueSize.setText("当前任务队列大小 : " + TaskManager.get().queueSize());
        });
        // main pages
        pages.setPageFactory(pageIndex -> {
//            LogUtils.d( "toPage pageIndex=" + pageIndex + " currentPageIndex=" + pages.getCurrentPageIndex());
            toPage(pageIndex);
            return nullRegion;
        });
        // 创建debug view
        debugView = new DebugView();
        // 设置日志输出监听
        LogManager.getInstance().setOutputListener(new OutputListener() {
            @Override
            public void write(int b) {
                if (debugView.isShowing()) {
                    debugView.print(String.valueOf((char) b));
                }
            }
            @Override
            public void write(byte[] buf, int off, int len) {
                if (debugView.isShowing()) {
                    debugView.print(new String(buf, off, len));
                }
            }
        });
        // 初始化打开输出目录按钮们
        initBtnOut();
        btnDex2JarOpenFile.setVisible(false);
        // 查看dex转换的jar
        btnDex2JarOpenFile.setOnAction(event -> {
            openFileOnJD(lastDex2JarFile);
        });
    }

    private static MainActivity sInstance;
    public static MainActivity getInstance() {
        return sInstance;
    }
}
