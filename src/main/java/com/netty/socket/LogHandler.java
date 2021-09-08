package com.netty.socket;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Sharable
@SuppressWarnings("static-access")
public class LogHandler extends LoggingHandler {

	public LogHandler(LogLevel level) {
		super(level, ByteBufFormat.HEX_DUMP);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isEnabled(internalLevel.INFO)) {
			logger.log(internalLevel.INFO, format(ctx, "ACTIVE"));
		}
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isEnabled(internalLevel.INFO)) {
			logger.log(internalLevel.INFO, format(ctx, "INACTIVE"));
		}
		ctx.fireChannelInactive();
	}

}