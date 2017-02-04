package com.linchaolong.apktoolplus.ui;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * 目录选择对话框
 *
 * Created by linchaolong on 2015/9/7.
 */
public class DirectorySelecter {

    private DirectoryChooser dirChooser;
    private Window window;

    private DirectorySelecter(Window window){
        dirChooser = new DirectoryChooser();
        this.window = window;
    }

    public static DirectorySelecter create(Window window){
        return new DirectorySelecter(window);
    }

    public DirectorySelecter setInitDir(File dir){
        dirChooser.setInitialDirectory(dir);
        return this;
    }

    public DirectorySelecter setTitle(String title){
        dirChooser.setTitle(title);
        return this;
    }

    public File showDialog(){
        return dirChooser.showDialog(window);
    }
}
