package com.linchaolong.apktoolplus.core;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ApkToolYml {

    private File file;
    private List<String> lines;
    private int targetSdkVersionIndex;

    public ApkToolYml(File file) {
        this.file = file;
        try {
            lines = FileUtils.readLines(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("targetSdkVersion")) {
                targetSdkVersionIndex = i;
                break;
            }
        }
    }

    public ApkToolYml setTargetVersion(String targetVersion) {
        lines.remove(targetSdkVersionIndex);
        lines.add(targetSdkVersionIndex, "  targetSdkVersion: '" + targetVersion + "'");
        return this;
    }

    public void save(){
        try {
            FileUtils.writeLines(file, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
