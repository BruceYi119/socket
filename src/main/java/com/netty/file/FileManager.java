package com.netty.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.netty.config.Env;
import com.netty.event.Event;
import com.netty.socket.SocketClient;
import com.netty.socket.SocketModel;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class FileManager {

	public static final Logger log = LoggerFactory.getLogger(FileManager.class);

	@SuppressWarnings("unused")
	private Env env;

	@EventListener
	public void event(Event event) {
		sendFile("test2.mp4");
		sendFile("test.mp4", Math.multiplyExact(1048576l, 1276));
//		sendFile("test.mp4", Math.multiplyExact(1048576l, 11000));
	}

	public static void sendFile(String fileNm) {
		sendFileProcess(fileNm, Env.getMaxSendSize());
	}

	public static void sendFile(String fileNm, long maxSendSize) {
		sendFileProcess(fileNm, maxSendSize);
	}

	public static void sendFileProcess(String fileNm, long maxSendSize) {
		List<Thread> list = new ArrayList<>();
		SocketModel model = null;
		long filePos = 0;
		long fileSize = 0;
		long sendSize = 0;
		long oriCut = 0;
		long cut = 0;
		long divisionVal = 0;
		long remainder = 0;

		try {
			fileSize = getFileSize(String.format("%s/%s", Env.getSendPath(), fileNm));
			oriCut = Math.floorDiv(fileSize, maxSendSize);
			cut = Math.addExact(oriCut, 1);
			divisionVal = Math.floorDiv(fileSize, cut);
			remainder = Math.floorMod(fileSize, cut);

			// multi Thread
			for (int i = 1; i <= cut; i++) {
				if (i == 1) {
					sendSize = divisionVal;
				} else if (i == cut) {
					filePos = Math.addExact(filePos, sendSize);
					sendSize = Math.addExact(divisionVal, remainder);
				} else {
					filePos = Math.addExact(filePos, sendSize);
					sendSize = divisionVal;
				}

				model = new SocketModel();
				model.setFileNm(fileNm);
				model.setFileSize(sendSize);
				model.setFilePos(filePos);
				model.setThreadIdx(cut == 1 ? 0 : i);
				list.add(new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp(), model)));
			}

			log.warn(String.format("[%s] Thread cnt : %d", fileNm, list.size()));

			for (Thread t : list)
				t.start();
		} catch (Exception e) {
			log.error("FileManager sendFile() Exception : ", e);
		}
	}

	@SuppressWarnings("finally")
	public static long getFileSize(String fileNm) throws Exception {
		long size = 0;
		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(fileNm, "r");
			size = raf.length();
		} catch (FileNotFoundException e) {
			log.error("FileManager getFileSize() FileNotFoundException : ", e);
			throw new FileNotFoundException(e.getMessage());
		} finally {
			if (raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					log.error("FileManager getFileSize() raf.close() IOException : ", e);
				}
			return size;
		}
	}

	@SuppressWarnings("finally")
	public static byte[] fileRead(String fileNm, byte[] bytes, long pos) throws Exception {
		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(fileNm, "r");
			raf.seek(pos);
			raf.read(bytes);
			log.info(String.format("FILE READ BYTES : %d [%s]", bytes.length, fileNm));
		} catch (FileNotFoundException e) {
			bytes = null;
			log.error("FileManager read() FileNotFoundException : ", e);
			throw new FileNotFoundException(e.getMessage());
		} finally {
			if (raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					log.error("FileManager read() raf.close() IOException : ", e);
				}
			return bytes;
		}
	}

	@SuppressWarnings("finally")
	public static boolean fileWrite(String fileNm, byte[] bytes, long pos) throws Exception {
		boolean r = false;

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(fileNm, "rw");
			raf.seek(pos);
			raf.write(bytes);
			r = true;
			log.warn(String.format("FILE WRITE BYTES : %d [%s]", bytes.length, fileNm));
		} catch (FileNotFoundException e) {
			log.error("FileManager write() FileNotFoundException : ", e);
			throw new FileNotFoundException(e.getMessage());
		} finally {
			if (raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					log.error("FileManager write() raf.close() IOException : ", e);
				}
			return r;
		}

	}

	public static void fileDelete(String fileNm) {
		File f = new File(fileNm);

		if (f.exists())
			f.delete();

		log.warn(String.format("FILE DELETE [%s]", fileNm));
	}

}