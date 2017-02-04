package com.linchaolong.apktoolplus.jiagu.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * 应用签名相关工具类
 *
 * Created by linchaolong on 2016/5/3.
 */
public class SignatureUtils {

    public static final String TAG = SignatureUtils.class.getSimpleName();

    public static void checkSign(Context context) {
        String certMD5 = SignatureUtils.getCertMD5(context);
        try {
            InputStream signIn = context.getAssets().open("sign.bin");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buff = new byte[10240];
            for (int len; (len = signIn.read(buff)) != -1; ) {
                output.write(buff, 0, len);
            }
            String signData = new String(ApkToolPlus.decrypt(output.toByteArray()));
//            Log.e(TAG, "certMD5 = " + certMD5);
//            Log.e(TAG, "sign.bin = " + signData);
            if(!certMD5.equals(signData)){
                // 签名不匹配，不能运行app，防止二次打包
                throw new RuntimeException("signature is not match!!! can't run app.");
            }
        } catch (Exception e) {
            throw new RuntimeException("check sign error!!! " + e.getMessage());
        }
    }

    public static String getCertMD5(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            if (signs.length > 0) {
                return getCertMD5(signs[0].toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCertMD5(byte[] signature) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
            String certMD5 = md5Digest(cert.getEncoded());
            return certMD5;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void parseSignature(byte[] signature) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
            String pubKey = cert.getPublicKey().toString();
            String signNumber = cert.getSerialNumber().toString();

//            System.out.println("signName:" + cert.getSigAlgName());
//            System.out.println("pubKey:" + pubKey);
//            System.out.println("signNumber:" + signNumber);
//            System.out.println("subjectDN:" + cert.getSubjectDN().toString());
//            System.out.println("certMd5 :" + md5Digest(cert.getEncoded()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String md5Digest(byte[] input) throws IOException {
        MessageDigest digest = getDigest("Md5");
        digest.update(input);
        return getHexString(digest.digest());
    }

    public static String getHexString(byte[] digest) {
        BigInteger bi = new BigInteger(1, digest);
        return String.format("%032x", bi);
    }

    public static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}
