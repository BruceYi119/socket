package com.netty.socket;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

@Sharable
public class ServerHandler extends ChannelDuplexHandler {

	public static final Logger log = LoggerFactory.getLogger(ServerHandler.class);

	private Map<ChannelId, SocketModel> models = new HashMap<>();

	private void initModel(ChannelHandlerContext ctx) {
		SocketModel model = new SocketModel();
//		model.setSb(new StringBuilder());
		model.setPacket(ctx.alloc().buffer());

		models.put(ctx.channel().id(), model);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ByteBuf buf = Unpooled.buffer();
		buf.writeBytes(String.format("%-100s", "C").replaceAll(" ", "C").getBytes());
		ctx.writeAndFlush(buf);
		initModel(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf b = (ByteBuf) msg;
		SocketModel model = models.get(ctx.channel().id());

		try {
			byte[] bytes = new byte[b.readableBytes()];
			b.readBytes(bytes);
			model.getPacket().writeBytes(bytes);
			b.release();
		} catch (Exception e) {
			log.error("ServerHandler channelRead() Exception : ", e);
			ReferenceCountUtil.safeRelease(b);
		}

		process(ctx);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		clearModel(ctx);
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("ServerHandler exceptionCaught() : ", cause);
		clearModel(ctx);
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				log.info("ClientHandler userEventTriggered() READER_IDLE");
				ctx.close();
			} else if (e.state() == IdleState.WRITER_IDLE) {
				log.info("ClientHandler userEventTriggered() WRITER_IDLE");
				ctx.close();
			}
		}
	}

	private void process(ChannelHandlerContext ctx) {
		SocketModel model = models.get(ctx.channel().id());
		ByteBuf packet = model.getPacket();

		while (packet.readableBytes() >= model.getMsgSize()) {
			byte[] bytes = new byte[100];
			packet.readBytes(bytes).discardReadBytes();
			ByteBuf buf = Unpooled.buffer();
			buf.writeBytes(String.format("%-100s", "S").replaceAll(" ", "S").getBytes());
			ctx.writeAndFlush(buf);
			log.warn(String.format("ServerHandler : [%s]", new String(bytes)));
			break;
		}
	}

	private void clearModel(ChannelHandlerContext ctx) {
		SocketModel model = models.get(ctx.channel().id());

		try {
			if (model != null)
				model.clear();
			models.remove(ctx.channel().id());
		} catch (Exception e) {
			log.error("ServerHandler clearModel() Exception : ", e);
		}
	}

}