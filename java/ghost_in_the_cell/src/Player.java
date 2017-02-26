import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;

class Factory {
	public int entityId;
	public int nbCyborg;
	public int production;

	Factory(int entityId, int nbCyborg, int production) {
		this.entityId = entityId;
		this.nbCyborg = nbCyborg;
		this.production = production;
	}

	public int getTotalFromDistance(int distance) {
		return this.nbCyborg + distance * this.production;
	}

	public int rate(int distance) {
		return production * 1000000 - distance * 1000000 - nbCyborg * 100000;
	}

	@Override
	public String toString() {
		return String.format("id = %d, nbCyborgs = %d, production = %d", this.entityId, this.nbCyborg, this.production);
	}
}

enum GamePhases {
	FIRST_STEP, RUN
}

class Player {

	int factoryCount;
	int linkCount;

	Integer[][] map; // factory1, factory2, distance

	Factory[] myFactories;
	Factory[] neutralFactories;
	Factory[] theirFactories;

	int myStartingFactory = -1;

	GamePhases gamePhase = GamePhases.FIRST_STEP;
	final static int MAX_NEUTRAL_CYBORG_FIRST_PHASE = 2;

	Player(Scanner in) {
		factoryCount = in.nextInt(); // the number of factories
		linkCount = in.nextInt(); // the number of links between factories

		System.err.println("factoryCount: " + factoryCount);
		System.err.println("linkCount: " + linkCount);

		myFactories = new Factory[factoryCount];
		neutralFactories = new Factory[factoryCount];
		theirFactories = new Factory[factoryCount];

		map = new Integer[factoryCount][];
		// Arrays.fill(map, -1);

		for (int i = 0; i < factoryCount; i++) {
			map[i] = new Integer[factoryCount];
			Arrays.fill(map[i], -1);
		}

		for (int i = 0; i < linkCount; i++) {
			int factory1 = in.nextInt();
			int factory2 = in.nextInt();
			int distance = in.nextInt();
			map[factory1][factory2] = map[factory2][factory1] = distance;

			System.err.format("%d : %d = %d\n", factory1, factory2, distance);
		}
	}

	int getDistance(Factory src, Factory dst) {
		return map[src.entityId][dst.entityId];
	}

	class Tuple<U> implements Comparable<Tuple<U>> {
		int rating;
		U value;

		public Tuple(int rating, U value) {
			this.rating = rating;
			this.value = value;
		}

		@Override
		public int compareTo(Tuple<U> value) {
			return value.rating - this.rating;
		}
	}

	public Factory[] sortByFitness(Factory[] factories, Factory mine) {
		Vector<Tuple<Factory>> values = new Vector<>(factories.length);
		for (int i = 0; i < factories.length; i++) {

			if (factories[i] == null)
				continue;

			values.add(new Tuple<>(factories[i].rate(getDistance(factories[i], mine)), factories[i]));
		}
		Collections.sort(values);

		Factory[] result = new Factory[values.size()];
		for (int i = 0; i < values.size(); i++) {
			Tuple<Factory> tp = values.elementAt(i);
			System.err.format("rating= %d, factory=%s\n", tp.rating, tp.value);
			result[i] = tp.value;
		}
		return result;
	}

	/*
	 * Target max production sites, then min distances
	 */
	public String firstGamePhase() {
		StringBuffer strBuff = new StringBuffer("WAIT");

		/*
		 * try to conqueir neutral factories
		 */
		for (Factory factory : myFactories) {

			if (factory == null)
				continue; // no factory here

			Factory[] neutrals = sortByFitness(neutralFactories, factory);
			Integer[] reachable = map[factory.entityId];

			for (Factory neutral : neutrals) {

				if (reachable[neutral.entityId] == -1)
					continue; // not a link

				if (neutral.production == 0)
					continue;

				// if (neutral.nbCyborg <= MAX_NEUTRAL_CYBORG_FIRST_PHASE) {
				strBuff.append(
						String.format("; MOVE %d %d %d", factory.entityId, neutral.entityId, neutral.nbCyborg + 1));
				// }
			}

			/*
			 * Integer[] reachable = map[factory.entityId];
			 * 
			 * for (int i = 0; i < factoryCount; i++) {
			 * 
			 * if (reachable[i] == -1) continue; // not a link
			 * 
			 * if (neutralFactories[i] != null) // ok neutral factory reachable
			 * { Factory neutral = neutralFactories[i];
			 * 
			 * if (neutral.production == 0) continue;
			 * 
			 * // if (neutral.nbCyborg <= MAX_NEUTRAL_CYBORG_FIRST_PHASE) {
			 * strBuff.append( String.format("; MOVE %d %d %d",
			 * factory.entityId, neutral.entityId, neutral.nbCyborg + 1)); // }
			 * } }
			 */
		}

		/*
		 * launch BOMB on enemy factory
		 */
		for (Factory myfactory : myFactories) {

			if (myfactory == null)
				continue;

			for (Factory factory : theirFactories) {

				if (factory == null)
					continue;

				strBuff.append(String.format("; BOMB %d %d", myfactory.entityId, factory.entityId));
				return strBuff.toString();
			}
		}
		return strBuff.toString();
	}

	StringBuffer selectNeutralFactory(StringBuffer strBuff) {
		for (Factory factory : myFactories) {

			if (factory == null)
				continue; // no factory here

			if (factory.nbCyborg == 0)
				continue; // no more cyborgs

			Factory[] neutrals = sortByFitness(neutralFactories, factory);
			Integer[] reachable = map[factory.entityId];
			
			for (Factory neutral : neutrals) {

				if (reachable[neutral.entityId] == -1)
					continue; // not a link

				if (neutral.production == 0)
					continue;

				strBuff.append(
						String.format("; MOVE %d %d %d", factory.entityId, neutralFactories[neutral.entityId].entityId,
								Math.max(neutralFactories[neutral.entityId].nbCyborg, 2)));
				return strBuff;
			}
		}
		return strBuff;
	}

	public String selectNearestNotMine() {

		StringBuffer strBuff = new StringBuffer("WAIT");
		strBuff = selectNeutralFactory(strBuff);

		/*
		 * At each turn, try to conquiert remaining neutral factories, sending 2
		 */

		for (Factory factory : myFactories) {

			int theirs = -1; // nearest of each
			int distance = Integer.MAX_VALUE;

			if (factory == null)
				continue; // no factory here
			if (factory.nbCyborg == 0)
				continue; // no more cyborgs

			System.err.println("fact> " + factory);

			Integer[] neighbours = map[factory.entityId];
			
			Factory[] theirsF = sortByFitness(theirFactories, factory);
			
			for (Factory theirFac: theirsF) {
				if (neighbours[theirFac.entityId] == -1)
					continue; // not a link
				if (theirFactories[theirFac.entityId] == null)
					continue;
				//if (neighbours[theirFac.entityId] < distance) {
					//distance = neighbours[theirFac.entityId];
					// System.out.format("total from distance: %d, total: %d\n",
					// their.getTotalFromDistance(distance),
					// factory.nbCyborg);
					// if (their.getTotalFromDistance(distance) <=
					// factory.nbCyborg) {
					theirs = theirFac.entityId;
					break ;
					// }
				}			

			if (theirs != -1) {
				System.err.println(theirs);
				int dist = theirFactories[theirs].nbCyborg + 2;// theirFactories[theirs].getTotalFromDistance(distance);
				strBuff.append(String.format("; MOVE %d %d %d", factory.entityId, theirs, dist == 0 ? 2 : dist));
			}
		}

		return strBuff.toString();
	}

	public void Run() {

		String result;
		if (gamePhase == GamePhases.FIRST_STEP) {
			result = firstGamePhase();
			gamePhase = GamePhases.RUN;
		} else {
			result = selectNearestNotMine();
		}
		System.out.println(result);
	}

	public void upFactory(int entityId, int playerid, int nbCyborg, int production) {
		Factory factory = new Factory(entityId, nbCyborg, production);
		System.err.format("playerid= %d > %s\n", playerid, factory);

		myFactories[entityId] = theirFactories[entityId] = neutralFactories[entityId] = null;
		switch (playerid) {
		case 1: {
			myFactories[entityId] = factory;
		}
			break;
		case 0: {
			neutralFactories[entityId] = factory;
		}
			break;
		case -1: {
			theirFactories[entityId] = factory;
		}
			break;
		}
	}

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		Player player = new Player(in);

		// game loop
		while (true) {
			int entityCount = in.nextInt(); // the number of entities (e.g.
											// factories and troops)
			for (int i = 0; i < entityCount; i++) {
				int entityId = in.nextInt();
				String entityType = in.next();

				if (entityType.equals("FACTORY")) {
					int arg1 = in.nextInt(); // joueur qui possède l'usine : 1
												// pour vous, -1 pour
												// l'adversaire et 0 si neutre
					int arg2 = in.nextInt(); // nombre de cyborgs dans l'usine
					int arg3 = in.nextInt(); // production de l'usine (entre 0
												// et 3)
					int arg4 = in.nextInt();
					int arg5 = in.nextInt();
					player.upFactory(entityId, arg1, arg2, arg3);

				} else { // means "TROOP"
					int arg1 = in.nextInt(); // joueur qui possède la troupe : 1
												// pour vous, -1 pour
												// l'adversaire
					int arg2 = in.nextInt(); // identifiant de l'usine de départ
					int arg3 = in.nextInt(); // identifiant de l'usine d'arrivée
					int arg4 = in.nextInt(); // nombre de cyborgs au sein de la
												// troupe (entier strictement
												// positif)
					int arg5 = in.nextInt(); // nombre de tours avant d'arriver
												// à destination (entier
												// strictement positif)
				}
			}
			player.Run();

			// Any valid action, such as "WAIT" or "MOVE source destination
			// cyborgs"
			// System.out.println("WAIT");
		}
	}
}