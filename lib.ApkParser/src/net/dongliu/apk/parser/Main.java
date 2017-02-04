package net.dongliu.apk.parser;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Locale;
import net.dongliu.apk.parser.bean.CertificateMeta;


/**
 * Main method for parser apk
 *
 * @author Liu Dong {@literal <im@dongliu.net>}
 */
public class Main {
    public static void main(String[] args) throws IOException, CertificateException {
        //String apkFile = args[0];
        String apkFile = "C:\\Users\\Administrator.USER-20150319TJ\\Desktop\\test\\锁屏.apk";
        try (ApkParser parser = new ApkParser(apkFile)) {
            parser.setPreferredLocale(Locale.getDefault());
            System.out.println(parser.getApkMeta());
            List<CertificateMeta> certList = parser.getCertificateMetaList();
            for (CertificateMeta certificateMeta : certList) {
                System.out.println(certificateMeta);
            }
        }
    }
}
