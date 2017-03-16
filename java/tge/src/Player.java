import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Vector;
import java.util.Stack;

/************************************************************************
 * TOOLS A-STAR
 **************************************************************************/

class Node<T> {
	Node<T> parent;
	T value;
	int f, g, h, x;

	public Node(T value) {
		this.value = value;
		f = g = h = x = 0;
		parent = null;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Node<?>) {
			if (((Node<?>) other).value.equals(value)) {
				return true;
			}
		}
		return false;
	}
}

interface Astarable<T> {
	Vector<Node<T>> getSuccessors(Node<T> node);

	boolean isGoal(Node<T> node);

	int getDistance(Node<T> current, Node<T> sucessor);

	int getDistanceFromGoal(Node<T> current);
}

class Astar<T> {

	private int capacity = 0;

	Astar(int initialCapacity) {
		this.capacity = initialCapacity;
	}

	/**
	 * successor is interesting whenever it is not present or if its f is below
	 * than a previous one
	 */
	boolean removeIfPresentAndLess(AbstractCollection<Node<T>> list, Node<T> successor) {
		for (Node<T> element : list) {
			if (element.equals(successor)) {
				if (element.f <= successor.f) {
					return false;
				} else {
					list.remove(element);
					return true;
				}
			}
		}
		return true;
	}

	boolean considerWorthTrying(Node<T> successor, PriorityQueue<Node<T>> openList, Vector<Node<T>> closeList) {
		if (!removeIfPresentAndLess(openList, successor)) {
			return false;
		}
		if (!removeIfPresentAndLess(closeList, successor)) {
			return false;
		}
		return true;
	}

	public Node<T> search(Astarable<T> astarable, T start) {

		Node<T> nodeStart = new Node<>(start);

		PriorityQueue<Node<T>> openList = new PriorityQueue<>(capacity, new Comparator<Node<T>>() {
			@Override
			public int compare(Node<T> arg0, Node<T> arg1) {
				return arg0.f - arg1.f;
			}
		});

		Vector<Node<T>> closeList = new Vector<>();
		openList.add(nodeStart);

		while (!openList.isEmpty()) {
			Node<T> current = openList.poll();
			Vector<Node<T>> successors = astarable.getSuccessors(current);
			for (Node<T> successor : successors) {

				successor.parent = current;
				successor.x = current.x + 1;

				if (astarable.isGoal(successor)) {
					return successor;
				}
				successor.g = current.g + astarable.getDistance(current, successor);
				successor.h = astarable.getDistanceFromGoal(successor);
				successor.f = successor.g + successor.h;

				if (considerWorthTrying(successor, openList, closeList)) {
					openList.add(successor);
				}
			}
			closeList.add(current);
		}
		return null; // means no path
	}

	Vector<T> getPath(Node<T> path) {
		Vector<T> vect = new Vector<>();
		while (path != null) {
			vect.insertElementAt(path.value, 0);
			path = path.parent;
		}
		return vect;
	}
}

/*
 * DFS
 */
class DFS<T> {

	public Node<T> search(Astarable<T> astarable, T start, int nbElements) {

		Node<T> nodeStart = new Node<>(start);
		Stack<Node<T>> stack = new Stack<>();

		int[] visited = new int[nbElements];
		Arrays.fill(visited, -1);

		stack.push(nodeStart);

		while (!stack.isEmpty()) {

			Node<T> current = stack.pop();
			Vector<Node<T>> successors = astarable.getSuccessors(current);

			visited[current.value.hashCode()] = 0;

			for (Node<T> successor : successors) {

				successor.parent = current;
				successor.x = current.x + 1;

				if (astarable.isGoal(successor)) {
					return successor;
				}

				if (visited[successor.value.hashCode()] != -1)
					continue;

				visited[successor.value.hashCode()] = 0;

				stack.push(successor);
			}
		}
		return null; // means no path
	}
}

/**
 * Position on the map
 */
class Unit {
	public int x;
	public int y;
	private int hashCode;
	private int moveIndex;

	Unit(int x, int y, int width) {
		this.x = x;
		this.y = y;

		hashCode = y * width + x;
		moveIndex = y * (width * 4) + (x * 4);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public int MoveIndex() {
		return moveIndex;
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
	public boolean dead;

	public Dragoon(int x, int y, int width, int wallsLeft) {
		super(x, y, width);

		this.wallsLeft = wallsLeft;
		dead = (x == -1 && y == -1);
	}

	public boolean isDead() {
		return dead;
	}
}

/**
 * Represents a Wall
 */
class Wall extends Unit {
	public boolean V = false;
	public boolean H = false;

	public Wall(int x, int y, int width, char orientation) {
		super(x, y, width);

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
class OptimizedBoard {

	public int width;
	public int height;
	public int nb_total;
	public static final int NB_MAX_POSSIBLE_MOVES = 4;
	public static final int NO_MOVE = -1;

	public static final int UP = 0;
	public static final int RIGHT = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;

	private int indexed_width;

	// -1 for not used
	// index is linear coordinates y * (width * 4) + (x * 4)
	// thus we may optimize simulation using a single arraycopy
	// will be tested against non optimized version
	int[] moves;

	// cache to get plot(x,y) from linear index
	Unit[] plots;

	DFS<Unit> dfs = new DFS<>();

	private Unit getOrCreateUnit(int x, int y) {
		int index = y * width + x;
		Unit p = plots[index];
		if (p != null) {
			return p;
		} else {
			p = Factory.createUnit(x, y);
			plots[p.hashCode()] = p;
			return p;
		}
	}

	public Unit getUnit(int x, int y) {
		return plots[y * width + x];
	}

	public Unit getUnit(int hashCode) {
		return plots[hashCode];
	}

	/**
	 * creates maps possible moves
	 */
	public OptimizedBoard(int width, int height) {

		this.width = width;
		this.height = height;
		this.nb_total = width * height;

		this.indexed_width = width * 4;
		moves = new int[height * this.indexed_width];
		Arrays.fill(moves, -1);

		plots = new Unit[height * width];

		for (int y = 0; y < height; y++) {
			int local_index = y * indexed_width; // select line
			for (int x = 0; x < width; x++) {
				int idx = local_index + (x * 4);
				if (y > 0) {
					moves[idx + OptimizedBoard.UP] = getOrCreateUnit(x, y - 1).hashCode(); // UP
				}
				if (x < width - 1) {
					moves[idx + OptimizedBoard.RIGHT] = getOrCreateUnit(x + 1, y).hashCode(); // RIGHT
				}
				if (y < height - 1) {
					moves[idx + OptimizedBoard.DOWN] = getOrCreateUnit(x, y + 1).hashCode(); // DOWN
				}
				if (x > 0) {
					moves[idx + OptimizedBoard.LEFT] = getOrCreateUnit(x - 1, y).hashCode(); // LEFT;
				}
			}
		}
	}

	/*
	 * 0 - UP 1 - RIGHT 2 - LEFT 3 - DOWN
	 */
	private void discardLink(int x, int y, int towards) {
		moves[getUnit(x, y).MoveIndex() + towards] = NO_MOVE;
	}

	public void updateBoard(int x, int y, char orientation) {
		if (orientation == 'V') {

			System.err.format("wall in %d,%d %c\n", x, y, 'V');

			discardLink(x, y, LEFT);
			discardLink(x - 1, y, RIGHT);

			discardLink(x, y + 1, LEFT);
			discardLink(x - 1, y + 1, RIGHT);

		} else {

			System.err.format("wall in %d,%d %c\n", x, y, 'H');

			discardLink(x, y, UP);
			discardLink(x, y - 1, DOWN);

			discardLink(x + 1, y, UP);
			discardLink(x + 1, y - 1, DOWN);
		}
	}

	private boolean isLink(int x, int y, int towards) {
		if (x < 0 || y < 0 || x >= width || y >= height)
			return false;
		return moves[getUnit(x, y).MoveIndex() + towards] != NO_MOVE;
	}

	public boolean isWallPossible(int x, int y, char orientation, Unit current, Astarable<Unit> astarable) {
		System.err.println("isWallPossible: " + x + ", " + y + " o=" + orientation);
		boolean isWallPlacable = true;
		if (orientation == 'V') {
			isWallPlacable = (isLink(x, y, LEFT) && isLink(x, y + 1, LEFT) && isLink(x, y, DOWN));
		} else {
			isWallPlacable = (isLink(x, y, UP) && isLink(x + 1, y, UP) && isLink(x + 1, y, LEFT));
		}

		if (!isWallPlacable) {
			return false;
		}

		int[] backup = backUpMoves();
		updateBoard(x, y, orientation);
		boolean isExit = dfs.search(astarable, current, nb_total) != null;
		System.err.println("isExit: " + isExit);
		moves = backup;

		return isExit;
	}

	private int[] backUpMoves() {
		int[] backup = new int[height * this.indexed_width];
		System.arraycopy(moves, 0, backup, 0, height * this.indexed_width);
		return backup;
	}

	public Vector<Node<Unit>> constructNodes(Unit u) {
		Vector<Node<Unit>> neighbours = new Vector<Node<Unit>>(4);
		int move_index = u.MoveIndex();
		// System.err.println("for: " + u);
		for (int i = 0; i < 4; i++, move_index++) {
			if (moves[move_index] != NO_MOVE) {

				// System.err.println(getUnit(moves[move_index]));
				neighbours.addElement(new Node<>(getUnit(moves[move_index])));
			}
		}
		return neighbours;
	}
}

/**
 * A Star customization
 */
class PathFinding implements Astarable<Unit> {

	Game game;
	int playerId;
	Unit goal;

	public PathFinding(Game game, int playerId) {
		this.game = game;
		this.playerId = playerId;
		
		goal = game.defineGoal(this.playerId);
	}

	@Override
	public Vector<Node<Unit>> getSuccessors(Node<Unit> node) {
		return game.board.constructNodes(node.value);
	}

	@Override
	public boolean isGoal(Node<Unit> node) { // goal hard-coded for every player
		switch (playerId) {
		case 0:
			return node.value.x == (game.board.width - 1);
		case 1:
			return node.value.x == 0;
		case 2:
			return node.value.y == (game.board.height - 1);
		default:
			throw new RuntimeException("goal not defined for playerId !");
		}
	}

	@Override
	public int getDistance(Node<Unit> current, Node<Unit> sucessor) {
		return Math.abs(sucessor.value.y - current.value.y) + Math.abs(sucessor.value.x - current.value.x);
	}

	@Override
	public int getDistanceFromGoal(Node<Unit> current) {
		if (goal.y == -1) { // means it's an X-goal
			return Math.abs(goal.x - current.value.x );
		}
		return Math.abs(goal.y - current.value.y);
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

	OptimizedBoard board;

	HashMap<Integer, Astarable<Unit>> allStarables = new HashMap<>();
	HashMap<Integer, Node<Unit>> pathes = new HashMap<>();
	Astar<Unit> astar;

	public Game(OptimizedBoard board, int nbPlayers, int myId) {
		this.board = board;
		this.nbPlayers = nbPlayers;
		this.myId = myId;

		dragoons = new Dragoon[nbPlayers];

		// create structs for pathfinding
		astar = new Astar<>(board.width * board.height);
		for (int i = 0; i < nbPlayers; i++) {
			allStarables.put(i, new PathFinding(this, i));
		}
	}

	public Unit defineGoal(int meId) {
		Unit lgoal;
		switch (meId) {
		case 0: { // goal is RIGHT
			lgoal = Factory.createUnit(board.width - 1, -1);
		}
			break;
		case 1: { // goal is LEFT
			lgoal = Factory.createUnit(0, -1);
		}
			break;
		case 2: { // goal is BOTTOM
			lgoal = Factory.createUnit(-1, board.height - 1);
		}
			break;
		default:
			throw new RuntimeException("undefined goal, not possible !");
		}
		return lgoal;
	}

	public Dragoon getMe() {
		return dragoons[myId];
	}

	public void setPlayer(int index, Dragoon dragoon) {
		dragoons[index] = dragoon;
		if (dragoon.isDead() && (allStarables.get(index) != null)) { 
			// if player is dead removed it from players map
			allStarables.remove(index);			
			System.err.println("removed: " + index);
		}
	}

	public Dragoon getDragoon(int index) {
		return dragoons[index];
	}

	/**
	 * Sets or activates a new orientation on a wall
	 */
	public void setWall(int x, int y, char orientation) {
		Unit key = board.getUnit(x, y);
		if (!walls.containsKey(key)) { // means no wall yet here
			walls.put(key, Factory.createWall(x, y, orientation));
		} else { // already a wall, just a new orientation
			walls.get(key).enableOrientation(orientation);
		}
		board.updateBoard(x, y, orientation);
	}

	/**
	 * Return a direction given the current position and the next
	 */
	public String getDirection(Unit current, Unit next) {
		if (current.x < next.x)
			return "RIGHT";
		if (current.x > next.x)
			return "LEFT";
		if (current.y < next.y)
			return "DOWN";
		return "UP";
	}

	public void guruMeditation() {

		pathes.clear();
		
		// simple idea: if myPath > other path, i need to stop him using block
		for (Entry<Integer, Astarable<Unit>> entry : allStarables.entrySet()) {
			System.err.format("dragoon %d> => %s\n", entry.getKey(), getDragoon(entry.getKey()));
			Node<Unit> way = astar.search(entry.getValue(), getDragoon(entry.getKey()));
			pathes.put(entry.getKey(), way);
		}

		int myPathLength = pathes.get(myId).x;
		int id = myId;
		int myx = myPathLength;
		
		if (myId != 0) {
		    myx += 1;
		}

		System.err.println("current position: " + getMe());
		/*
		 * Vector<Unit> pathx = astar.getPath(pathes.get(myId)); for (Unit unit
		 * : pathx) { System.err.println("=> " + unit); }
		 */

		// get the player that is in position to win
		for (Entry<Integer, Node<Unit>> entry : pathes.entrySet()) {
			System.err.format("id= %d, length=%d\n", entry.getKey(), entry.getValue().x);
			if (entry.getKey() != myId) {
				if (myx > entry.getValue().x) {
					myx = entry.getValue().x;
					id = entry.getKey();
				}
			}
		}
		
		
		 Vector<Unit> pathy = astar.getPath(pathes.get(2)); 
		 for (Unit unit : pathy) { System.err.println("=> " + unit); }

		if ((id != myId) && (getMe().wallsLeft > 0) && (myx <= 3)) { // find were to put wall if
													// we need to block someone

			System.err.println("attacking :"  + id + "myx: " + myx);
            Astarable<Unit> pathfinding = allStarables.get(id);
            
    		
    		 Vector<Unit> pathx = astar.getPath(pathes.get(id)); 
    		 for (Unit unit : pathx) { System.err.println("=> " + unit); }
    		 
            
			// get his next position and block
			Vector<Unit> hispath = astar.getPath(pathes.get(id));
			System.err.println("his current position: " + getDragoon(id));
			System.err.println("his next position: " + hispath.elementAt(1));

			Unit current = getDragoon(id);
			Unit next = hispath.elementAt(1);

			// walls are up,left, length 2
			// we need to go were he is heading & if we put a V or H wall
			if (walls.get(current) == null) {
				System.err.println("(walls.get(current) == null)");

				if ((next.x - current.x) != 0) { // means opponent go left or right
												
					System.err.println("((next.x - current.x) != 0)");
					if ((next.x - current.x) > 0) { // oponent go rights
						// ok check if there is no walls there
						System.err.println("oponent go rights");
						if (board.isWallPossible(next.x, next.y, 'V', current, pathfinding)) {
							System.out.format("%d %d %c\n", next.x, next.y, 'V');
							return;
						} 
							
						if (board.isWallPossible(next.x, next.y - 1, 'V', current, pathfinding)) {
							System.out.format("%d %d %c\n", next.x, next.y - 1, 'V');
							return;
						} 			
						
						if (board.isWallPossible(next.x + 1, next.y, 'V', current, pathfinding)) {
							System.out.format("%d %d %c\n", next.x + 1, next.y, 'V');
							return;
						} 		
					} else {
						System.err.println("oponent go left");
					
						if (board.isWallPossible(current.x, current.y, 'V', current, pathfinding)) {
							System.out.format("%d %d %c\n", current.x, current.y, 'V');
							return;
						} 						
						if (board.isWallPossible(current.x, current.y - 1, 'V', current, pathfinding)) {
							System.out.format("%d %d %c\n", current.x, current.y - 1, 'V');
							return;
						}		
						
						if (board.isWallPossible(next.x, next.y + 1, 'V', current, pathfinding)) {
							System.out.format("%d %d %c\n", next.x, next.y + 1, 'V');
							return;
						} 							
					}
			
				} else {
					System.err.println("else ((next.x - current.x) != 0)");
					if ((next.y - current.y) > 0) { // oponent go down
						System.err.println("oponent goes down");
						// ok check if there is no walls there
						if (board.isWallPossible(next.x, next.y, 'H', current, pathfinding)) {
							System.out.format("%d %d %c\n", next.x, next.y, 'H');
							return;
						}
						
						if (board.isWallPossible(next.x - 1, next.y, 'H', current, pathfinding)) {
							System.out.format("%d %d %c\n", next.x - 1, next.y, 'H');
							return;
						}

					} else {
						
						System.err.println("oponent goes up");
						if (board.isWallPossible(current.x, current.y, 'H', current, pathfinding)) {
							System.out.format("%d %d %c\n", current.x, current.y, 'H');
							return;
						} 
						
						if (board.isWallPossible(current.x - 1, current.y, 'H', current, pathfinding)) {
							System.out.format("%d %d %c\n", current.x - 1, current.y, 'H');
							return;
						}
					}
				}
				System.err.println("otherwise, walk normally because of no choice");
				Vector<Unit> path = astar.getPath(pathes.get(myId));
				System.out.println(getDirection(getMe(), path.elementAt(1)));
				 

			} else { // otherwise, walk normally
				System.err.println("otherwise, walk normally");
				Vector<Unit> path = astar.getPath(pathes.get(myId));
				System.out.println(getDirection(getMe(), path.elementAt(1)));
			}

		} else {
			System.err.println("otherwise, walk normally because of ahead");
			Vector<Unit> path = astar.getPath(pathes.get(myId));
			System.out.println(getDirection(getMe(), path.elementAt(1)));
		}
	}}

final class Factory {
	static Factory _instance;
	int width;

	private Factory(int width) {
		this.width = width;
	}

	public static void createFactory(int width) {
		if (_instance == null) {
			_instance = new Factory(width);
		}
	}

	static Unit createUnit(int x, int y) {
		return new Unit(x, y, _instance.width);
	}

	static Dragoon createDragon(int x, int y, int wallsLeft) {	
		return new Dragoon(x, y, _instance.width, wallsLeft);
	}

	static Wall createWall(int x, int y, char orientation) {
		return new Wall(x, y, _instance.width, orientation);
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

		Factory.createFactory(w);

		OptimizedBoard board = new OptimizedBoard(w, h); // generate possible
															// moves
		Game game = new Game(board, playerCount, myId);

		// game loop
		while (true) {
			for (int i = 0; i < playerCount; i++) {
				int x = in.nextInt(); // x-coordinate of the player
				int y = in.nextInt(); // y-coordinate of the player
				int wallsLeft = in.nextInt(); // number of walls available for
												// the player
				game.setPlayer(i, Factory.createDragon(x, y, wallsLeft));

			}
			int wallCount = in.nextInt(); // number of walls on the board
			for (int i = 0; i < wallCount; i++) {
				int wallX = in.nextInt(); // x-coordinate of the wall
				int wallY = in.nextInt(); // y-coordinate of the wall
				String wallOrientation = in.next(); // wall orientation ('H' or
													// 'V')
				game.setWall(wallX, wallY, wallOrientation.charAt(0));
			}

			long startTime = System.nanoTime();
			game.guruMeditation();
			long endTime = System.nanoTime();
			System.err.println("compute time = " + (endTime - startTime));
		}
	}
}