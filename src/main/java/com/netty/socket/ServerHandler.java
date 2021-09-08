package com.netty.socket;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netty.component.Components;
import com.netty.config.Env;

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
		model.setSb(new StringBuilder());
		model.setPacket(ctx.alloc().buffer());
		models.put(ctx.channel().id(), model);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		initModel(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf b = (ByteBuf) msg;
		SocketModel model = models.get(ctx.channel().id());

		try {
			b.readBytes(model.getPacket());
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
		int idx = 0;
		byte[] bytes = null;
		List<byte[]> msgList = null;

		while (packet.readableBytes() >= 4) {
			if (packet.readableBytes() < 4)
				break;

			if (!model.isMsgSizeRead()) {
				bytes = new byte[4];
				packet.readBytes(bytes).discardReadBytes();
				model.setMsgSize(Integer.parseInt(new String(bytes)) - 4);
				model.setMsgSizeRead(true);
			}

			if (packet.readableBytes() < model.getMsgSize() && model.isMsgSizeRead())
				break;

			if (packet.readableBytes() >= model.getMsgSize() && model.isMsgSizeRead()) {
				// 공통 읽기
				msgList = readMsg(Env.getMsgLen().get("GG"), packet);
				for (byte[] b : msgList) {
					if (idx == 0)
						model.setMsgType(Components.convertByteToString(b));
					else if (idx == 1)
						model.setMsgRsCode(Components.convertByteToString(b));
					else
						break;

					idx++;
				}

				idx = 0;

				// 타입별 전문 읽기
				switch (model.getMsgType()) {
				case "SI":
					msgList = readMsg(Env.getMsgLen().get(model.getMsgType()), packet);
					for (byte[] b : msgList) {
						if (idx == 0)
							model.setMsgMulti(Integer.parseInt(Components.convertByteToString(b)));
						else if (idx == 1)
							model.setMsgChkCnt(Integer.parseInt(Components.convertByteToString(b)));
						else if (idx == 2)
							model.setFileNm(Components.convertByteToString(b).trim());
						else if (idx == 3)
							model.setFilePos(Long.parseLong(Components.convertByteToString(b)));
						else if (idx == 4)
							model.setFileSize(Long.parseLong(Components.convertByteToString(b)));
						else
							break;

						idx++;
					}

					log.info(String.format("ClientHandler MSG : [%d %s %s %d %d %s %d %d]",
							Math.addExact(model.getMsgSize(), 4), model.getMsgType(), model.getMsgRsCode(),
							model.getMsgMulti(), model.getMsgChkCnt(), model.getFileNm(), model.getFilePos(),
							model.getFileSize()));

					model.getSb().append(Components.numPad(13, 4));
					model.getSb().append(String.format("RI0002%s", Components.numPad(model.getMsgChkCnt(), 3)));

					ByteBuf buf = Unpooled.buffer();
					buf.writeBytes(model.getSb().toString().getBytes());
					ctx.writeAndFlush(buf);

					log.info(String.format("ServerHandler MSG : [%s]", model.getSb().toString()));
					model.getSb().setLength(0);
					break;
				case "SS":
					msgList = readMsg(Env.getMsgLen().get(model.getMsgType()), packet);
					for (byte[] b : msgList) {
						if (idx == 0)
							model.setSendSeq(Integer.parseInt(Components.convertByteToString(b)));
						else
							break;

						idx++;
					}

					log.info(String.format("ClientHandler MSG : [%d %s %s %d]", model.getMsgSize(), model.getMsgType(),
							model.getMsgRsCode(), model.getSendSeq()));

					bytes = new byte[model.getMsgSize() - 19];
					packet.readBytes(bytes).discardReadBytes();

					try {
						model.setRaf(new RandomAccessFile(
								String.format("%s/%s", Env.getUploadPath(), model.getFileNm()), "w"));
						model.getRaf().seek(model.getFilePos());
						model.getRaf().write(bytes);
						model.getRaf().close();
						model.setRaf(null);
						model.setRecvSeq(model.getRecvSeq() + 1);
						model.setFilePos(Math.addExact(model.getFilePos(), bytes.length));
						model.setRecvSize(Math.addExact(model.getRecvSize(), bytes.length));
					} catch (FileNotFoundException e) {
						log.error("ServerHandler process() FileNotFoundException : ", e);
					} catch (IOException e) {
						log.error("ServerHandler process() IOException : ", e);
					}
					break;
				case "SC":
					msgList = readMsg(Env.getMsgLen().get(model.getMsgType()), packet);
					for (byte[] b : msgList) {
						if (idx == 0)
							model.setSendSeq(Integer.parseInt(Components.convertByteToString(b)));
						else if (idx == 1)
							model.setSendSize(Long.parseLong(Components.convertByteToString(b)));
						else
							break;

						idx++;
					}

					log.info(String.format("ClientHandler MSG : [%d %s %s %d %d]", model.getMsgSize(),
							model.getMsgType(), model.getMsgRsCode(), model.getSendSeq(), model.getSendSize()));

					if (model.getRecvSeq() != model.getSendSeq())
						model.setMsgRsCode("999");
					if (model.getRecvSize() != model.getSendSize())
						model.setMsgRsCode("999");

					model.getSb().append(Components.numPad(39, 4));

					if (model.getMsgRsCode().equals("000"))
						model.getSb().append("RC");
					else if (model.getMsgRsCode().equals("999"))
						model.getSb().append("RE");

					model.getSb().append(model.getMsgRsCode());
					model.getSb().append(Components.numPad(model.getRecvSeq(), 10));
					model.getSb().append(Components.numPad(model.getRecvSize(), 20));

					buf = Unpooled.buffer();
					buf.writeBytes(model.getSb().toString().getBytes());
					ctx.writeAndFlush(buf);

					log.info(String.format("ServerHandler MSG : [%s]", model.getSb().toString()));
					model.getSb().setLength(0);
				case "SE":
					msgList = readMsg(Env.getMsgLen().get(model.getMsgType()), packet);

					for (byte[] b : msgList) {
						if (idx == 0)
							model.setSendSeq(Integer.parseInt(Components.convertByteToString(b)));
						else if (idx == 1)
							model.setSendSize(Long.parseLong(Components.convertByteToString(b)));
						else
							break;

						idx++;
					}

					log.info(String.format("ClientHandler MSG : [%d %s %s %d %d]", model.getMsgSize(),
							model.getMsgType(), model.getMsgRsCode(), model.getSendSeq(), model.getSendSize()));

					model.getSb().append(Components.numPad(39, 4));
					model.getSb().append("RE");
					model.getSb().append(model.getMsgRsCode());
					model.getSb().append(Components.numPad(model.getRecvSeq(), 10));
					model.getSb().append(Components.numPad(model.getRecvSize(), 20));

					buf = Unpooled.buffer();
					buf.writeBytes(model.getSb().toString().getBytes());
					ctx.writeAndFlush(buf);

					log.info(String.format("ServerHandler MSG : [%s]", model.getSb().toString()));
					model.getSb().setLength(0);

					clearModel(ctx);
					ctx.close();
					break;
				default:
					log.warn("ServerHandler process() switch default");
					break;
				}
			}

			idx = 0;
			model.setMsgSizeRead(false);
		}
	}

	private List<byte[]> readMsg(Integer[] lenList, ByteBuf packet) {
		List<byte[]> list = new ArrayList<>();
		byte[] bytes = null;

		for (int len : lenList) {
			bytes = new byte[len];
			packet.readBytes(bytes).discardReadBytes();
			list.add(bytes);
		}

		return list;
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