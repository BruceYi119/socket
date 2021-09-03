package com.netty.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

@Sharable
public class ClientHandler extends ChannelDuplexHandler {

	public static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private SocketModel model = null;

	private void initModel(ChannelHandlerContext ctx) {
		model = new SocketModel();
//		model.setSb(new StringBuilder());
		model.setPacket(ctx.alloc().buffer());
//		try {
//			model.setRaf(new RandomAccessFile(
//					String.format("%s%s%S", com.netty.config.Env.getSendPath(), File.separator, "test.mp4"), "r"));
//			model.setFileSize(model.getRaf().length());
//			if (model.getFileSize() > model.getFileBufSize())
//				model.setFileBuf(ctx.alloc().buffer(model.getFileBufSize()));
//			byte[] bytes = new byte[model.getFileBufSize()];
//			model.getRaf().read(bytes);
//			model.getFileBuf().writeBytes(bytes);
//			model.setReadSize(model.getFileBufSize());
//			model.getRaf().close();
//		} catch (FileNotFoundException e) {
//			log.error("ServerHandler initModel() FileNotFoundException : ", e);
//		} catch (IOException e) {
//			log.error("ServerHandler initModel() IOException : ", e);
//		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		initModel(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf b = (ByteBuf) msg;

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
		clearModel();
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("ClientHandler exceptionCaught() : ", cause);
		clearModel();
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
		ByteBuf packet = model.getPacket();

		while (packet.readableBytes() >= model.getMsgSize()) {
			byte[] bytes = new byte[100];
			packet.readBytes(bytes).discardReadBytes();
			ByteBuf buf = Unpooled.buffer();
			buf.writeBytes(String.format("%-100s", "C").replaceAll(" ", "C").getBytes());
			ctx.writeAndFlush(buf);
			log.warn(String.format("ClientHandler : [%s]", new String(bytes)));
			break;
		}
	}

	private void clearModel() {
		try {
			if (model != null)
				model.clear();
		} catch (Exception e) {
			log.error("ClientHandler clearModel() Exception : ", e);
		}
	}

}