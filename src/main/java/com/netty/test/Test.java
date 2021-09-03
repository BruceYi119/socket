package com.netty.test;

public class Test {

	public static void main(String[] args) throws Exception {
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
		int n = 2147483647;
		long l = 4294967299l;
		long q = l / (Math.floorDiv(l, n) + 1);

		System.out.println(Math.floorMod(l, n));
		System.out.println(Math.floorDiv(l, n) + 1);
		System.out.println(q);
		System.out.println(Math.multiplyExact(q, 3));
		System.out.println(Math.multiplyExact(q, 3));
	}
}