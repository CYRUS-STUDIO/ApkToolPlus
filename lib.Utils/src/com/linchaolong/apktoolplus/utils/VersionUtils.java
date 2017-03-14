package com.linchaolong.apktoolplus.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版本工具类
 *
 * Created by linchaolong on 2016/4/11.
 */
public class VersionUtils {

    /**
     * 分解版本号
     *
     * @param version 版本号，格式：xxx.xxx.xxx...（x代表的是数字）
     * @param count   版本号组数
     * @return  如果匹配失败返回null，否则返回包含count个int类型整数的List
     */
    public static List<Integer> splitVersion(String version, int count){
        if(count <= 0){
            return null;
        }
        StringBuilder patternBuilder = new StringBuilder();
        for(int i=0; i<count; ++i){
            patternBuilder.append("([\\d]+)\\.");
        }
        patternBuilder.delete(patternBuilder.length()-2,patternBuilder.length());
//        Pattern pattern = Pattern.compile("([\\d]+)\\.([\\d]+)\\.([\\d]+)");
        Pattern pattern = Pattern.compile(patternBuilder.toString());
        Matcher matcher = pattern.matcher(version);
        if(matcher.find()){
            List<Integer> splitVersion = new ArrayList<>(matcher.groupCount());
            for(int i=1; i<=matcher.groupCount(); ++i){
                splitVersion.add(Integer.parseInt(matcher.group(i)));
            }
            return splitVersion;
        }
        return null;
    }

    /**
     * 升级版本号
     *
     * @param version 版本号，格式：xx.xxx.xx（x代表的是数字）
     * @return
     */
    public static String upgradeVersion(String version){
        List<Integer> splitVersion = splitVersion(version, 3);
        if(splitVersion != null && splitVersion.size() == 3){
            Integer v3 = splitVersion.get(2);
            Integer v2 = splitVersion.get(1);
            Integer v1 = splitVersion.get(0);
            if(v3 < 99){
                ++v3;
            }else{
                if(v2 < 999){
                    ++v2;
                }else{
                    ++v1;
                }
            }
            return v1+"."+v2+"."+v3;
        }
        return version;
    }

}
