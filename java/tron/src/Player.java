import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import timeit.Timeit;

class Coordinates {
	public int x;
	public int y;
	private int hashCode;

	public Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
		hashCode = y * MazeTree.HEIGHT + x;
	}

	public int hashCode() {
		return hashCode;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Coordinates)) {
			return false;
		}
		return hashCode == ((Coordinates) o).hashCode();
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
	private long bestScore = 0;
	private Coordinates bestNeighbour = null;

	public long score(Vector<Vector<Coordinates>> player_starts) {

		// player -> coordinates -> iteration
		HashMap<Integer, HashMap<Coordinates, Integer>> graphs = new HashMap<>();
		for (int i = 0; i < nbPlayers; i++) {
			graphs.put(i, new HashMap<>());
		}

		int turn = 1;
		Set<Coordinates> graphset = new HashSet<>(occupied.keySet());

		// play order
		int[] order = new int[nbPlayers];
		for (int i = 0; i < nbPlayers; i++) {
			order[i] = (i + myId) % nbPlayers;
		}

		while (true) {

			//Timeit.begin();

			boolean Full = true;
			HashMap<Coordinates, Integer> moves = new HashMap<>();

			// play each player
			for (int o = 0; o < nbPlayers; o++) {
				for (Coordinates playerPos : player_starts.elementAt(order[o])) {
					// for all neighbours of player pos
					Coordinates[] neighbours = mazeTree.getNeighbours(playerPos);
					for (Coordinates neighbour : neighbours) {
						// if neighbour not visited by other bots earlier
						if (!graphset.contains(neighbour) || (moves.containsKey(neighbour) && turn == 1)) {
							Full = false;
							graphset.add(neighbour);
							moves.put(neighbour, o);

							// update the graph position while we are on it
							graphs.get(o).put(neighbour, turn);
						}
					}
				}
			}

			// means no more possible moves
			if (Full)
				break;

			for (int i = 0; i < nbPlayers; i++) {
				player_starts.elementAt(i).clear();
			}

			for (Map.Entry<Coordinates, Integer> move : moves.entrySet()) {
				player_starts.elementAt(move.getValue()).add(move.getKey());
			}

			turn++;
			/*System.err.println("it " + turn);
			Timeit.EndAndDisp("score");*/
		}

		// number of tiles
		long numberMyTiles = graphs.get(myId).size();

		// number of tiles for every player
		long sumOfEnemyTiles = 0;
		long sumOfEnemyDistances = 0;
		for (int id = 0; id < nbPlayers; id++) {
			if (id != myId) {
				HashMap<Coordinates, Integer> positions = graphs.get(id);
				sumOfEnemyTiles += positions.size();

				for (int i = 0; i < nbPlayers; i++) {
					if (i != myId) {
						for (Integer turned : positions.values()) {
							sumOfEnemyDistances += turned;
						}
					}
				}
			}
		}
		long score = (long) (numberMyTiles * 1000) + (sumOfEnemyTiles * -10) + sumOfEnemyDistances;
		return score;
	}

	public void runer(Scanner in) {

		Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
		while (true) {
			v.clear();

			int l_nbPlayers = in.nextInt(); // total number of players (2 to 4).
			int l_myId = in.nextInt(); // your player number (0 to 3).

			v.clear();
			for (int i = 0; i < l_nbPlayers; i++) {

				Vector<Integer> playerPos = new Vector<>();
				int X0 = in.nextInt();
				int Y0 = in.nextInt();
				int X1 = in.nextInt();
				int Y1 = in.nextInt();

				playerPos.add(X0);
				playerPos.add(Y0);
				playerPos.add(X1);
				playerPos.add(Y1);

				v.add(playerPos);
			}
			System.out.format("%s\n", doTheRun(l_nbPlayers, l_myId, v));
		}
	}

	public String doTheRun(int nbPlayer, int myId, Vector<Vector<Integer>> v) {

		long score = 0;
		this.bestScore = 0;
		this.nbPlayers = nbPlayer;
		this.myId = myId;

		// add new occupy coordinates
		curr_moves.clear();
		for (int i = 0; i < nbPlayers; i++) {

			Vector<Integer> input = v.elementAt(i);
			int X0 = input.elementAt(0);
			int Y0 = input.elementAt(1);
			int X1 = input.elementAt(2);
			int Y1 = input.elementAt(3);

			occupied.put(new Coordinates(X0, Y0), i);
			occupied.put(new Coordinates(X1, Y1), i);
			curr_moves.add(new Coordinates(X1, Y1));
		}



		Coordinates coord = curr_moves.elementAt(myId);
		for (Coordinates neighbour : mazeTree.getNeighbours(coord)) {
			
			// constructs move structures
			// list of moves by player
			Vector<Vector<Coordinates>> player_starts = initPlayersStart();

			// remove dead bots from occupied
			removeDeadPlayersMoves(player_starts);

			// if next neighbour is free
			if (occupied.containsKey(neighbour))
				continue;

			// we consider it as a new possible starting position
			fixPayerPosition(neighbour, player_starts);

			// and score this path
			score = score(player_starts);
			if (score > bestScore) {
				bestScore = score;
				bestNeighbour = neighbour;
			}
		}
		//System.err.println("bestNeighbour= " + bestNeighbour);
		return display(curr_moves.elementAt(myId), bestNeighbour);
	}

	private void fixPayerPosition(Coordinates position, Vector<Vector<Coordinates>> player_starts) {
		player_starts.elementAt(myId).clear();
		player_starts.elementAt(myId).add(position);
	}

	private Vector<Vector<Coordinates>> initPlayersStart() {
		Vector<Vector<Coordinates>> player_starts = new Vector<Vector<Coordinates>>();
		for (int i = 0; i < nbPlayers; i++) {
			player_starts.add(new Vector<Coordinates>());
			player_starts.elementAt(i).add(curr_moves.elementAt(i));
		}
		return player_starts;
	}

	private void removeDeadPlayersMoves(Vector<Vector<Coordinates>> player_starts) {
		for (int z = 0; z < curr_moves.size(); z++) {
			Coordinates cm = curr_moves.elementAt(z);
			if (cm == DEAD_BOT) {
				player_starts.elementAt(z).clear();
			}
		}
	}

	private String display(Coordinates oldC, Coordinates newC) {
		if (oldC.x < newC.x) {
			return "RIGHT";
		}
		if (oldC.x > newC.x) {
			return "LEFT";
		}
		if (oldC.y < newC.y) {
			return "DOWN";
		}
		return "UP";
	}

	public static void main(String args[]) {
		new Player().runer(new Scanner(System.in));
	}
}