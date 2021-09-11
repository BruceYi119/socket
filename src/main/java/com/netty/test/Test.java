package com.netty.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Test {

//	public static void main(String[] args) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//		Calendar c = Calendar.getInstance();
//		long l = Math.multiplyExact(1048576l, 2000);
//		long pos = 0;
//		long len = 0;
//		long cnt = 0;
//		byte[] bytes;
//		long remainder = 0;
//
//		System.out.println(String.format("%s : START", sdf.format(c.getTime())));
//
//		RandomAccessFile raf = null;
//		try {
//			raf = new RandomAccessFile("D:/file/send/test.mp4", "r");
//			len = raf.length();
//			raf.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		RandomAccessFile raf2 = null;
//		try {
//			raf2 = new RandomAccessFile("D:/file/upload/test.mp4", "rw");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		pos = 0;
//		remainder = Math.floorMod(len, l);
//		cnt = remainder > 0 ? Math.addExact(Math.floorDiv(len, l), 1) : Math.floorDiv(len, l);
//
//		try {
//			for (int i = 0; i < cnt; i++) {
//				System.out.println(String.valueOf(i));
//				raf = new RandomAccessFile("D:/file/send/test.mp4", "r");
//				if (i + 1 == cnt) {
//					System.out.println("last");
//					bytes = new byte[(int) remainder];
//					raf.seek(pos);
//					raf.read(bytes);
//					raf2.seek(pos);
//					raf2.write(bytes);
//				} else {
//					System.out.println("run");
//					bytes = new byte[(int) l];
//					raf.seek(pos);
//					raf.read(bytes);
//					raf2.seek(pos);
//					raf2.write(bytes);
//					pos = Math.addExact(pos, bytes.length);
//				}
//
//				raf.close();
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		c = Calendar.getInstance();
//		System.out.println(String.format("%s : END", sdf.format(c.getTime())));
//		System.out.println(Math.addExact(0, 5000));
//		System.out.println(Math.addExact(303, 4040));
//		System.out.println(1 % 30);
//
//		int n = 99;
//		long l = 2020;
//
//		System.out.println(Math.subtractExact(n, l));
//
//		String s = "00090";
//		System.out.println(Long.parseLong(s));
//		int i = 5;
//		boolean read = false;
//
//		while (i >= 5) {
//			if (!read) {
//				read = true;
//			}
//
//			System.out.println(read);
//			System.out.println(i);
//			break;
//		}
//
//		System.out.println("end");
//
//		long n = 943718400;
//		RandomAccessFile raf = new RandomAccessFile("D:/file/send/test.mp4", "r");
//		long l = 2182407056l;
//		long oriCut = Math.floorDiv(l, n);
//		long cut = Math.addExact(oriCut, 1);
//		long divisionVal = l / cut;
//		long remainder = Math.floorMod(l, cut);
//
//		System.out.println(oriCut);
//		System.out.println(cut);
//		System.out.println(divisionVal);
//		System.out.println(remainder);
//		System.out.println(Math.addExact(Math.multiplyExact(divisionVal, cut), remainder));
//		System.out.println(Math.addExact(Math.multiplyExact(727469018l, 3l), 2l));
//		System.out.println(Math.floorDiv(l, 5120l));
//		System.out.println(Math.multiplyExact(426251l, 5120l));
//		System.out.println(Math.multiplyExact(426251l, 20l));
//	}

}