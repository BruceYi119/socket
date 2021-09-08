package com.netty.controller;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netty.config.Env;
import com.netty.socket.SocketClient;
import com.netty.socket.SocketModel;

@Controller
public class MainController {

	public static final Logger log = LoggerFactory.getLogger(MainController.class);

	@GetMapping("/")
	@ResponseBody
	@SuppressWarnings("finally")
	public Object test() {
		Map<String, String> json = new HashMap<>();
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

		json.put("result", "fail");

		try {
			RandomAccessFile raf = new RandomAccessFile(
					String.format("%s%s%s", Env.getSendPath(), File.separator, fileNm), "r");

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
//					filePos = Math.addExact(filePos, model.getMaxSendSize());
//				}
//
			json.put("result", "success");
//				list.add(new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp(), sendSize,
//						filePos, fileNm)));
			list.add(new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp(),
					model.getFileSize(), 0, fileNm)));
//			}

			for (Thread t : list)
				t.start();
		} catch (Exception e) {
			log.error("Exception : ", e);
		} finally {
			return json;
		}
	}

}