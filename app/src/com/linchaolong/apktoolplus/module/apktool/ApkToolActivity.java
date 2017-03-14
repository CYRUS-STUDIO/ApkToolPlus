package com.linchaolong.apktoolplus.module.apktool;

import javafx.fxml.FXML;
import com.linchaolong.apktoolplus.core.ApkToolPlus;
import com.linchaolong.apktoolplus.core.Callback;
import com.linchaolong.apktoolplus.core.KeystoreConfig;
import com.linchaolong.apktoolplus.core.SettingHelper;
import com.linchaolong.apktoolplus.Config;
import com.linchaolong.apktoolplus.ui.DirectorySelecter;
import com.linchaolong.apktoolplus.ui.FileSelecter;
import com.linchaolong.apktoolplus.ui.Loading;
import com.linchaolong.apktoolplus.utils.LogUtils;
import com.linchaolong.apktoolplus.utils.FileHelper;
import com.linchaolong.apktoolplus.utils.TaskManager;
import com.linchaolong.apktoolplus.utils.ViewUtils;

import java.io.File;
import java.util.List;

/**
 *
 */
public class ApkToolActivity extends ApkToolView {

    public static final String TAG = ApkToolActivity.class.getSimpleName();

    //////////////////////////// 反编译 //////////////////////////////
    /**
     * 反编译apk列表
     **/
    private List<File> decompileApkList;
    /**
     * 是否正在反编译
     **/
    private boolean isDecompiling = false;
    private File lastSignedApk;

    /**
     * 选择apk（可多选）
     */
    @FXML
    public void selectDecompileApk() {
        File lastDir = Config.getDir(Config.kLastOpenDecompileDir);
        // apk列表
        List<File> apkList = FileSelecter.create(btnDecompile.getScene().getWindow())
                .setTitle("选择apk（可多选）")
                .addFilter("apk")
                .setInitDir(lastDir)
                .showMultiDialog();

        if (apkList != null && !apkList.isEmpty()) {
            decompileApkList = apkList;
            textFieldDecompileApkList.setText(getStringList(decompileApkList));
            Config.set(Config.kLastOpenDecompileDir, getApkListDir(decompileApkList).getPath());
        }
    }

    private File lastDecompileDir;

    /**
     * 反编译
     */
    @FXML
    public void startDecompile() {

        if (decompileApkList == null || decompileApkList.isEmpty()) {
            showToast("请先选择apk");
            return;
        }

        if (isDecompiling) {
            showToast("正在反编译..");
            return;
        }

        isDecompiling = true;
        Loading loading = ViewUtils.showLoading(paneDecompile, "正在反编译,请稍候...", 20000);

        TaskManager.get().queue(() -> {
            boolean isSuccess = true;
            for (File apk : decompileApkList) {
                lastDecompileDir = new File(apk.getParentFile(), FileHelper.getNoSuffixName(apk));
                isSuccess = ApkToolPlus.decompile(apk, lastDecompileDir, new Callback<Exception>() {
                    @Override
                    public void callback(Exception e) {
                        showToast(apk.getName() + "反编译失败。");
                    }
                });
                if (!isSuccess) {
                    showToast(apk.getName() + "反编译失败");
                    break;
                }
            }
            isDecompiling = false;
            ViewUtils.hideLoading(paneDecompile, loading);
            // 显示打开输出目录
            btnOpenDecompileOut.setVisible(true);
            if (isSuccess) {
                showToast("反编译完成");
            }
        });
    }

    /**
     * 打开输出目录
     */
    @FXML
    public void openDecompileOut() {
        FileHelper.showInExplorer(lastDecompileDir);
    }


    //////////////////////////// 回编译 //////////////////////////////
    /**
     * 回编译apk
     **/
    private File recompileApkDir;
    /**
     * 是否正在回编译
     **/
    private boolean isRecompiling = false;

    /**
     * 回编译apk（可多选）
     */
    @FXML
    public void selectRecompileApk() {
        File lastDir = Config.getDir(Config.kLastOpenRecompileDir);
        // apk列表
        File selectDir = DirectorySelecter.create(btnSelectRecompileApk.getScene().getWindow())
                .setTitle("选择反编译apk所在目录")
                .setInitDir(lastDir)
                .showDialog();
        // 显示目录路径
        if (selectDir != null && selectDir.exists()) {
            recompileApkDir = selectDir;
            textFieldRecompileApkDir.setText(selectDir.getPath());
            Config.set(Config.kLastOpenRecompileDir, selectDir.getPath());
        }
    }

    /**
     * 回编译
     */
    @FXML
    public void startRecompile() {
        LogUtils.d("startRecompile");

        if (recompileApkDir == null) {
            showToast("请先选择apk反编译文件目录");
            return;
        }

        if (!recompileApkDir.exists()) {
            showToast("目录不存在");
            return;
        }

        if (isRecompiling) {
            showToast("正在回编译..");
            return;
        }

        isRecompiling = true;
        Loading loading = ViewUtils.showLoading(paneRecompile, "正在回编译,请稍候...", 18000);

        TaskManager.get().queue(() -> {
            boolean isSuccess = ApkToolPlus.recompile(recompileApkDir, null, new Callback<Exception>() {
                @Override
                public void callback(Exception e) {
                    showToast("回编译失败，请检查是否是一个有效目录。");
                }
            });
            isRecompiling = false;
            ViewUtils.hideLoading(paneRecompile, loading);
            if (isSuccess) {
                // 显示打开输出目录
                btnOpenRecompileOut.setVisible(true);
                showToast("回编译成功");
            } else {
                showToast("回编译失败");
            }
        });
    }

    /**
     * 打开输出目录
     */
    @FXML
    public void openRecompileOut() {
        FileHelper.showInExplorer(new File(Config.getDir(Config.kLastOpenRecompileDir), "dist"));
    }


    //////////////////////////// apk签名 //////////////////////////////
    /**
     * 签名apk目录
     **/
    private List<File> signApkList;
    /**
     * 是否正在签名
     **/
    private boolean isSigning = false;

    /**
     * 选择签名apk
     */
    @FXML
    public void selectSignApk() {
        File lastDir = Config.getDir(Config.kLastOpenApkSignDir);
        // apk列表
        List<File> selectApkList = FileSelecter.create(btnSelectSignApk.getScene().getWindow())
                .setTitle("选择需要签名的apk（可多选）")
                .addFilter("apk")
                .setInitDir(lastDir)
                .showMultiDialog();
        // 显示目录路径
        if (selectApkList != null && !selectApkList.isEmpty()) {
            signApkList = selectApkList;
            textFieldSignApkList.setText(getStringList(signApkList));
            Config.set(Config.kLastOpenApkSignDir, getApkListDir(signApkList).getPath());
        }
    }

    /**
     * 开始签名
     */
    @FXML
    public void startApkSign() {
        KeystoreConfig config = SettingHelper.getKeystoreConfig();
        if (config == null) {
            showToast("keytore配置不正确，请到设置界面确认配置");
            return;
        }

        if (signApkList == null || signApkList.isEmpty()) {
            showToast("请先选择需要签名的apk（可多选）");
            return;
        }

        if (isSigning) {
            showToast("正在签名..");
            return;
        }

        isSigning = true;
        Loading loading = ViewUtils.showLoading(paneApkSign, "正在签名,请稍候...", 15000);

        TaskManager.get().queue(() -> {

            for (File apk : signApkList) {
                String name = apk.getName();
                showToast(name + "正在签名...");
                lastSignedApk = ApkToolPlus.signApk(apk, config);
                showToast(name + "签名完成。");
            }

            isSigning = false;
            ViewUtils.hideLoading(paneApkSign, loading);
            // 显示打开输出目录
            btnOpenApkSignOut.setVisible(true);
        });
    }

    /**
     * 打开输出目录
     */
    @FXML
    public void openApkSignOut() {
        if (FileHelper.exists(lastSignedApk)) {
            FileHelper.showInExplorer(lastSignedApk);
        } else {
            FileHelper.showInExplorer(Config.getDir(Config.kLastOpenApkSignDir));
        }
    }


    //////////////////////////// 通用方法 //////////////////////////////

    /**
     * 获取文件列表的字符串列表
     *
     * @param fileList 文件列表
     * @return 字符串列表
     */
    public String getStringList(List<File> fileList) {
        StringBuilder listBuilder = new StringBuilder();
        for (File file : fileList) {
            listBuilder.append(file.getPath()).append(";");
        }
        return listBuilder.toString();
    }

    /**
     * 获取apk列表所在目录，默认最后一个apk的目录作为返回值，否则返回null
     */
    public File getApkListDir(List<File> apkList) {
        if (apkList != null && apkList.size() > 0) {
            return apkList.get(apkList.size() - 1).getParentFile();
        }
        return null;
    }

}
