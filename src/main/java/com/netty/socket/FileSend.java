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

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
@SuppressWarnings("unused")
public class FileSend {

	public static final Logger log = LoggerFactory.getLogger(FileSend.class);

	private Env env;

	@EventListener
	@SuppressWarnings("static-access")
	public void event(Event event) {
		List<Thread> list = new ArrayList<>();
		String fileNm = "test.mp4";
		long filePos = 0;
		long sendSize = 0;
		long oriCut = 0;
		long cut = 0;
		long divisionVal = 0;
		long remainder = 0;
//		long oriCut = Math.floorDiv(l, n);
//		long cut = Math.addExact(oriCut, 1);
//		long divisionVal = l / cut;
//		long remainder = Math.floorMod(l, cut);
		SocketModel model = new SocketModel();

		try {
			RandomAccessFile raf = new RandomAccessFile(String.format("%s/%s", Env.getSendPath(), fileNm), "r");
			model.setFileNm(fileNm);
			model.setFileSize(raf.length());
			raf.close();

			oriCut = Math.floorDiv(model.getFileSize(), model.getMaxSendSize());
			cut = Math.addExact(oriCut, 1);
			divisionVal = model.getFileSize() / cut;
			remainder = Math.floorMod(model.getFileSize(), cut);

//			for (int i = 1; i <= cut; i++) {
//				if (i == 0) {
//					sendSize = model.getMaxSendSize();
//				} else if (i == cut) {
//					sendSize = model.getMaxSendSize();
//					filePos = Math.addExact(filePos, model.getMaxSendSize());
//				} else {
//					sendSize = Math.addExact(model.getMaxSendSize(), remainder);
//					filePos = Math.addExact(filePos, model.getMax	SendSize());
//				}
//
//				list.add(new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp(), sendSize,
//						filePos, fileNm)));
			list.add(new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp(),
					model.getFileSize(), 0, fileNm)));

//			}

			Thread st = new Thread();
			log.warn("event sleep");
			st.sleep(3000);
			log.warn("event start");

			for (Thread t : list)
				t.start();
		} catch (Exception e) {
			log.error("Exception : ", e);
		}
	}

}