package com.linchaolong.apktoolplus.utils;

public class StringUtils {

	public static boolean isEmpty(String str){
		return str == null || str.trim().length() == 0;
	}

	public static boolean isEquals(String str, String other){
		return str != null && str.equals(other);
	}
	
}
