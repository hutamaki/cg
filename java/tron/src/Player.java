import java.util.*;
import java.io.*;
import java.math.*;

class Player {

	private final static int WIDTH = 30;
	private final static int HEIGHT = 20;
	private final int[] maze = new int[WIDTH * HEIGHT];
	private final static String[] MOVES_STR = { "UP", "DOWN", "RIGHT", "LEFT" };

	private final static int UP = 0;
	private final static int DOWN = 1;
	private final static int RIGHT = 2;
	private final static int LEFT = 3;

	private final static int[][] MOVES = { { 0, -1 }, { 0, 1 }, { 0, 1 }, { -1, 0 } };

	private Vector<Integer> moves = new Vector<Integer>();

	private int tomaze(int x, int y) {
		return y * WIDTH + x;
	}

	public Player() {
		Arrays.fill(maze, -1);
	}

	private void possibleMoves(Vector<Integer> race, int x, int y) {
		race.clear();
		for (int i = 0; i < MOVES.length; i++) {
			int[] move = MOVES[i];

			if (x == 0 && move[0] == -1)
				continue; // left not possible
			if (y == 0 && move[1] == -1)
				continue; // up not possible
			if (x == WIDTH - 1 && move[0] == 1)
				continue; // right not possible
			if (y == HEIGHT - 1 && move[1] == 1)
				continue; // down not possible

			if (maze[tomaze(x + move[0], y + move[1])] != -1)
				continue; // place already taken.

			// legit move
			race.add(i);
		}
	}

	public void runer(Scanner in) {
		int N = in.nextInt(); // total number of players (2 to 4).
		int P = in.nextInt(); // your player number (0 to 3).

		System.err.format("P = %d\n", P);

		for (int i = 0; i < N; i++) {
			int X0 = in.nextInt(); // starting X coordinate of lightcycle (or
									// -1)
			int Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or
									// -1)
			int X1 = in.nextInt(); // starting X coordinate of lightcycle (can
									// be the same as X0 if you play before this
									// player)
			int Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can
									// be the same as Y0 if you play before this
									// player)

			if (i != P) {
				maze[tomaze(X1, Y1)] = i;
			} else {
				possibleMoves(moves, X1, Y1);

				Integer move = moves.elementAt(0);
				maze[tomaze(X1 + MOVES[move][0], Y1 + MOVES[move][1])] = i;

				System.out.println(MOVES_STR[move]);
			}
		}

	}

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		Player player = new Player();

		while (true) {
			player.runer(in);
		}
	}
}