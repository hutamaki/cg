import static org.junit.Assert.assertEquals;

import java.util.Scanner;

import org.junit.Test;

public class TestSolution {

	@Test
	public void testSolution() {
		int nbSeconds = 4 * 60 + 47;
		int nbPlayers = 0;

		System.err.println("nbSeconds : " + nbSeconds);
		int res = Solution.startAt(nbSeconds, nbPlayers);
		System.err.println("res: " + res);
		assertEquals(31, res);
	}
	
	@Test 
	public void testScannerOneEntry() {
		String input = "1\n4:47";
		
        Scanner in = new Scanner(input);
        int n = in.nextInt();
        in.nextLine();
        in.useDelimiter(":");
        assertEquals(4, in.nextInt());
        assertEquals(47, in.nextInt());		
	}
}
