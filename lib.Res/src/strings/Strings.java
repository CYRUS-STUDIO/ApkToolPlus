package strings;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 字符串国际化工具类
 *
 * Created by linchaolong on 2016/3/25.
 */
public class Strings {

    /** 字符串资源路径 **/
    private static final String LANG_PATH = Strings.class.getPackage().getName()+".lang";
    private static ResourceBundle lang = ResourceBundle.getBundle(LANG_PATH, Locale.getDefault());

    /**
     *  ResourceBundle中文乱码问题
     *
     *  http://www.360doc.com/content/11/0118/11/4154133_87313484.shtml
     */

    /**
     * 修改本地化设置
     *
     * @param locale
     */
    public static void locale(Locale locale){
        lang = ResourceBundle.getBundle(LANG_PATH, locale);
    }

    /**
     * 获取本地化字符串
     *
     * @param key
     * @return
     */
    public static String get(String key){
        try {
            return new String(lang.getString(key).getBytes("ISO8859-1"),"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
