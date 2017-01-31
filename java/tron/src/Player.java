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

	private final static int[][] MOVES = { { 0, -1 }, { 0, 1 }, { 1, 0 }, { -1, 0 } };

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

	public int getDX(int X, int Y, int from_move) {
		int freeCount = 0;
		int dx = MOVES[from_move][0]; // could be 1 or -1
		while (X >= 0 && X < WIDTH && maze[tomaze(X, Y)] == -1) {
			freeCount++;
			X += dx;
			System.err.format("getdx: X=%d, dx=%d", X, dx);
		}
		return freeCount;
	}

	public int getDY(int X, int Y, int from_move) {
		int freeCount = 0;
		int dy = MOVES[from_move][1]; // could be 1 or -1
		while (Y >= 0 && Y < HEIGHT && maze[tomaze(X, Y)] == -1) {
			freeCount++;
			Y += dy;
			System.err.println("getdy");
		}
		return freeCount;
	}

	public int selectZoneToMoveFirst(Vector<Integer> race, int X1, int Y1) {
		int selected_move = race.elementAt(0);
		int max_dxy = 0;
		for (Integer possibleMove : race) {
			int move_x = X1 + MOVES[possibleMove][0];
			int move_y = Y1 + MOVES[possibleMove][1];

			switch (possibleMove) {
			case UP: {
				int tmp = getDY(move_x, move_y, UP);
				if (max_dxy < tmp) {
					max_dxy = tmp;
					selected_move = UP;
				}
			}
				break;
			case DOWN: {
				int tmp = getDY(move_x, move_y, DOWN);
				if (max_dxy < tmp) {
					max_dxy = tmp;
					selected_move = DOWN;
				}
				break;
			}
			case LEFT: {
				int tmp = getDX(move_x, move_y, LEFT);
				if (max_dxy < tmp) {
					max_dxy = tmp;
					selected_move = LEFT;
				}
				break;
			}
			case RIGHT: {
				int tmp = getDX(move_x, move_y, RIGHT);
				if (max_dxy < tmp) {
					max_dxy = tmp;
					selected_move = RIGHT;
				}
				break;
			}
			}

			// check if dx or dy is better than standart going to pre-define
			// path UDRL

		}

		System.err.format("max_dxy= %d, move = %s", max_dxy, MOVES_STR[selected_move]);
		return selected_move;
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

			System.err.format("%d> (%d, %d) (%d, %d)", i, X0, Y0, X1, Y1);

			if (i != P) {
				maze[tomaze(X1, Y1)] = i;
			} else {
				possibleMoves(moves, X1, Y1);

				Integer move = selectZoneToMoveFirst(moves, X1, Y1);
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