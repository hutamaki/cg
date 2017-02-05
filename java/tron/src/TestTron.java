import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Test;

public class TestTron {

	@Test
	public void testOnePass() {

		Player p = new Player();

		Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();

		Vector<Integer> player1 = new Vector<>();
		player1.add(7);
		player1.add(11);
		player1.add(7);
		player1.add(11);
		v.add(player1);

		Vector<Integer> player2 = new Vector<>();
		player2.add(14);
		player2.add(7);
		player2.add(14);
		player2.add(7);
		v.add(player2);

		String res = p.doTheRun(2, 0, v);
		System.out.println("res> " + res);
		assertEquals("RIGHT", res);
	}

	@Test
	public void testTwoPass() {

		Player p = new Player();

		{
			Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
			Vector<Integer> player1 = new Vector<>();
			player1.add(18);
			player1.add(4);
			player1.add(18);
			player1.add(4);
			v.add(player1);

			Vector<Integer> player2 = new Vector<>();
			player2.add(12);
			player2.add(14);
			player2.add(12);
			player2.add(14);
			v.add(player2);

			String res = p.doTheRun(2, 0, v);
			System.out.println("res> " + res);
			assertEquals("DOWN", res);
		}
		{
			Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
			Vector<Integer> player1 = new Vector<>();
			player1.add(18);
			player1.add(4);
			player1.add(18);
			player1.add(5);
			v.add(player1);

			Vector<Integer> player2 = new Vector<>();
			player2.add(12);
			player2.add(14);
			player2.add(12);
			player2.add(13);
			v.add(player2);

			String res = p.doTheRun(2, 0, v);
			System.out.println("res> " + res);
			assertEquals("DOWN", res);
		}
	}
	
	@Test
	public void testThreePlayerOnePassWrong() {

		Player p = new Player();
		{
			Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
			Vector<Integer> player1 = new Vector<>();
			player1.add(-1);
			player1.add(-1);
			player1.add(-1);
			player1.add(-1);
			v.add(player1);

			Vector<Integer> player2 = new Vector<>();
			player2.add(18);
			player2.add(19);
			player2.add(18);
			player2.add(19);
			v.add(player2);
			
			Vector<Integer> player3 = new Vector<>();
			player3.add(2);
			player3.add(3);
			player3.add(2);
			player3.add(3);
			v.add(player3);

			String res = p.doTheRun(3, 1, v);
			System.out.println("res> " + res);
			assertEquals("RIGHT", res);
		}
	}
	
	public static void addMove(Vector<Integer> player, int x0, int y0, int x1, int y1) {
		player.add(x0);
		player.add(y0);
		player.add(x1);
		player.add(y1);
	}
	
	@Test
	public void testThreePlayersTwoPassWrong() {

		Player p = new Player();
		{
			Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
			Vector<Integer> player1 = new Vector<>();
			addMove(player1, 16, 7, 15, 7);
			v.add(player1);

			Vector<Integer> player2 = new Vector<>();
			addMove(player2, 13, 12, 14, 12);
			v.add(player2);
			
			Vector<Integer> player3 = new Vector<>();
			addMove(player3, 10, 3, 10, 3);
			v.add(player3);

			String res = p.doTheRun(3, 2, v);
			System.out.println("res> " + res);
			assertEquals("RIGHT", res);
		}
		{
			Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
			Vector<Integer> player1 = new Vector<>();
			addMove(player1, 16, 7, 15, 8);
			v.add(player1);

			Vector<Integer> player2 = new Vector<>();
			addMove(player2, 13, 12, 15, 12);
			v.add(player2);
			
			Vector<Integer> player3 = new Vector<>();
			addMove(player3, 10, 3, 11, 3);
			v.add(player3);

			String res = p.doTheRun(3, 2, v);
			System.out.println("res> " + res);
			assertEquals("DOWN", res);
		}
	}
	
	@Test
	public void testThreePlayersNoGoodPath() {

		Player p = new Player();
		{
			Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
			Vector<Integer> player1 = new Vector<>();
			addMove(player1, 9, 5, 8, 5);
			v.add(player1);

			Vector<Integer> player2 = new Vector<>();
			addMove(player2, 8, 14, 8, 14);
			v.add(player2);
			
			Vector<Integer> player3 = new Vector<>();
			addMove(player3, 0, 5, 0, 5);
			v.add(player3);

			String res = p.doTheRun(3, 1, v);
			System.out.println("res> " + res);
			assertEquals("UP", res);
		}
		{
			Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
			Vector<Integer> player1 = new Vector<>();
			addMove(player1, 16, 7, 15, 8);
			v.add(player1);

			Vector<Integer> player2 = new Vector<>();
			addMove(player2, 13, 12, 15, 12);
			v.add(player2);
			
			Vector<Integer> player3 = new Vector<>();
			addMove(player3, 10, 3, 11, 3);
			v.add(player3);

			String res = p.doTheRun(3, 2, v);
			System.out.println("res> " + res);
			assertEquals("DOWN", res);
		}
	}
}
