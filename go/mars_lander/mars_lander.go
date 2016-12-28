package main

import "fmt"
import "os"
import "math"

type Point struct {
	x, y int
}

func max(a int, b int) int {
	if a > b {
		return a
	} else {
		return b
	}
}

func min(a int, b int) int {
	if a < b {
		return a
	} else {
		return b
	}
}

func abs(a int) int {
	if a < 0 {
		return -a
	} else {
		return a
	}
}

func isOnSegment(p Point, a Point, b Point) bool {

	if p.x <= max(a.x, b.x) && p.x >= min(a.x, b.x) &&
		p.y >= min(a.y, b.y) && p.y <= max(a.y, b.y) {
		return true
	} else {
		return false
	}
}

func slope(a Point, b Point) int {
	if diff := b.y - a.y; diff != 0 {
		if diff2 := b.x - a.x; diff2 != 0 {
			return diff / diff2
		}
	}
	return 0
}

func orientation(a Point, b Point, p Point) int {
	return slope(b, p) - slope(a, b)
}

func intersect(a Point, b Point, p Point, q Point) bool {
	oa := orientation(a, b, p)
	ob := orientation(a, b, q)

	oc := orientation(p, q, a)
	od := orientation(p, q, b)

	if oa != ob && oc != od {
		fmt.Fprintln(os.Stderr, "onoz")
		return true
	}

	if oa == 0 && isOnSegment(p, b, b) {
		fmt.Fprintln(os.Stderr, "oa == 0 && isOnSegment")
		return true
	}
	if ob == 0 && isOnSegment(q, a, b) {
		fmt.Fprintln(os.Stderr, "ob == 0 && isOnSegment")
		return true
	}
	if oc == 0 && isOnSegment(a, p, q) {
		fmt.Fprintln(os.Stderr, "oc == 0 & isOnSegment")
		return true
	}
	if od == 0 && isOnSegment(b, p, q) {
		//fmt.Fprintf(os.Stderr, "(%d,%d) (%d, %d) (%d,%d)\n", b.x, b.y, p.x, p.y, q.x, q.y)
		fmt.Fprintln(os.Stderr, "od == 0 && isOnSegment")
		return true
	}
	return false
}

// return the index of the landing zone
// landing zone means diff(y1, y2) == 0
func landingZone(surface []Point, nb int) int {
	for i := 1; i < nb; i++ {
		if surface[i - 1].y - surface[i].y == 0 {
			return i
		}
	}
	return -1
}

// compute nb collisions
func collisions(surface []Point, nb int, landing Point, position Point) int {
	nbCollisions := 0
	for i := 1; i < nb; i++ {
		if intersect(position, landing, surface[i - 1], surface[i]) {
			nbCollisions++
		}
	}
	return nbCollisions
}

func angle(vspeed float64, power float64) float64 {
	return math.Acos(float64(power / vspeed)) * 180 / math.Pi
}

type Ship struct {
	r float64
	throttle int
	pos Point
}

func main() {
	// read bn segemnts of surface
	var surfaceN int
	fmt.Scan(&surfaceN)

	// constructs the map of the world & get the highest peak
	var highest_point int = 0
	mars_surface := make([]Point, surfaceN)
	for i := 0; i < surfaceN; i++ {
		msurf := &mars_surface[i]
		fmt.Scan(&msurf.x, &msurf.y)
		if highest_point < msurf.y {
			highest_point = msurf.y
		}
	}

	// get the landing zone index
	idx := landingZone(mars_surface, surfaceN)
	fmt.Fprintf(os.Stderr, "landing zone: index=%d> coords=(%d, %d) (%d, %d)\n", idx, mars_surface[idx - 1].x, mars_surface[idx - 1].y, mars_surface[idx].x, mars_surface[idx].y)
	fmt.Fprintf(os.Stderr, "peak: %d", highest_point)

	// compute landing point ie middle of landing zone
	var landing Point
	landing.x = (mars_surface[idx].x + mars_surface[idx - 1].x) / 2
	landing.y = mars_surface[idx].y

	// clearWay
	//d_hspeed := 20



	/*    if position.y < highest_y { // landing
		//rotate_angle := d_hspeed
	    }*/
	//var okCaptain = false
	firstPhase := true
	for {
		// read game informations
		var ship Ship
		var hspeed, vspeed, fuel, rotate, power int
		fmt.Scan(&ship.pos.x, &ship.pos.y, &hspeed, &vspeed, &fuel, &rotate, &power)

		nbCollisions := collisions(mars_surface, surfaceN, landing, ship.pos)
		fmt.Fprintf(os.Stderr, "collisions> %d\n", nbCollisions)
		if nbCollisions > 1 {
			// we need to do something later
			// basically the idea is to rotate & move slowly to the direction of landing zone
			// until nbCollisions == 1
		}

		dx := landing.x - ship.pos.x

		if firstPhase && hspeed <= 40 {
			ship.throttle = 4
			ship.r = -45

			if dx == 0 {
				ship.r = 0
			}

		} else {
			firstPhase = false

			fmt.Fprintf(os.Stderr, "first phase finished!\n")
			// MRUA
			//  v^2 = v0^2 + 2 * a (x - x0)
			// http://www.physics.ohio-state.edu/~dws/class/131/lecture_recap.pdf
			// => a = (v^2 - v0^2) / 2 * (x - x0)

			// compute x
			speedy := hspeed * hspeed
			fmt.Fprintf(os.Stderr, "speedy= %d\n", speedy)
			twodx := 2 * dx
			fmt.Fprintf(os.Stderr, "twodx= %d\n", twodx)
			fmt.Fprintf(os.Stderr, "res= %d\n",  - (speedy) / twodx)


			ship.throttle = - (hspeed * hspeed) / (2 * dx)
			//ship.r = -math.Min(angle(float64(vspeed), float64(ship.throttle)), 45.0)
			ship.r = -angle(float64(vspeed), float64(ship.throttle))

			if ship.throttle < 0 {
				ship.throttle = - ship.throttle
				ship.r = -ship.r
			}

		}
		fmt.Fprintf(os.Stderr, "satellite position= (%d, %d)\n", ship.pos.x, ship.pos.y)
		fmt.Fprintf(os.Stderr, "landing position= (%d, %d)\n", landing.x, landing.y)
		fmt.Fprintf(os.Stderr, "dx= %d\n", dx)
		fmt.Fprintf(os.Stderr, "hspeed= %d\n", hspeed)
		fmt.Fprintf(os.Stderr, "ship.throttle= %d\n", ship.throttle)
		fmt.Fprintf(os.Stderr, "ship.r= %d\n", int(ship.r))




		/*if !okCaptain {
			if abs(position.x - landing.x) < 1700 {
				angle = 0
				speed = 0
				okCaptain = true
				fmt.Fprintf(os.Stderr, "okcaptain !")
			} else {

			}
		}

		if okcaptain {
			//high := position.y - landing.y
			if vspeed < -40 {
				speed = 4
			} else {
				speed = 0
			}
		}
		fmt.Fprintf(os.Stdout,"%d %d\n", angle, speed)*/
		fmt.Fprintf(os.Stdout,"%d %d\n", int(ship.r), ship.throttle)
	}

	fmt.Fprintf(os.Stdout, "%d", angle(3.71,4));
}

/*

	MRUA
	x(t) = x0 + v0 * t + 1/2 * a * t ^ 2

	donc:

	xt - x0 - v0 * t - 2



	 relation entre l'accélération, la variation de vitesse et le chemin parcouru

	 v^2 = v0^2 + 2 * a * (x - x0)


 */

/* Notes:

The force of gravity, g = 3.711 m/s²
Time to splat: sqrt ( 2 * y / g )
Velocity at splat time: sqrt( 2 * g * y )
time = [ −vi + √(vi² + 2gy) ]/g


 */