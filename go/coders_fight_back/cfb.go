package main

import "fmt"
import (
	"os"
	"math"
)

// rank is now bronze: 1584/6412
const oponent_radius int = 400
const check_points_radius = 600

type Pod struct {
	thrust, x, y int
}

type Vector struct {
	x, y float64
}

func (v *Vector) scaleBy(value float64) {
	v.x *= value
	v.y *= value
}

func (v *Vector) square_length() float64 {
	return v.x * v.x + v.y * v.y
}

func (v *Vector) length() float64 {
	return math.Sqrt(v.square_length())
}

func (v *Vector) truncate(value float64) {
	if i := value / v.length(); i < 1.0 {
		v.scaleBy(1.0)
	} else {
		v.scaleBy(value)
	}
}


func main() {
	var thurst int = 100
	var boost bool = true
	for {
		// nextCheckpointX: x position of the next check point
		// nextCheckpointY: y position of the next check point
		// nextCheckpointDist: distance to the next checkpoint
		// nextCheckpointAngle: angle between your pod orientation and the direction of the next checkpoint

		/*	X,Y = 12938 1520
			nextCheckPointX, nextCheckPointY = 10546 5968
			nextCheckpointDist = 5050
			nextCheckpointAngle = 0
			opponentX = 12014, opponentY =  1136
		*/

		/*
			mass of oponent = 400
			mass of checkpoints = 600
		 */

		var x, y, nextCheckpointX, nextCheckpointY, nextCheckpointDist, nextCheckpointAngle int
		//fmt.Scan(&x, &y, &nextCheckpointX, &nextCheckpointY, &nextCheckpointDist, &nextCheckpointAngle)

		var opponentX, opponentY int
		//fmt.Scan(&opponentX, &opponentY)

		fmt.Fprintf(os.Stderr, "X,Y = %d %d\n", x, y)
		fmt.Fprintf(os.Stderr, "nextCheckPointX, nextCheckPointY = %d %d\n", nextCheckpointX, nextCheckpointY)
		fmt.Fprintf(os.Stderr, "nextCheckpointDist = %d\nnextCheckpointAngle = %d\n", nextCheckpointDist, nextCheckpointAngle)
		fmt.Fprintf(os.Stderr, "opponentX = %d, opponentY =  %d", opponentX, opponentY)

		if nextCheckpointDist > 600 {
			thurst = 100
		}

		if nextCheckpointAngle > 90 || nextCheckpointAngle < -90 {
			thurst = 0
		} else {
			if nextCheckpointDist < 800 {
				thurst -= 20
			}
		}
		if boost {
			fmt.Printf("%d %d BOOST\n", nextCheckpointX, nextCheckpointY)
			boost = false
		} else {
			fmt.Printf("%d %d %d\n", nextCheckpointX, nextCheckpointY, thurst)
		}
	}
}
