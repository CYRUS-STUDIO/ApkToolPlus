package com.linchaolong.apktoolplus.builder;

import com.linchaolong.apktoolplus.builder.task.UpdateJiaGuTask;
import com.linchaolong.apktoolplus.core.jiagu.JiaGu;

import java.io.File;

/**
 * 更新 apk 加固的库
 *
 * Created by linchaolong on 2017/1/30.
 */
public class UpdateJiaGu {

    public static void main(String[] args) {
        UpdateJiaGuTask task = new UpdateJiaGuTask();

        // 工程目录
        task.setProjectDir(new File("lib.JiaGu"));

        String packagePath = JiaGu.class.getPackage().getName().replaceAll("\\.","/");
        // copy 到 app/src/packagePath/jiagu.zip
        task.addOutFile(new File("./app/src/" + packagePath + "/" + UpdateJiaGuTask.JIAGU_ZIP));

        //  更新工程 out 目录下的加固库
        task.addOutFile(new File("./out/production/app/" + packagePath + "/" + UpdateJiaGuTask.JIAGU_ZIP));

        task.execute();
    }

}
