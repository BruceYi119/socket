package com.netty.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class Env {

	private static String clientIp, sendPath, uploadPath, serverPort, clientPort;
	private static Environment env;

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

}