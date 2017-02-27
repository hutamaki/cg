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
		return production * 1000000 - distance * 1000000 - nbCyborg * 10000;
	}

	@Override
	public boolean equals(Object obj) {
		return ((Factory) obj).entityId == entityId;
	}

	@Override
	public String toString() {
		return String.format("id = %d, nbCyborgs = %d, production = %d", this.entityId, this.nbCyborg, this.production);
	}
}

class Troop {
	int entityId;
	int start;
	int target;
	int number;
	int eta;

	public Troop(int entityId, int start, int target, int number, int eta) {
		this.entityId = entityId;
		this.start = start;
		this.target = target;
		this.number = number;
		this.eta = eta;
	}

	@Override
	public boolean equals(Object obj) {
		return ((Troop) obj).entityId == entityId;
	}
}

enum GamePhases {
	FIRST_STEP, RUN
}

class Cache {
	Factory[] myFactories;
	Factory[] neutralFactories;
	Factory[] theirFactories;

	Army their;
	Army my;
	
	Troop[] myTroops;
	Troop[] theirTroops;

	void update(Player player) {
		myFactories = player.myFactories;
		neutralFactories = player.neutralFactories;
		theirFactories = player.theirFactories;

		my = player.my;
		their = player.their;
	}
}

class Army {
	int nbFactories = 0;
	int totalProduction = 0;
	int availableCyborgs = 0;
	int onTheirWayCyborgs = 0;
	int totalCyborgs = 0;

	void update(Factory[] factories) {
		nbFactories = 0;
		totalProduction = 0;
		availableCyborgs = 0;
		totalCyborgs = 0;

		for (Factory fact : factories) {
			if (fact == null)
				continue;
			nbFactories++;
			totalProduction += fact.production;
			availableCyborgs += fact.nbCyborg;
		}
	}
}

class Player {

	int factoryCount;
	int linkCount;
	int nbTurns = 0;

	int waitTurns = 0;
	int waitBomb = 0;
	Factory done = null;

	int myCount = 0;
	int theirCount = 0;

	Integer[][] map; // factory1, factory2, distance

	Factory[] myFactories;
	Factory[] neutralFactories;
	Factory[] theirFactories;

	Cache cache = new Cache();
	Army my = new Army();
	Army their = new Army();
	
	Troop[] myTroops;
	Troop[] neutralTroops;	
	Troop[] theirTroops;

	GamePhases gamePhase = GamePhases.FIRST_STEP;

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

		// early increase
		/*
		 * for (Factory factory : myFactories) {
		 * 
		 * if (factory == null) continue; // no factory here
		 * 
		 * if (factory.production < 3) { factory.nbCyborg -= 10;
		 * strBuff.append(String.format("; INC %d", factory.entityId)); } }
		 */

		/*
		 * try to conquer neutral factories
		 */
		for (Factory factory : myFactories) {

			if (factory == null)
				continue; // no factory here

			Factory[] neutrals = sortByFitness(neutralFactories, factory);
			for (Factory neutral : neutrals) {

				if (neutral.production == 0)
					continue;

				if ((neutral.nbCyborg + 1) <= factory.nbCyborg) { // first game
																	// phase we
																	// launch it
																	// all
					strBuff.append(
							String.format("; MOVE %d %d %d", factory.entityId, neutral.entityId, neutral.nbCyborg + 1));
					factory.nbCyborg -= (neutral.nbCyborg + 1);
				}
			}
		}

		/*
		 * launch BOMB on enemy factory
		 */
		for (Factory myfactory : myFactories) {

			if (myfactory == null)
				continue;

			Factory[] theirsF = sortByFitness(theirFactories, myfactory);
			for (Factory factory : theirsF) {

				if (factory.production >= 2) {
					strBuff.append(String.format("; BOMB %d %d", myfactory.entityId, factory.entityId));
					waitBomb = getDistance(myfactory, factory);
					done = factory;
				}
				return strBuff.toString();
			}
		}

		return strBuff.toString();
	}

	StringBuffer selectNeutralFactory(StringBuffer strBuff) {

		System.err.println("waitTurns: " + waitTurns);
		if (waitTurns > 0) {
			waitTurns--;
			return strBuff;
		}

		for (Factory factory : myFactories) {

			if (factory == null)
				continue; // no factory here

			if (factory.nbCyborg == 0)
				continue; // no more cyborgs

			Factory[] neutrals = sortByFitness(neutralFactories, factory);
			for (Factory neutral : neutrals) {

				if (neutral.production == 0)
					continue;

				int nbCyborgsToSend = Math.max(neutral.nbCyborg + 1, 2);
				strBuff.append(String.format("; MOVE %d %d %d", factory.entityId, neutral.entityId, nbCyborgsToSend));
				factory.nbCyborg -= nbCyborgsToSend;
				waitTurns = getDistance(factory, neutral);
				return strBuff;
			}
		}
		return strBuff;
	}

	public String selectNearestNotMine() {
		StringBuffer strBuff = new StringBuffer("WAIT");

		/*
		 * At each turn, try to conquer remaining neutral factories
		 */
		strBuff = selectNeutralFactory(strBuff);

		for (Factory factory : myFactories) {

			if (factory == null)
				continue; // no factory here

			if (factory.nbCyborg == 0)
				continue; // no more cyborgs

			System.err.println("fact> " + factory);

			Factory[] theirsF = sortByFitness(theirFactories, factory);
			for (Factory theirFac : theirsF) {

				int theirCyborgs = theirFac.nbCyborg + 2;
				if (theirCyborgs < factory.nbCyborg) {
					int dist = theirCyborgs + 2;// theirFactories[theirs].getTotalFromDistance(distance);
												// // need some distance
												// calculation here
					strBuff.append(String.format("; MOVE %d %d %d", factory.entityId, theirFac.entityId, dist));
					factory.nbCyborg -= theirCyborgs;
				}

				if (factory.nbCyborg == 0) { // no more cyborgs, no need ton
												// continue
					break;
				}
			}
		}

		/*
		 * launch BOMB on enemy factory
		 */

		if (waitBomb <= 0) {
			for (Factory myfactory : myFactories) {

				if (myfactory == null)
					continue;

				Factory[] theirsF = sortByFitness(theirFactories, myfactory);
				for (Factory factory : theirsF) {

					if (done != null && factory.entityId == done.entityId) { // don't
																				// bomb
																				// same
																				// twice
						continue;
					}

					// better bomb better selecte one
					// if (factory.production == 3) { // bomb maximum production
					// entity @NOT TESTED

					strBuff.append(String.format("; BOMB %d %d", myfactory.entityId, factory.entityId));
					waitBomb = getDistance(myfactory, factory);
					done = factory;
					return strBuff.toString();
				}
			}
		} else {
			waitBomb--;
		}
		return strBuff.toString();
	}

	public void Run() {
		nbTurns++;

		System.err.println("turn > " + nbTurns + " <");
		String result;
		if (gamePhase == GamePhases.FIRST_STEP) {
			result = firstGamePhase();
			gamePhase = GamePhases.RUN;
		} else {
			result = selectNearestNotMine();
		}
		System.out.println(result);
	}

	public void updateArmies() {
		my = new Army();
		their = new Army();

		my.update(myFactories);
		their.update(theirFactories);
	}
	
	public void beginParams() {
		cache.update(this);
	}
	
	public void endParams() {
		updateArmies();
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
	
	public void upTroop(int entityId, int playerId, int start, int target, int number, int eta) {		
		Troop troop = new Troop(entityId, start, target, number, eta);
		
	}

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		Player player = new Player(in);

		// game loop
		while (true) {
			
			player.beginParams();
			
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
			player.endParams();
			
			player.Run();
		}
	}
}