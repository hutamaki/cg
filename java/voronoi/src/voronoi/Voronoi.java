package voronoi;

class Coordinates {
	public int x;
	public int y;

	public Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

}

public class Voronoi {

	public final static int HEIGHT = 20;
	public final static int WIDTH = 30;

	private final int[] map = new int[HEIGHT * WIDTH];

	private int manDist(int x, int y, Coordinates player) {
		return Math.abs(player.x - x) + Math.abs(player.y - y);
	}

	private int minIndex(int[] values) {
		int min = Integer.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] < min) {
				min = values[i];
				index = i;
			}
		}
		return index;
	}

	public void voronoi(Coordinates player1, Coordinates player2, Coordinates player3) {
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int[] playerDistance = { manDist(x, y, player1), manDist(x, y, player2), manDist(x, y, player3) };
				map[y * WIDTH + x] = minIndex(playerDistance);
			}
		}
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				strBuf.append(map[y * WIDTH + x]);
				strBuf.append(" ");
			}
			strBuf.append("\n");
		}
		return strBuf.toString();
	}
};
