package com.netty.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class Env {

	private static String clientIp, sendPath, uploadPath, serverPort, clientPort;
	private static Environment env;
	private static Map<String, Integer[]> msgLen = new HashMap<>();

	static {
		msgLen.put("GG", new Integer[] { 2, 3 });
		msgLen.put("SI", new Integer[] { 1, 3, 20, 20, 20 });
		msgLen.put("RI", new Integer[] { 9, 4 });
		msgLen.put("SS", new Integer[] { 10 });
		msgLen.put("SC", new Integer[] { 10, 20 });
		msgLen.put("RC", new Integer[] { 10, 20 });
		msgLen.put("SE", new Integer[] { 10, 20 });
		msgLen.put("RE", new Integer[] { 10, 20 });
	}

	public Env(Environment environment) {
		env = environment;
		initProp();
		initDir();
	}

	private void initDir() {
		Path path = Paths.get(Env.getUploadPath());

		if (!path.toFile().exists())
			path.toFile().mkdirs();

		path = Paths.get(Env.getSendPath());

		if (!path.toFile().exists())
			path.toFile().mkdirs();
	}

	private void initProp() {
		clientIp = env.getProperty("custom.socket.client.ip");
		sendPath = env.getProperty("custom.file.send.path");
		uploadPath = env.getProperty("custom.file.upload.path");
		serverPort = env.getProperty("custom.socket.server.port");
		clientPort = env.getProperty("custom.socket.client.port");
	}

	public static String getClientIp() {
		return clientIp;
	}

	public static String getSendPath() {
		return sendPath;
	}

	public static String getUploadPath() {
		return uploadPath;
	}

	public static String getServerPort() {
		return serverPort;
	}

	public static String getClientPort() {
		return clientPort;
	}

	public static Environment getEnv() {
		return env;
	}

	public static Map<String, Integer[]> getMsgLen() {
		return msgLen;
	}

}