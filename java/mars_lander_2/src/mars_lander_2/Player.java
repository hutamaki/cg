package mars_lander_2;

import java.util.Scanner;

class Player {
	
	class Point	{
		public int x;
		public int y;
	}
	
	class Ship 	{
		double r;
		int throttle;
		Point pos;
	}
	
	public boolean isOnSegment(Point p, Point a, Point b) {
		return (p.x <= Math.max(a.x, b.x) && p.x >= Math.min(a.x, b.x) &&
				p.y >= Math.min(a.y, b.y) && p.y <= Math.max(a.y, b.y));
	}
	
	public int slope(Point a, Point b) {
		int diff = b.y - a.y;
		if (diff != 0) {
			int diff2 = b.x - a.x;
			if (diff2 != 0) {
				return diff / diff2;
			}
		}
		return 0;
	}
	
	public int orientation(Point a, Point b, Point p) {
		return slope(b, p) - slope(a, b);
	}
	
	public boolean intersect(Point a, Point b, Point p, Point q) {
		int oa = orientation(a, b, p);
		int ob = orientation(a, b, q);
		
		int oc = orientation(p, q, a);
		int od = orientation(p, q, b);
		
		if (oa != ob && oc != od) {
			System.err.println("onoz");
			return true;
		}
		
		if (oa == 0 && isOnSegment(p, a, b)) {
			System.err.println("oa == 0 && isOnSegment");
			return true;
		}
		if (ob == 0 && isOnSegment(q, a, b)) {
			System.err.println("ob == 0 && isOnSegment");
			return true;
		}
		if (oc == 0 && isOnSegment(a, p, q)) {
			System.err.println("oc == 0 && isOnSegment");
			return true;
		}
		if (od == 0 && isOnSegment(b, p, q)) {
			System.err.println("od == 0 && isOnSegment");
			return true;
		}
		return false;
	}
	
	/*
	 * @param Array of (x,y) points representing the mars surface
	 * @throws Exception if no landing zone is detected
	 * @return index of the landing zone 
	 */
	public int landingZone(Point[] surface) throws Exception {
		for (int i = 1; i < surface.length; i++) {
			if ((surface[i - 1].y - surface[i].y) == 0) {
				return i;
			}
		}
		throw new Exception("no landing zone detected !");
	}
	
	/*
	 * @param Array of (x,y) points representing the mars surface
	 * @return nbCollisions = 1 if there is no obstacle between landing zone & position
	 */
	public int collisions(Point[] surface, Point landing, Point position) {
		int nbCollisions = 0;
		for (int i = 1; i < surface.length; i++) {
			if (intersect(position, landing, surface[i - 1], surface[i])) {
				nbCollisions++;
			}
		}
		return nbCollisions;
	}
	
	/*
	 * Computes the angle of the module depending of power & speed
	 * @param current module speed
	 * @param current module power
	 * @return wanted module angle
	 */
	public double angle(double vspeed, double power) {
		return Math.acos(power / vspeed) * 180.0 * Math.PI;
	}

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int surfaceN = in.nextInt(); // the number of points used to draw the
										// surface of Mars.
		for (int i = 0; i < surfaceN; i++) {
			int landX = in.nextInt(); // X coordinate of a surface point. (0 to
										// 6999)
			int landY = in.nextInt(); // Y coordinate of a surface point. By
										// linking all the points together in a
										// sequential fashion, you form the
										// surface of Mars.
		}

		// game loop
		while (true) {
			int X = in.nextInt();
			int Y = in.nextInt();
			int hSpeed = in.nextInt(); // the horizontal speed (in m/s), can be
										// negative.
			int vSpeed = in.nextInt(); // the vertical speed (in m/s), can be
										// negative.
			int fuel = in.nextInt(); // the quantity of remaining fuel in
										// liters.
			int rotate = in.nextInt(); // the rotation angle in degrees (-90 to
										// 90).
			int power = in.nextInt(); // the thrust power (0 to 4).

			// Write an action using System.out.println()
			// To debug: System.err.println("Debug messages...");

			// rotate power. rotate is the desired rotation angle. power is the
			// desired thrust power.
			System.out.println("-20 3");
		}
	}
}