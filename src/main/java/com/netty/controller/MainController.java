package com.netty.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netty.config.Env;
import com.netty.socket.SocketClient;

@Controller
public class MainController {

	public static final Logger log = LoggerFactory.getLogger(MainController.class);

	@GetMapping("/")
	@ResponseBody
	@SuppressWarnings("finally")
	public Object test() {
		Map<String, String> json = new HashMap<>();
		Thread t = null;

		json.put("result", "fail");

		try {
			json.put("result", "success");
			t = new Thread(new SocketClient(Integer.parseInt(Env.getClientPort()), Env.getClientIp()));
			t.start();
		} catch (Exception e) {
			log.error("Exception : ", e);
		} finally {
			return json;
		}
	}

}