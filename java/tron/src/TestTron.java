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

}
