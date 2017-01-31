import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

class Maze {
	public final static int WIDTH = 30;
	public final static int HEIGHT = 20;
	public final static int DEFAULT = -1;

	private final int[] maze = new int[WIDTH * HEIGHT];

	public Maze() {
		Arrays.fill(maze, DEFAULT);
	}

	public boolean isFree(int coordinate) {
		System.err.format("isFree>  %d | p=%d\n", coordinate, maze[coordinate]);
		return maze[coordinate] == DEFAULT;
	}

	public void occupy(int x, int y, int p) {
		occupy(y * WIDTH + x, p);
	}

	public void occupy(int coordinate, int p) {
		maze[coordinate] = p;		
		System.err.format("occupy: %d | p=%d\n", coordinate, maze[coordinate]);
	}
}

class Move {

	private final static String[] MOVES_STR = { "UP", "DOWN", "RIGHT", "LEFT" };

	public int dx;
	public int dy;
	public int id;

	public Move(int dx, int dy, int id) {
		this.dx = dx;
		this.dy = dy;
		this.id = id;
	}

	public int toCoordinates(int x, int y) {
		return (y + dy) * Maze.WIDTH + x + dx;
	}

	public boolean isInBounds(int x, int y) {
		int real_x = x + dx;
		if (real_x < 0 || real_x >= Maze.WIDTH) // left or right not possible
			return false;
		int real_y = y + dy;
		if (real_y < 0 || real_y >= Maze.HEIGHT) // up or down not possible
			return false;
		return true;
	}

	public String toString() {
		return MOVES_STR[id];
	}
}

class Player {

	private final static int UP = 0;
	private final static int DOWN = 1;
	private final static int RIGHT = 2;
	private final static int LEFT = 3;
	private final static Move[] MOVES = { new Move(0, -1, UP), new Move(0, 1, DOWN), new Move(1, 0, RIGHT),
			new Move(-1, 0, LEFT) };

	private Vector<Move> moves = new Vector<Move>();
	private final Maze maze = new Maze();

	private void possibleMoves(Vector<Move> race, int x, int y) {
		race.clear();
		for (int i = 0; i < MOVES.length; i++) {

			Move move = MOVES[i];
			if (!move.isInBounds(x, y)) // clipping: ignoring outside moves
				continue;

			if (!maze.isFree(move.toCoordinates(x, y))) // place already taken.
				continue;

			// legit move
			race.add(move);
		}
	}

	public int getDXY(int X, int Y, Move move) {
		int freeCount = 0;
		while (move.isInBounds(X, Y) && maze.isFree(move.toCoordinates(X, Y))) {
			freeCount++;
			X += move.dx;
			Y += move.dy;
		}
		System.err.format("getdxy: X=%d, Y=%d, dx=%d, dy=%d, freeCount=%d (move=%s)\n", X, Y, move.dx, move.dy, freeCount, move);
		return freeCount;
	}

	public Move selectZoneToMoveFirst(Vector<Move> race, int x, int y) {
		Move selected_move = moves.elementAt(0);
		int max_dxy = 0;
		for (Move possibleMove : race) {

			int tmp = getDXY(x, y, possibleMove);
			if (max_dxy < tmp) {
				max_dxy = tmp;
				selected_move = possibleMove;
			}
		}
		System.err.format("max_dxy= %d, move = %s\n", max_dxy, selected_move);
		return selected_move;
	}

	public void runer(Scanner in) {
		int N = in.nextInt(); // total number of players (2 to 4).
		int P = in.nextInt(); // your player number (0 to 3).

		System.err.format("P = %d\n", P);

		int PX1 = 0, PY1 = 0;
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

			System.err.format("%d> (%d, %d) (%d, %d)\n", i, X0, Y0, X1, Y1);

			if (i != P) {
				maze.occupy(X1, Y1, i);
				maze.occupy(X0, Y0, i);
			} else {
				PX1 = X1;
				PY1 = Y1;
				maze.occupy(X0, Y0, P);
			}
		}

		possibleMoves(moves, PX1, PY1);
		Move move = selectZoneToMoveFirst(moves, PX1, PY1);
		maze.occupy(move.toCoordinates(PX1, PY1), P);
		System.out.println(move);
	}

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		Player player = new Player();

		while (true) {
			player.runer(in);
		}
	}
}