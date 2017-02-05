package timeit;

public class Timeit {
	private static long begin = 0;
	private static long end = 0;

	public static void begin() {
		begin = System.currentTimeMillis();
	}

	public static void end() {
		end = System.currentTimeMillis();
	}

	public static long diff() {
		return end - begin;
	}

	public static void EndAndDisp(String method) {
		end();
		System.err.format("score(): %d elapsed\n", diff(), method);
	}
}