package com.netty.socket;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocketModel {

	public static final Logger log = LoggerFactory.getLogger(SocketModel.class);

	private int msgSize = 100;

	private long fileSize = 0;
	private long readSize = 0;
	private long sendSize = 0;
	private long recvSize = 0;
	// 100MB
	private int fileBufSize = 104857600;
	private byte[] data = new byte[5120];
	private ByteBuf packet = null;
	private ByteBuf fileBuf = null;
	private StringBuilder sb = null;
	private RandomAccessFile raf = null;
	private FileOutputStream fos = null;
	private BufferedOutputStream bos = null;

	public void clear() throws Exception {
		fileSize = 0;
		readSize = 0;
		sendSize = 0;
		recvSize = 0;
		data = null;
		sb = null;

		try {
			if (packet != null) {
				while (packet.refCnt() > 0)
					packet.release();
			}
			if (fileBuf != null) {
				while (fileBuf.refCnt() > 0)
					fileBuf.release();
			}
			if (raf != null)
				raf.close();
			if (bos != null)
				bos.close();
			if (fos != null)
				fos.close();
		} catch (Exception e) {
			log.error("SocketModel clear() Exception : ", e);
			ReferenceCountUtil.safeRelease(packet);
			ReferenceCountUtil.safeRelease(fileBuf);
			throw new Exception(e);
		}

		packet = null;
		fileBuf = null;
		raf = null;
		fos = null;
		bos = null;
	}

	@Override
	public String toString() {
		return "SocketModel [fileSize=" + fileSize + ", readSize=" + readSize + ", sendSize=" + sendSize + ", recvSize="
				+ recvSize + ", fileBufSize=" + fileBufSize + ", packet=" + packet + ", fileBuf=" + fileBuf + ", sb="
				+ sb + ", raf=" + raf + ", fos=" + fos + ", bos=" + bos + "]";
	}

}