package com.netty.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@SuppressWarnings("static-access")
public class LogHandler extends LoggingHandler {

	public LogHandler(LogLevel level) {
		super(level, ByteBufFormat.HEX_DUMP);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isEnabled(internalLevel.WARN)) {
			logger.log(internalLevel.WARN, format(ctx, "ACTIVE"));
		}
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isEnabled(internalLevel.WARN)) {
			logger.log(internalLevel.WARN, format(ctx, "INACTIVE"));
		}
		ctx.fireChannelInactive();
	}

}