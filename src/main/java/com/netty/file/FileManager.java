package com.netty.file;

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
	}

	public void sendFile(String fileNm) {
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
			RandomAccessFile raf = new RandomAccessFile(String.format("%s/%s", Env.getSendPath(), fileNm), "r");
			fileSize = raf.length();
			raf.close();

			oriCut = Math.floorDiv(fileSize, Env.getMaxSendSize());
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
				list.add(new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp(), model)));
			}

			log.warn(String.format("[%s] Thread cnt : %d", fileNm, list.size()));

			for (Thread t : list)
				t.start();
		} catch (Exception e) {
			log.error("FileManager sendFile() Exception : ", e);
		}
	}

}