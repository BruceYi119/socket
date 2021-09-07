package com.netty.socket;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SocketClient implements Runnable {

	public static final Logger log = LoggerFactory.getLogger(SocketClient.class);

	private int port;
	private String host;
	private Bootstrap b;
	private EventLoopGroup group;
	private InitHandler handlers;

	public SocketClient(int port, String host) {
		ArrayList<ChannelHandler> handlers = new ArrayList<ChannelHandler>();
		handlers.add(new IdleHandler(30, 30, 0));
		handlers.add(new ClientHandler());
		this.port = port;
		this.host = host;
		this.handlers = new InitHandler(handlers);

		run();
	}

	@Override
	public void run() {
		b = new Bootstrap();
		group = new NioEventLoopGroup();

		b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000).handler(handlers);

		try {
			ChannelFuture cf = b.connect(host, port).sync();
			cf.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			log.error("SocketClient run() InterruptedException : ", e);
		} finally {
			b.config().group().shutdownGracefully();
		}
	}

	public void stop() {
		b.config().group().shutdownGracefully();
	}

}