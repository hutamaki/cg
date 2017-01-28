package mars_lander_2;

import java.util.Scanner;

class Point	{
	public int x;
	public int y;
}

class Ship 	{
	double r;
	int throttle;
	Point pos;
}

public class Player {

	private Point[] surface;
	private int peak;
	
	private Point landing;
	
	/*
	 *  constructs the world map & gets the highest peak
	 *  initializes array Point[] surface 
	 *  initializes attributes peak
	 */
	private void constructSurfaceAndPeak(Scanner in) {
		int surfaceN = in.nextInt(); // the number of points used to draw the surface of Mars.		
		surface = new Point[surfaceN];
		int highest_point = 0;
		for (int i = 0; i < surfaceN; i++) {
			Point p = new Point();
			p.x = in.nextInt();
			p.y = in.nextInt();
			
			if (highest_point < p.y) {
				highest_point = p.y;
			}			
			surface[i] = p;
		}
		peak = highest_point;
	}
	
	/*
	 * construct the landing point (ie middle of the landing zone)
	 */
	private void constructLandingPoint() throws Exception {
		int indexOfLandingZone = landingZone(surface);
		landing = new Point();
		landing.x = (surface[indexOfLandingZone].x + surface[indexOfLandingZone - 1].x) / 2;
		landing.y = surface[indexOfLandingZone].y;
	}
	
	public Player(Scanner in) throws Exception {
		constructSurfaceAndPeak(in);
		constructLandingPoint();
	}
		
	/*
	 * Check wheter p in on segment AB
	 * @param p point to check
	 * @param a a coord of AB segment
	 * @param b b coord of AB segment
	 * @return true if p in on segment AB
	 */
	public boolean isOnSegment(Point p, Point a, Point b) {
		return (p.x <= Math.max(a.x, b.x) && p.x >= Math.min(a.x, b.x) &&
				p.y >= Math.min(a.y, b.y) && p.y <= Math.max(a.y, b.y));
	}
	
	/*
	 * Return the slope of segment AB
	 * @param a a coord of AB segment
	 * @param b b coord of AB segment
	 * @return the slope of AB semgent or 0 if divideBy0
	 */
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
	
	/*
	 * @param X x-pos of the module
	 * @param Y y-pos of the module
	 * @param hSpeed the horizontal speed (in m/s), can be negative.
	 * @param vSpeed the vertical speed (in m/s), can be negative.
	 * @param fuel the quantity of remaining fuel in liters.
	 * @param rotate the rotation angle in degrees (-90 to 90).
	 * @param power the thrust power (0 to 4).
	 */
	public void land(int X, int Y, int hSpeed, int vSpeed, int fuel, int rotate, int power) {
		System.err.println("20 3 ");
	}

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		Player p = null;
		try {
			p = new Player(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (true) {
			int X = in.nextInt();
			int Y = in.nextInt();
			int hSpeed = in.nextInt();
			int vSpeed = in.nextInt(); 
			int fuel = in.nextInt(); 
			int rotate = in.nextInt(); 
			int power = in.nextInt(); 
			
			p.land(X, Y, hSpeed, vSpeed, fuel, rotate, power);
		}
	}
}