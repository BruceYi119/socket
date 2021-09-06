package com.netty.test;

public class Test {

	public static void main(String[] args) throws Exception {
		int i = 5;
		boolean read = false;

		while (i >= 5) {
			if (!read) {
				read = true;
			}

			System.out.println(read);
			System.out.println(i);
			break;
		}

		System.out.println("end");

//		List<Thread> threads = new ArrayList<Thread>();
//
//		threads.add(new Thread(new FileTest(0, "TEST".getBytes())));
//		threads.add(new Thread(new FileTest(100, "ABCDEFG".getBytes())));
//		threads.add(new Thread(new FileTest(50, "50".getBytes())));
//		threads.add(new Thread(new FileTest(10, "?????".getBytes())));
//		threads.add(new Thread(new FileTest(150, "#####".getBytes())));
//		threads.add(new Thread(new FileTest(90, "AA+++".getBytes())));
//
//		for (Thread t : threads)
//			t.start();

//		long n = 943718400;
//		// 943718400
//		// 2182407056
//		RandomAccessFile raf = new RandomAccessFile("D:/file/send/test.mp4", "r");
//		long l = 2182407056l;
//		long oriCut = Math.floorDiv(l, n);
//		long cut = Math.addExact(oriCut, 1);
//		long divisionVal = l / cut;
//		long remainder = Math.floorMod(l, cut);
//
//		System.out.println(oriCut);
//		System.out.println(cut);
//		System.out.println(divisionVal);
//		System.out.println(remainder);
//		System.out.println(Math.addExact(Math.multiplyExact(divisionVal, cut), remainder));
//		System.out.println(Math.addExact(Math.multiplyExact(727469018l, 3l), 2l));
//		System.out.println(Math.floorDiv(l, 5120l));
//		System.out.println(Math.multiplyExact(426251l, 5120l));
//		System.out.println(Math.multiplyExact(426251l, 20l));
	}
}