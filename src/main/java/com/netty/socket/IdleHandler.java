package com.netty.socket;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleStateHandler;

@Sharable
public class IdleHandler extends IdleStateHandler {

//	public static final Logger log = LoggerFactory.getLogger(IdleHandler.class);

	public IdleHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
		super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
	}

//	@Override
//	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//		if (evt instanceof IdleStateEvent) {
//			IdleStateEvent e = (IdleStateEvent) evt;
//			if (e.state() == IdleState.READER_IDLE) {
//				log.info("ClientHandler userEventTriggered() READER_IDLE");
//				ctx.close();
//			} else if (e.state() == IdleState.WRITER_IDLE) {
//				log.info("ClientHandler userEventTriggered() WRITER_IDLE");
//				ctx.close();
//			}
//		}
//	}

}