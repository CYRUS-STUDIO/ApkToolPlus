package com.linchaolong.apktoolplus.core.packagetool;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SmaliTool {

    public File file;
    private List<String> lines;
    private int superIndex;

    public SmaliTool(File file) {
        this.file = file;
        try {
            lines = FileUtils.readLines(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(".super ")) {
                superIndex = i;
                break;
            }
        }
    }

    public SmaliTool setSuper(String superClass){
        lines.remove(superIndex);
        lines.add(superIndex, ".super L"+superClass.replace(".", "/")+";");
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
