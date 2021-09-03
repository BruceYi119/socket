package com.netty.socket;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.netty.config.Env;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Component
public class SocketServer implements ApplicationListener<ApplicationStartedEvent> {

	public static final Logger log = LoggerFactory.getLogger(SocketServer.class);

	private int port;
	private ServerBootstrap sb;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private InitHandler handlers;

	public SocketServer(Env env) {
		ArrayList<ChannelHandler> handlers = new ArrayList<ChannelHandler>();
		handlers.add(new ServerHandler());
		this.handlers = new InitHandler(handlers);

		// static Env를 활용해서 설정파일 읽기
		log.warn(Env.getEnv().getProperty("logging.logback.rollingpolicy.file-name-pattern"));
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		port = Integer.parseInt(Env.getServerPort());

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();

		sb = new ServerBootstrap();
		sb.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
				.handler(new LoggingHandler(LogLevel.INFO)).childHandler(this.handlers);

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