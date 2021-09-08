package com.netty.socket;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netty.component.Components;
import com.netty.config.Env;

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
	private String fileNm;
	private long filePos, sendSize;

	public ClientHandler(long sendSize, long filePos, String fileNm) {
		this.fileNm = fileNm;
		this.filePos = filePos;
		this.sendSize = sendSize;
	}

	private void initModel(ChannelHandlerContext ctx) {
		model = new SocketModel();
		model.setSb(new StringBuilder());
		model.setPacket(ctx.alloc().buffer());
		model.setFileNm(fileNm);
		model.setFileSize(sendSize);
		model.setFilePos(filePos);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		initModel(ctx);
		ByteBuf buf = Unpooled.buffer();
		model.getSb().append(Components.numPad(73, 4));
		model.getSb().append(String.format("SI0001%s", Components.numPad(model.getMsgChkCnt(), 3)));
//		model.getSb().append(String.format("SI0002%s", Components.numPad(model.getMsgChkCnt(), 3)));
		model.getSb().append(Components.strPad(model.getFileNm(), 20));
		model.getSb().append(Components.numPad(model.getFilePos(), 20));
		model.getSb().append(Components.numPad(model.getFileSize(), 20));
		buf.writeBytes(model.getSb().toString().getBytes());
		ctx.writeAndFlush(buf);

		log.info(String.format("ClientHandler MSG : [%s]", model.getSb().toString()));
		model.getSb().setLength(0);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf b = (ByteBuf) msg;

		log.info(String.format("packet : %s", model.getPacket()));

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
		int idx = 0;
		byte[] bytes = null;
		ByteBuf buf = null;
		List<byte[]> msgList = null;

		while (packet.readableBytes() >= 4) {
			if (packet.readableBytes() < 4)
				break;

			if (!model.isMsgSizeRead()) {
				bytes = new byte[4];
				packet.readBytes(bytes);
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
				case "RI":
					msgList = readMsg(Env.getMsgLen().get(model.getMsgType()), packet);
					for (byte[] b : msgList) {
						if (idx == 0)
							model.setMsgMulti(Integer.parseInt(Components.convertByteToString(b)));
						else if (idx == 1)
							model.setMsgChkCnt(Integer.parseInt(Components.convertByteToString(b)));
						else
							break;

						idx++;
					}

					log.info(String.format("ServerHandler MSG : [%d %s %s %d %d]", Math.addExact(model.getMsgSize(), 4),
							model.getMsgType(), model.getMsgRsCode(), model.getMsgMulti(), model.getMsgChkCnt()));

					bytes = sendFile(model);

					buf = Unpooled.buffer();
					buf.writeBytes(model.getSb().toString().getBytes());
					if (bytes != null)
						buf.writeBytes(bytes);
					ctx.writeAndFlush(buf);

					log.info(String.format("ClientHandler MSG : [%s]", model.getSb().toString()));
					model.getSb().setLength(0);
					break;
				case "RC":
					msgList = readMsg(Env.getMsgLen().get(model.getMsgType()), packet);
					for (byte[] b : msgList) {
						if (idx == 0)
							model.setRecvSeq(Integer.parseInt(Components.convertByteToString(b)));
						else if (idx == 1)
							model.setRecvSize(Long.parseLong(Components.convertByteToString(b)));
						else
							break;

						idx++;
					}

					log.info(String.format("ServerHandler MSG : [%d %s %s %d %d]", Math.addExact(model.getMsgSize(), 4),
							model.getMsgType(), model.getMsgRsCode(), model.getRecvSeq(), model.getRecvSize()));

					if (model.getSendSeq() != model.getRecvSeq())
						model.setMsgRsCode("999");
					if (model.getSendSize() != model.getRecvSize())
						model.setMsgRsCode("999");

					if (model.getMsgRsCode().equals("000")) {
						bytes = sendFile(model);
					} else if (model.getMsgRsCode().equals("999")) {
						bytes = null;
						model.getSb().append(Components.numPad(39, 4));
						model.getSb().append("SE");
						model.getSb().append(model.getMsgRsCode());
						model.getSb().append(Components.numPad(model.getSendSeq(), 10));
						model.getSb().append(Components.numPad(model.getSendSize(), 20));
					}

					buf = Unpooled.buffer();
					buf.writeBytes(model.getSb().toString().getBytes());
					if (bytes != null)
						buf.writeBytes(bytes);
					ctx.writeAndFlush(buf);

					log.info(String.format("ClientHandler MSG : [%s]", model.getSb().toString()));
					model.getSb().setLength(0);
					break;
				case "RE":
					msgList = readMsg(Env.getMsgLen().get(model.getMsgType()), packet);

					for (byte[] b : msgList) {
						if (idx == 0)
							model.setRecvSeq(Integer.parseInt(Components.convertByteToString(b)));
						else if (idx == 1)
							model.setRecvSize(Long.parseLong(Components.convertByteToString(b)));
						else
							break;

						idx++;
					}

					log.info(String.format("ServerHandler MSG : [%d %s %s %d %d]", Math.addExact(model.getMsgSize(), 4),
							model.getMsgType(), model.getMsgRsCode(), model.getRecvSeq(), model.getRecvSize()));
					clearModel();
					ctx.close();
					break;
				default:
					log.warn("ClientHandler process() switch default");
					break;
				}
			}

			idx = 0;
			model.setMsgSizeRead(false);
		}
	}

	private byte[] sendFile(SocketModel model) {
		byte[] bytes = null;
		long size = Math.subtractExact(model.getFileSize(), model.getSendSize());

		if (size > 0) {
			try {
				model.setRaf(new RandomAccessFile(String.format("%s/%s", Env.getSendPath(), model.getFileNm()), "r"));
				bytes = size > 5101 ? new byte[5101] : new byte[(int) size];
				model.getRaf().read(bytes);
				model.getRaf().close();
				model.setRaf(null);
				model.setSendSeq(model.getSendSeq() + 1);
				model.setSendSize(Math.addExact(model.getSendSize(), size));
				model.getSb().append(Components.numPad(Math.addExact(size, 19), 4));
				model.getSb().append("SS000");
				model.getSb().append(Components.numPad(model.getSendSeq(), 10));
			} catch (FileNotFoundException e) {
				log.error("ClientHandler process() FileNotFoundException : ", e);
			} catch (IOException e) {
				log.error("ClientHandler process() IOException : ", e);
			}
		} else {
			model.getSb().append(Components.numPad(39, 4));
			model.getSb().append("SE000");
			model.getSb().append(Components.numPad(model.getSendSeq(), 10));
			model.getSb().append(Components.numPad(model.getSendSize(), 20));
		}

		return bytes;
	}

	private List<byte[]> readMsg(Integer[] lenList, ByteBuf packet) {
		List<byte[]> list = new ArrayList<>();
		byte[] bytes = null;

		for (int len : lenList) {
			bytes = new byte[len];
			packet.readBytes(bytes);
			list.add(bytes);
		}

		return list;
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