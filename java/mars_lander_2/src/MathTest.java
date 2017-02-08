import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathTest {

	@Test
	public void testDistance() {
		Point p1 = new Point(3,  -1);
		Point p2 = new Point(0, 2);
		assertEquals(4.24, MathTools.getDistance(p1, p2), 0.01);		
	}
	
	@Test
	public void testDistance2() {
		Point p1 = new Point(3,  -1);
		Point p2 = new Point(0, 2);
		assertEquals(18, MathTools.getDistance2(p1, p2));		
	}
	
	@Test
	public void testAngle() {
		Point p1 = new Point(0, 0);
		Point p2 = new Point(0, 1);	
		assertEquals(90, MathTools.getAngleFull(p1,  p2), 0.1);
	}

	@Test
	public void testAngle2() {
		Point p1 = new Point(0, 0);
		Point p2 = new Point(0, -1);	
		assertEquals(270, MathTools.getAngleFull(p1,  p2), 0.1);
	}
	
	@Test
	public void testAngleMarsLander() {
		Point p1 = new Point(2500, 2700);
		Point p2 = new Point(4750, 150);	
		assertEquals(48, MathTools.getAngleMid(p1,  p2), 1);
	}

}
