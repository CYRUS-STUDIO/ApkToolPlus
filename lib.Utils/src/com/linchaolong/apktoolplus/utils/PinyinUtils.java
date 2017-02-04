package com.linchaolong.apktoolplus.utils;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;

/**
 * 拼音工具类
 *
 * JPinyin：https://github.com/stuxuhai/jpinyin
 *
 * 使用示例：https://github.com/stuxuhai/jpinyin/tree/master/src/test/java/com/github/stuxuhai/jpinyin
 *
 * Created by linchaolong on 2017/1/29.
 */
public class PinyinUtils {

    public static String shortPinyin(String str) {
        try {
            return PinyinHelper.getShortPinyin(str);
        } catch (PinyinException e) {
            e.printStackTrace();
        }
        return "";
    }

}
