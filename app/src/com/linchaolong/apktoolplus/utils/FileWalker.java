package com.linchaolong.apktoolplus.utils;

import java.io.File;

public abstract class FileWalker {

    public void walk(String path) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return;

        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath());
                handle(f);
            } else {
                handle(f);
            }
        }
    }

    public abstract void handle(File file);

}
