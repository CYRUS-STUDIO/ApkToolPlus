package com.linchaolong.apktoolplus.module.settings;

import javafx.fxml.Initializable;
import com.linchaolong.apktoolplus.base.Activity;
import com.linchaolong.apktoolplus.core.AppManager;
import com.linchaolong.apktoolplus.Config;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by linchaolong on 2016/3/29.
 */
public class AboutSettingsActivity extends Activity implements Initializable {

    public static final String TAG = AboutSettingsActivity.class.getSimpleName();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void actionJianShu() {
        AppManager.browser(Config.JIANSHU_URL);
    }

    /**
     * 作者博客
     */
    public void actionBlog() {
        AppManager.browser(Config.BLOG_URL);
    }

    /**
     * 作者博客
     */
    public void actionGithub() {
        AppManager.browser(Config.GITHUB_URL);
    }


}
