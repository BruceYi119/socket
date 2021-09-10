package com.netty.socket;

import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;

@Data
public class SocketModel {

	// 500MB (1MB * 500)
	private long maxSendSize = Math.multiplyExact(1048576l, 500);
	// 10MB
	private int maxfileBufSize = 10485760;
	// 5MB
	private int maxfileReadBufSize = 5242880;
	private boolean msgSizeRead = false;
	private long readSize = 0;
	private ByteBuf packet = null;
	private ByteBuf fileBuf = null;
	private StringBuilder sb = null;
	private RandomAccessFile raf = null;
//	private FileOutputStream fos = null;
//	private BufferedOutputStream bos = null;

	// 전문 구성
	// SI 개시요구[73]
	// RI 개시응답[13]
	// SS 전송[20~5120]
	// SC 확인요구[39]
	// RC 확인응답[39]
	// SE 종료요구[39]
	// RE 종료응답[39]

	// 전문 공통 전문길이[4] + 타입[2] + 응답코드[3] = 9 (요구/응답 공통)
	// 전문길이[4]
	private int msgSize = 0;
	// 타입[2] sender(SI:개시/SS:전송/SC:확인/SE:종료) / recever(RI:개시응답/RC:확인응답/RE:종료응답)
	private String msgType = null;
	// 응답코드[3](000 : 정상, 999 : 오류)
	private String msgRsCode = "000";

	// 개시요구[SI] 공통[9] + 멀티전송[1] + 확인간격[3] + 파일명[20] + 포지션[20] + 파일사이즈[20] = 73
	// 개시응답[RI] 공통[9] + 멀티전송[1] + 확인간격[3] = 13
	// 멀티전송[1](1:일반전송/2>:멀티쓰레드)
	private int msgMulti = 0;
	// 확인간격[3]
	private int msgChkCnt = 30;
	// 파일명[20]
	private String fileNm = null;
	// 포지션[20]
	private long filePos = 0;
	// 파일사이즈[20]
	private long fileSize = 0;

	// 전송[SS] 공통[9] + 보낸시퀀스[10] + 데이터[1~5101] = 19 + [1~5101] = 20~5120
	// 시퀀스[10]
	private int msgSeq = 0;

	// 확인요구[SC] 공통[9] + 보낸시퀀스[10] + 보낸크기[20] = 39
	// 보낸 시퀀스
	private int sendSeq = 0;
	// 보낸 파일 크기
	private long sendSize = 0;

	// 확인응답[RC] 공통[9] + 받은시퀀스[10] + 받은크기[20] = 39
	// 받은 시퀀스
	private int recvSeq = 0;
	// 받은 파일 크기
	private long recvSize = 0;

	// 종료요구[SE] 공통[9] + 보낸시퀀스[10] + 보낸크기[20] = 39
	// 종료요구[RE] 공통[9] + 받은시퀀스[10] + 받은크기[20] = 39

	public void clear() throws Exception {
		msgSize = 0;
		msgType = null;
		msgMulti = 1;
		filePos = 0;
		msgChkCnt = 30;
		msgSizeRead = false;
		msgRsCode = "000";
		msgSeq = 0;
		fileNm = null;
		fileSize = 0;
		readSize = 0;
		sendSize = 0;
		recvSize = 0;
		sendSeq = 0;
		recvSeq = 0;
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
//			if (bos != null)
//				bos.close();
//			if (fos != null)
//				fos.close();
		} catch (Exception e) {
			ReferenceCountUtil.safeRelease(packet);
			ReferenceCountUtil.safeRelease(fileBuf);
			throw new Exception(e);
		}

		packet = null;
		fileBuf = null;
		raf = null;
//		fos = null;
//		bos = null;
	}

}