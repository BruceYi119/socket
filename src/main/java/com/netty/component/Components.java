package com.netty.component;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.stereotype.Component;

@Component
public class Components {

	public static String numPad(int n, int len) {
		return String.format("%0" + len + "d", n);
	}

	public static String numPad(long n, int len) {
		return String.format("%0" + len + "d", n);
	}

	public static String strPad(String str, int len) {
		return String.format("%-" + len + "s", str);
	}

	public static String convertByteToString(byte[] bytes) {
		return new String(bytes);
	}

	@SuppressWarnings("finally")
	public static String convertByteToString(byte[] bytes, String charset) {
		String str = null;

		try {
			str = new String(bytes, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			return str;
		}
	}

	public static String getNowTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar c = Calendar.getInstance();

		return sdf.format(c.getTime());
	}

}