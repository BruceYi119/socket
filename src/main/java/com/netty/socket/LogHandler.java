package com.netty.socket;

import org.springframework.context.ApplicationEventPublisher;

import com.netty.event.Event;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@SuppressWarnings("static-access")
public class LogHandler extends LoggingHandler {

	private ApplicationEventPublisher publisher;

	public LogHandler(LogLevel level, ApplicationEventPublisher publisher) {
		super(level, ByteBufFormat.HEX_DUMP);
		this.publisher = publisher;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isEnabled(internalLevel.WARN)) {
			logger.log(internalLevel.WARN, format(ctx, "ACTIVE"));
		}
		ctx.fireChannelActive();
//		publisher.publishEvent(new Event(this));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isEnabled(internalLevel.WARN)) {
			logger.log(internalLevel.WARN, format(ctx, "INACTIVE"));
		}
		ctx.fireChannelInactive();
	}

}