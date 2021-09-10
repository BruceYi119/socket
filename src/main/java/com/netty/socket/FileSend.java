package com.netty.socket;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.netty.config.Env;
import com.netty.event.Event;

@Component
public class FileSend {

	public static final Logger log = LoggerFactory.getLogger(FileSend.class);

	@EventListener
	public void event(Event event) {
		List<Thread> list = new ArrayList<>();
		String fileNm = "test2.mp4";
		long filePos = 0;
		long sendSize = 0;
		long oriCut = 0;
		long cut = 0;
		long divisionVal = 0;
		long remainder = 0;

		SocketModel model = new SocketModel();

		try {
			RandomAccessFile raf = new RandomAccessFile(String.format("%s/%s", Env.getSendPath(), fileNm), "r");
			model.setFileNm(fileNm);
			model.setFileSize(raf.length());
			raf.close();

			oriCut = Math.floorDiv(model.getFileSize(), model.getMaxSendSize());
			cut = Math.addExact(oriCut, 1);
			divisionVal = Math.floorDiv(model.getFileSize(), cut);
			remainder = Math.floorMod(model.getFileSize(), cut);

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

				list.add(new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp(), sendSize,
						filePos, fileNm)));

			}

			log.warn(String.format("Thread cnt : %d", list.size()));

			// ONE Thread 고정
//			fileNm = "test.mp4";
//			filePos = 0;
//			sendSize = 0;
//			oriCut = 0;
//			cut = 0;
//			divisionVal = 0;
//			remainder = 0;
//			raf = new RandomAccessFile(String.format("%s/%s", Env.getSendPath(), fileNm), "r");
//			model.setFileNm(fileNm);
//			model.setFileSize(raf.length());
//			raf.close();
//			list.add(new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp(),
//					model.getFileSize(), 0, fileNm)));

			for (Thread t : list)
				t.start();
		} catch (Exception e) {
			log.error("Exception : ", e);
		}
	}

}