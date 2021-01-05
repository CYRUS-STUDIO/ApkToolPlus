package com.linchaolong.apktoolplus.core.packagetool;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileConfigTool {

    private File file;
    private Map<String, String> params = new LinkedHashMap<>();

    public FileConfigTool(File file) {
        this.file = file;
    }

    public FileConfigTool setParam(String key, String value){
        params.put(key, value);
        return this;
    }

    public FileConfigTool setParams(Map<String, String> params){
        this.params = params;
        return this;
    }

    public void save(){
        if (!params.isEmpty()) {
            try {
                String content = FileUtils.readFileToString(file);
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    content = content.replace(entry.getKey(), entry.getValue());
                }
                FileUtils.writeStringToFile(file, content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
