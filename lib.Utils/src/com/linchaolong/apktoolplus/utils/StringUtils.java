package com.linchaolong.apktoolplus.utils;

public class StringUtils {

	public static boolean isEmpty(String str){
		return str == null || "".equals(str.trim());
	}

	public static boolean isEquals(String str, String other){
		return str != null && other != null && str.equals(other);
	}
	
}
