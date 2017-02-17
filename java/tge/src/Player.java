import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

/**
 * Position on the map
 */
class Unit {
	public int x;
	public int y;
	private int hashCode;

	Unit(int x, int y) {
		this.x = x;
		this.y = y;

		hashCode = y << 16 + x;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Unit)) {
			return false;
		}
		return hashCode == ((Unit) o).hashCode();
	}

	public String toString() {
		StringBuffer strBuff = new StringBuffer("(");
		strBuff.append(x);
		strBuff.append(", ");
		strBuff.append(y);
		strBuff.append(")");
		return strBuff.toString();
	}
}

/**
 * Represents a Player
 */
class Dragoon extends Unit {
	public int wallsLeft;

	public Dragoon(int x, int y, int wallsLeft) {
		super(x, y);

		this.wallsLeft = wallsLeft;
	}
}

/**
 * Represents a Wall
 */
class Wall extends Unit {
	public boolean V = false;
	public boolean H = false;

	public Wall(int x, int y, char orientation) {
		super(x, y);

		if (orientation == 'V') {
			V = true;
		} else {
			H = true;
		}
	}

	public void enableOrientation(char c) {
		if (c == 'V') {
			V = true;
		} else {
			H = true;
		}
	}
}

/**
 * Board of possible Moves
 */
class Board {
	public int width;
	public int height;

	private HashMap<Unit, Unit[]> moves = new HashMap<>();

	/**
	 * creates all map possible moves
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Vector<Unit> units = new Vector<>();
				if (x > 0) {
					units.add(new Unit(x - 1, y)); // LEFT
				}
				if (x < width - 1) {
					units.add(new Unit(x + 1, y)); // RIGHT
				}
				if (y > 0) {
					units.add(new Unit(x, y - 1)); // DOWN
				}
				if (y < height - 1) {
					units.add(new Unit(x, y + 1));
				}
				Unit[] tmpArray = new Unit[units.size()];
				units.toArray(tmpArray);
				moves.put(new Unit(x, y), tmpArray);
			}
		}
	}

	public Unit[] getMoves(Unit u) {
		return moves.get(u);
	}
}

/**
 * Enter the Game
 */
class Game {
	int nbPlayers;
	int myId;

	Dragoon[] dragoons;
	HashMap<Unit, Wall> walls = new HashMap<>();

	Board board;

	public Game(Board board, int nbPlayers, int myId) {
		this.board = board;
		this.nbPlayers = nbPlayers;
		this.myId = myId;

		dragoons = new Dragoon[nbPlayers];
	}

	public Dragoon getMe() {
		return dragoons[myId];
	}

	public void setPlayer(int index, Dragoon dragoon) {
		dragoons[index] = dragoon;
	}

	/**
	 * Sets or activates a new orientation on a wall
	 */
	public void setWall(int x, int y, char orientation) {
		Unit key = new Unit(x, y);
		if (!walls.containsKey(key)) { // means no wall yet here
			walls.put(key, new Wall(x, y, orientation));
		} else { // already a wall, just a new orientation
			walls.get(key).enableOrientation(orientation);
		}
	}

	/**
	 * returns if a wall blocks the way, given supposed walls coordinates there
	 * are two coordinates, since walls are 2 cases-length
	 */
	public boolean isBlocked(int a, int b, int c, int d, boolean V) {
		Wall[] wallsToCheck = { walls.get(new Unit(a, b)), walls.get(new Unit(c, d)) };
		for (Wall wall : wallsToCheck) {
			if (wall == null) { // there is a wall
				continue;
			}
			if (V) { // check if vertical & wall is vertical
				if (wall.V) {
					return true;
				}
			} else {
				if (wall.H) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the movement from from to to is valid, false otherwise
	 */
	public boolean isNextMoveValid(Unit from, Unit to) {
		int dx = to.x - from.x;
		int dy = to.y - from.y;

		if (dx == 0) {
			if (dy < 0) { // UP
				// H (x,y)(x-1,y)
				return isBlocked(from.x, from.y, from.x - 1, from.y, false); // false
																				// means
																				// H
			} else {
				// H (x,y-1)(x-1,y-1)
				return isBlocked(from.x, from.y - 1, from.x - 1, from.y - 1, false);
			}
		} else { // implies dy == 0
			if (dx < 0) { // LEFT
				// V (x+1,y)(x+1,y-1)
				return isBlocked(from.x + 1, from.y, from.x + 1, from.y - 1, true);
			} else { // RIGHT
				// V (x,y)(x,y-1)
				return isBlocked(from.x, from.y, from.x, from.y - 1, true);
			}
		}
	}
}

class Player {

	private static Scanner in;

	public static void main(String args[]) {
		in = new Scanner(System.in);
		int w = in.nextInt(); // width of the board
		int h = in.nextInt(); // height of the board
		int playerCount = in.nextInt(); // number of players (2 or 3)
		int myId = in.nextInt(); // id of my player (0 = 1st player, 1 = 2nd
									// player, ...)

		Board board = new Board(w, h); // generate possible moves
		Game game = new Game(board, playerCount, myId);

		// game loop
		while (true) {
			for (int i = 0; i < playerCount; i++) {
				int x = in.nextInt(); // x-coordinate of the player
				int y = in.nextInt(); // y-coordinate of the player
				int wallsLeft = in.nextInt(); // number of walls available for
												// the player
				game.setPlayer(i, new Dragoon(x, y, wallsLeft));

			}
			int wallCount = in.nextInt(); // number of walls on the board
			for (int i = 0; i < wallCount; i++) {
				int wallX = in.nextInt(); // x-coordinate of the wall
				int wallY = in.nextInt(); // y-coordinate of the wall
				String wallOrientation = in.next(); // wall orientation ('H' or
													// 'V')
				game.setWall(wallX, wallY, wallOrientation.charAt(0));
			}

			Dragoon me = game.getMe();
			Unit[] possibleMoves = game.board.getMoves(me);

			System.err.format("possible moves: %d\n", possibleMoves.length);
			for (Unit move : possibleMoves) {
				System.err.format("%s => %b\n", move, game.isNextMoveValid(me, move));
			}
			if (game.myId == 0) {
				System.out.println("RIGHT");
			} else
				System.out.println("LEFT");

		}
	}
}