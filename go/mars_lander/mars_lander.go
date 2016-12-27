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
		return false;
	}
}

func slope(a Point, b Point) int {
	if diff := b.y - a.y; diff != 0 {
		if diff2 := b.x - a.x; diff2 != 0 {
			return diff / diff2;
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

func computeAngle(landing Point, position Point) int {
	dist_to_land_y := float64(position.y - landing.y)
	dist_to_land_x := float64(position.x - landing.x)
	fmt.Fprintf(os.Stderr,"dist to land> (%d, %d)\n", dist_to_land_x, dist_to_land_y)
	tan_b := float64(dist_to_land_x / dist_to_land_y)
	angle := int(math.Floor(math.Atan(tan_b) * 180 / math.Pi))
	fmt.Fprintf(os.Stderr, "%d\n", angle)
	return angle
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

func main() {
	var surfaceN int
	fmt.Scan(&surfaceN)

	var highest_point int = 0
	mars_surface := make([]Point, surfaceN)
	for i := 0; i < surfaceN; i++ {
		msurf := &mars_surface[i]
		fmt.Scan(&msurf.x, &msurf.y)
		if highest_point < msurf.y {
			highest_point = msurf.y
		}
	}

	idx := landingZone(mars_surface, surfaceN)
	fmt.Fprintf(os.Stderr, "%d> (%d, %d) (%d, %d)\n",
		idx, mars_surface[idx - 1].x, mars_surface[idx - 1].y, mars_surface[idx].x, mars_surface[idx].y)
	fmt.Fprintf(os.Stderr, "> highest_y: %d", highest_point)

	var landing Point
	landing.x = (mars_surface[idx].x + mars_surface[idx - 1].x) / 2
	landing.y = mars_surface[idx].y

	// clearWay
	//d_hspeed := 20



	/*    if position.y < highest_y { // landing
		//rotate_angle := d_hspeed
	    }*/
	var okcaptain = false
	var angle = 0
	var speed = 3

	for {
		var position Point
		var hspeed, vspeed, fuel, rotate, power int
		fmt.Scan(&position.x, &position.y, &hspeed, &vspeed, &fuel, &rotate, &power)

		nbCollisions := collisions(mars_surface, surfaceN, landing, position)
		fmt.Fprintf(os.Stderr, "collisions> %d\n", nbCollisions)

		if !okcaptain {
			if abs(position.x - landing.x) < 1700 {
				angle = 0
				speed = 0
				okcaptain = true
				fmt.Fprintf(os.Stderr, "okcaptain !")
			} else {
				angle = computeAngle(landing, position)
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
		fmt.Fprintf(os.Stdout,"%d %d\n", angle, speed)
		//fmt.Fprintf(os.Stdout,"-45 4\n")
	}
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