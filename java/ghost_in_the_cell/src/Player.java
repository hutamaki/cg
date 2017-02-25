import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;

class Factory
{
	public int entityId;
	public  int nbCyborg;
	public int production;
	
	Factory(int entityId, int nbCyborg, int production) {
		this.entityId = entityId;
		this.nbCyborg = nbCyborg;
		this.production = production;
	}
	
	@Override
	public String toString() {
		return String.format("id = %d, nbCyborgs = %d, production = %d", this.entityId, this.nbCyborg, this.production);
	}
}

class Player {
	
	int factoryCount;
	int linkCount;
	
	Integer[][] map; // factory1, factory2, distance
	
	Factory[] myFactories;
	Factory[] theirFactories;
	
	int myStartingFactory = -1;
				
	Player(Scanner in) {
		factoryCount = in.nextInt(); // the number of factories
        linkCount = in.nextInt(); // the number of links between factories
        
        System.err.println("factoryCount: " + factoryCount);
        System.err.println("linkCount: " + linkCount);
        
        myFactories = new Factory[factoryCount];
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
	
	public void selectNearestNotMine() {		
		int minDistance = Integer.MAX_VALUE;
		int mine = -1;
		int theirs = -1;		
		for (Factory factory : myFactories) {			
			if (factory == null) continue ;
			mine = factory.entityId;
			Integer[] neighbours = map[factory.entityId];
			for (int i = 0; i < factoryCount; i++) {
				if (neighbours[i] == -1) continue;
				if (neighbours[i] < minDistance) {
					minDistance = neighbours[i];
					theirs = i;
					mine = factory.entityId;
				}
			}
		}
		System.out.format("MOVE %d %d\n", mine, theirs);
	}
	
	public void upFactory(int entityId, int playerid, int nbCyborg, int production) {
		Factory factory = new Factory(entityId, nbCyborg, production);	
		System.err.println(factory);
		if (playerid == -1) {
			theirFactories[entityId] = factory;
			myFactories[entityId] = null;
		} else {
			
			if (myStartingFactory == -1) {
				myStartingFactory = entityId;
			}
			
			myFactories[entityId] = factory;
			theirFactories[entityId] = null;
		}
	}

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Player player = new Player(in); 

        // game loop
        while (true) {
            int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                
                if (entityType.equals("FACTORY")) {
                	int arg1 = in.nextInt(); // joueur qui possède l'usine : 1 pour vous, -1 pour l'adversaire et 0 si neutre 
                    int arg2 = in.nextInt(); // nombre de cyborgs dans l'usine
                    int arg3 = in.nextInt(); // production de l'usine (entre 0 et 3)
                    int arg4 = in.nextInt();
                    int arg5 = in.nextInt();                    
                    player.upFactory(entityId, arg1, arg2, arg3);
                    
                } else { // means "TROOP"	
                    int arg1 = in.nextInt(); // joueur qui possède la troupe : 1 pour vous, -1 pour l'adversaire
                    int arg2 = in.nextInt(); // identifiant de l'usine de départ
                    int arg3 = in.nextInt(); // identifiant de l'usine d'arrivée
                    int arg4 = in.nextInt(); // nombre de cyborgs au sein de la troupe (entier strictement positif)
                    int arg5 = in.nextInt(); // nombre de tours avant d'arriver à destination (entier strictement positif)
                }
            }
            player.selectNearestNotMine();
            
            // Any valid action, such as "WAIT" or "MOVE source destination cyborgs"
            //System.out.println("WAIT");
        }
    }
} 