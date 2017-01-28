package mars_lander_2;

import static org.junit.Assert.*;

import org.junit.Test;

public class MRUA {

	@Test
	public void testCaseAccelerationMRUA() {
		double x0 = 0;
		double v0 = 27.8;
		
		double x = 50;
		double v = 0;
		
		double a = Player.MRUA(x0, v0, x, v);		
		assertEquals(a, -7.7, 0.2);
	}
	
	@Test
	public void testCaseMarsLander() {
		double x0 = 2500;
		double v0 = 3.711;
				
		double x = 4500;
		double v = 0;
		
		
		double a = Player.MRUA(x0, v0, x, v);		
		assertEquals(a, -0.003441025, 0.001);		
	}
}
