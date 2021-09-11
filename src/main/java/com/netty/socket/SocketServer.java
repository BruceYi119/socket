package com.netty.socket;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.netty.config.Env;
import com.netty.file.FileManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;

@Component
public class SocketServer implements ApplicationListener<ApplicationReadyEvent> {

	public static final Logger log = LoggerFactory.getLogger(SocketServer.class);

	private int port;
	private ServerBootstrap sb;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private InitHandler handlers;
	private ApplicationEventPublisher publisher;

	public SocketServer(FileManager fm, ApplicationEventPublisher publisher) {
		ArrayList<ChannelHandler> handlers = new ArrayList<ChannelHandler>();
		handlers.add(new IdleHandler(30, 30, 0));
		handlers.add(new ServerHandler());
		this.handlers = new InitHandler(handlers);
		this.publisher = publisher;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		port = Integer.parseInt(Env.getServerPort());

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();

		sb = new ServerBootstrap();
		sb.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
				.handler(new LogHandler(LogLevel.DEBUG, publisher)).childHandler(handlers);

		try {
			ChannelFuture cf = sb.bind(port).sync();
			cf.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			log.error("SocketServer onApplicationEvent() InterruptedException : ", e);
		} finally {
			sb.config().group().shutdownGracefully();
			sb.config().childGroup().shutdownGracefully();
		}
	}

}