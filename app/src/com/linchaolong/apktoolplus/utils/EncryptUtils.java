package com.linchaolong.apktoolplus.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class EncryptUtils {

	private static final int BUFF_SIZE = 1024*1024*5; // 10MB

	public static byte[] encryptXXTEA(byte[] data){
		return XXTEA.encrypt(data,"lcl_apktoolplus");
	}

    /**
     * 已过时，请使用{@link #encryptXXTEA(byte[])}
     *
     * @param data
     * @return
     */
    @Deprecated
	public static byte[] encrypt(byte[] data){
		String key = "linchaolong";
		int keyLen = key.length();
		int size = data.length;
		
		// 加密数据
		 int i = 0;
		 int offset = 0;
		 for(; i<size; ++i, ++offset){
			 if (offset >= keyLen){
				offset = 0;
			}
			 data[i] ^= key.charAt(offset);
		 }
		 
		 return data;
	}

	public static void encrypt(File file, File outFile){

		if (!FileHelper.exists(file)){
			LogUtils.e("file not exists!!! : " + file.getAbsolutePath());
			return;
		}

		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(file);
			out = new FileOutputStream(outFile);
			ByteArrayOutputStream byteOutput;

			try {
				byteOutput = new ByteArrayOutputStream();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				// 文件过大，内存分配失败
				return;
			}

			byte[] buff = new byte[BUFF_SIZE];
			int len;
			while ((len = in.read(buff)) != -1) {
				byteOutput.write(buff, 0, len);
			}

			byte[] encryptData = encryptXXTEA(byteOutput.toByteArray());
			out.write(encryptData);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 释放资源
			IOUtils.close(in);
			IOUtils.close(out);
		}
	}
}
