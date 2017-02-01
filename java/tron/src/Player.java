import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

class Coordinates {
	public int x;
	public int y;

	public Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/*
	 * from http://stackoverflow.com/questions/113511/best-implementation-for-
	 * hashcode-method
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result = 128;
		result = 37 * result + this.x;
		result = 37 * result + this.y;
		return result;
	}
	
	public boolean equals(Coordinates coord) {
		return coord.x == x && coord.y == y;
	}
}

class Timeit {
	private static long begin = 0;
	private static long end = 0;

	public static void begin() {
		begin = System.nanoTime();
	}

	public static void end() {
		end = System.nanoTime();
	}

	public static long diff() {
		return end - begin;
	}

	public static void EndAndDisp(String method) {
		System.err.format("timeit: %d\n elapsed in %s", diff(), method);
	}
}

class MazeTree {

	public final static int WIDTH = 30;
	public final static int HEIGHT = 20;

	private HashMap<Coordinates, Coordinates[]> neighbours = new HashMap<>();

	public MazeTree() {

		Timeit.begin();
		for (int i = 0; i < WIDTH; i++) {
			Vector<Coordinates> localneighbours = new Vector<>();
			for (int j = 0; j < HEIGHT; j++) {
				localneighbours.clear();
				if (i < WIDTH - 1) {
					localneighbours.add(new Coordinates(i + 1, j));
				}
				if (i > 0) {
					localneighbours.add(new Coordinates(i - 1, j));
				}
				if (j < HEIGHT - 1) {
					localneighbours.add(new Coordinates(i, j + 1));
				}
				if (j > 0) {
					localneighbours.add(new Coordinates(i, j - 1));
				}
				Coordinates[] tmpArray = new Coordinates[localneighbours.size()];
				localneighbours.toArray(tmpArray);
				neighbours.put(new Coordinates(i, j), tmpArray);
			}
		}
		Timeit.EndAndDisp("MazeTree()");
	}
	
	public Coordinates[] getNeighbours(Coordinates coordinates) {
		return neighbours.get(coordinates);
	}
}

class Player {

	private final MazeTree mazeTree = new MazeTree();
	private final HashMap<Coordinates, Integer> occupied = new HashMap<>();
	private static final Coordinates DEAD_BOT = new Coordinates(-1, -1);	
	
	private Vector<Coordinates> curr_moves = new Vector<>();
	private int myId;
	private int nbPlayers;

	public void score(int nbPlayers) {			
		/*for (int i = 0; i < nbPlayers; i++) {			
		}*/
		Set<Coordinates> graphset = new HashSet<>(occupied.keySet());
		
		// play order
		int[] order = new int[nbPlayers];
		for (int i = 0; i < nbPlayers; i++) {	
			order[i] = (i + myId) % nbPlayers; 
		}		
	}

	public void runer(Scanner in) {

		while (true) {
			nbPlayers = in.nextInt(); // total number of players (2 to 4).
			myId = in.nextInt(); // your player number (0 to 3).			

			// add new occupy coordinates
			curr_moves.clear();
			for (int i = 0; i < nbPlayers; i++) {
				int X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
				int Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
				int X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
				int Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

				occupied.put(new Coordinates(X0, Y0), i);
				occupied.put(new Coordinates(X1, Y1), i);
				curr_moves.add(new Coordinates(X1, Y1));
			}
			
			// remove dead bots from occupied
			for (int i = 0; i < curr_moves.size(); i++) {
				Coordinates cm = curr_moves.elementAt(i);
				if (cm == DEAD_BOT) {
					occupied.values().removeAll(Collections.singleton(i));
				}
			}

			for (int i = 0; i < nbPlayers; i++) {
				Coordinates coord = curr_moves.elementAt(i);				
				if (i == myId) {				
					for (Coordinates neighbour : mazeTree.getNeighbours(coord)) {
						if (!occupied.containsKey(neighbour)) {
							
						}
					}
				}
			}			
		}
	}

	public static void main(String args[]) {
		new Player().runer(new Scanner(System.in));
	}
}