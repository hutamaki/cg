import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;

import sun.security.mscapi.KeyStore.MY;

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
	
	@Override
	public String toString() {
		return String.format("troop: id= %d, start= %d, end= %d, number= %d, eta= %d", entityId, start, target, number, eta);
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
	Troop[] neutralTroops;
	Troop[] theirTroops;	

	void update(Player player) {
		myFactories = player.myFactories;
		neutralFactories = player.neutralFactories;
		theirFactories = player.theirFactories;
		
		myTroops = player.myTroops;
		neutralTroops = player.neutralTroops;
		theirTroops = player.theirTroops;

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

	void update(Factory[] factories, Troop[] troops) {
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
		
		for (Troop troop : troops) {
			if (troop == null)
				continue;
			onTheirWayCyborgs += troop.number;			
		}
		
		totalCyborgs = availableCyborgs + onTheirWayCyborgs;
	}
	
	@Override
	public String toString() {
		return String.format("army: nbFactories= %d, totapProduction= %d, available= %d, way= %d, total= %d", 
				nbFactories, totalProduction, availableCyborgs, onTheirWayCyborgs, totalCyborgs);
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

				int nbCyborgsToSend = neutral.nbCyborg + 1;
				strBuff.append(String.format("; MOVE %d %d %d", factory.entityId, neutral.entityId, nbCyborgsToSend));
				factory.nbCyborg -= nbCyborgsToSend;
				waitTurns = getDistance(factory, neutral);
				return strBuff;
			}
		}
		return strBuff;
	}
	
	class Triple<U> implements Comparable<Triple<U>> {
		int rating;
		U item1;
		U item2;

		public Triple(int rating, U item1, U item2) {
			this.rating = rating;
			this.item1 = item1;
			this.item2 = item2;
		}

		@Override
		public int compareTo(Triple<U> value) {
			return value.rating - this.rating;
		}
	}
	
	int troopsOnWayTarget(Factory t, int distance, Troop[] troops) {
		int acc = 0;
		for (Troop troop : troops) {
			if (troop.target == t.entityId && troop.eta <= distance) {
				acc += troop.number;
			}
		}
		return acc;
	}
	
	public String battleFrontStrat() {
		
		Vector<Troop> troopT = new Vector<>();		
		StringBuffer strBuff = new StringBuffer("WAIT");

		/*
		 *  compute sending order ie minimize distance(x, enemy)
		 */
		int myCount = 0;
		Vector<Triple<Factory>> byDistance = new Vector<>(); 
		for (Factory myFact : myFactories) {
			if (myFact == null) continue;
			myCount++;
			for (Factory their : theirFactories) {
				if (their == null) continue;
				int distance = getDistance(myFact, their); // or neutral
				byDistance.add(new Triple<Factory>(distance, myFact, their));				
			}
		}
		Collections.sort(byDistance);
		
		/*
		 * attack from 50% of factories, others are for reloading
		 */
		//int nbFactories_to_attack = myCount / 2; // maybe, maybe not, now blind !
		for (Triple<Factory> triple : byDistance) {			
			
			Factory factory = triple.item1;
			Factory their = triple.item2;
			int distance = triple.rating;
			
			if (factory.nbCyborg == 0)
				continue; // no more cyborgs
			
			// compute how many troops to send
			int nbToSend = their.nbCyborg + their.production * distance + 1;
			nbToSend += troopsOnWayTarget(their, distance, theirTroops); // add moving troops if any
			nbToSend -= troopsOnWayTarget(their, distance, myTroops);
			
			// adjust by already sent
			for (Troop already : troopT) {
				if (already.target == their.entityId) {
					nbToSend -= already.number;
				}
			}
			
			if (factory.nbCyborg > nbToSend) { // not really cold we could check on sum of 3 nearest for instance
				//int realSend = Math.min(factory.nbCyborg, nbToSend);
				troopT.add(new Troop(-1, factory.entityId, their.entityId, nbToSend, distance));
				strBuff.append(String.format("; MOVE %d %d %d", factory.entityId, their.entityId, nbToSend));
			}
		}
		
		/*
		 * compute reinforcement order ie minimize distance (my, reinforcement)
		 */
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
				
				// heuristic here, each factory attacks only one other
				
				}
			}
		}
		
		strBuff = selectNeutralFactory(strBuff);

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

	public String selectNearestNotMine() {
		StringBuffer strBuff = new StringBuffer("WAIT");

		/*
		 * At each turn, try to conquer remaining neutral factories
		 */

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
				
				// heuristic here, each factory attacks only one other
				
				}
			}
		}
		
		strBuff = selectNeutralFactory(strBuff);

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

		my.update(myFactories, myTroops);
		their.update(theirFactories, theirTroops);
	}

	public void beginParams(int entityCount) {
		cache.update(this);
		
		myFactories = new Factory[entityCount];
		theirFactories = new Factory[entityCount];
		neutralFactories = new Factory[entityCount];
		
		myTroops = new Troop[entityCount];
		theirTroops = new Troop[entityCount];
		neutralTroops = new Troop[entityCount];		
	}

	public void endParams() {
		
		updateArmies(); // update troops in player
		
		// once it has been done, we can 
	}

	public void upFactory(int entityId, int playerid, int nbCyborg, int production) {

		
		Factory factory = new Factory(entityId, nbCyborg, production);
		System.err.format("playerid= %d > factory: %s\n", playerid, factory);

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
		System.err.format("playerid= %d > %s\n", playerId, troop);

		myTroops[entityId] = theirTroops[entityId] = neutralTroops[entityId] = null;
		switch (playerId) {
		case 1: {
			myTroops[entityId] = troop;
		}
			break;
		case 0: {
			neutralTroops[entityId] = troop;
		}
			break;
		case -1: {
			theirTroops[entityId] = troop;
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
			player.beginParams(entityCount);			
			for (int i = 0; i < entityCount; i++) {
				int entityId = in.nextInt();
				String entityType = in.next();

				if (entityType.equals("FACTORY")) {
					int arg1 = in.nextInt(); // joueur qui poss�de l'usine : 1
												// pour vous, -1 pour
												// l'adversaire et 0 si neutre
					int arg2 = in.nextInt(); // nombre de cyborgs dans l'usine
					int arg3 = in.nextInt(); // production de l'usine (entre 0
												// et 3)
					int arg4 = in.nextInt();
					int arg5 = in.nextInt();
					player.upFactory(entityId, arg1, arg2, arg3);

				} else { // means "TROOP"
					int arg1 = in.nextInt(); // joueur qui poss�de la troupe : 1
												// pour vous, -1 pour
												// l'adversaire
					int arg2 = in.nextInt(); // identifiant de l'usine de d�part
					int arg3 = in.nextInt(); // identifiant de l'usine d'arriv�e
					int arg4 = in.nextInt(); // nombre de cyborgs au sein de la
												// troupe (entier strictement
												// positif)
					int arg5 = in.nextInt(); // nombre de tours avant d'arriver
												// � destination (entier
												// strictement positif)
					player.upTroop(entityId, arg1, arg2, arg3, arg4, arg5);
				}
			}
			player.endParams();

			player.Run();
		}
	}
}