package voronoi;

import org.junit.Test;

public class TestVoronoi {

	@Test
	public void test1() {
		//(29,15)(24,19)(25,7)
		Voronoi voronoi = new Voronoi();
		
		Coordinates player1 = new Coordinates(29, 15);
		Coordinates player2 = new Coordinates(24, 19);
		Coordinates player3 = new Coordinates(25, 7);
		
		voronoi.voronoi(player1, player2, player3);
		System.err.println(voronoi.toString());
		
	}
	
	@Test
	public void test2() {
		//(29,15)(24,19)(25,7)
		Voronoi voronoi = new Voronoi();
		
		Coordinates player1 = new Coordinates(29, 15);
		Coordinates player2 = new Coordinates(24, 19);
		Coordinates player3 = new Coordinates(25, 7);
		
		voronoi.voronoi(player1, player2, player3);
		System.err.println(voronoi.toString());
		
	}

}
