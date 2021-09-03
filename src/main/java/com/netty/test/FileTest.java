package com.netty.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileTest implements Runnable {

	private long pos;
	private byte[] data;

	public FileTest(long pos, byte[] data) {
		this.pos = pos;
		this.data = data;
	}

	public void fWrite() {
		try {
			RandomAccessFile raf = new RandomAccessFile("D:/file/test.txt", "rw");
			raf.seek(pos);
			raf.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		fWrite();
	}

}