package main

import (
	"fmt"
	"os"
)

type Lemming struct {
	cloneFloor, clonePos int
	direction string
}

var (
	elevators map[int][]int
	lemming Lemming
	nbFloors, width, nbRounds, exitFloor, exitPos, nbTotalClones, nbAdditionalElevators, nbElevators int
)

type Move struct {
	action string // action could be ELEVATOR, WAIT, BLOCK
	togo int // x pos to goto, only if WAIT
	nextMoves []Move
}

func ComputeMoves(leny Lemming) []Move {

	var nextMoves []Move

	// check if elevators
	currentFloorElevators := elevators[leny.cloneFloor]
	if len(currentFloorElevators) > 0 {
		// go to left & right nearest elevators since others are not reachable from this point
		var left_elevator, right_elevator int = -1, width
		for _, x := range currentFloorElevators {
			if x <= lemming.clonePos { // elevator is @ left
				if x < left_elevator { // too far, don't consider
					continue
				} else {
					left_elevator = x // ok take it
				}
			} else { // elevator is @right
				if x > right_elevator { // too fat, don't consider
					continue
				} else {
					right_elevator = x // ok take it
				}
			}
		}

		if left_elevator != -1 { // we have the nearest elevator @left
			wait := Move{"WAIT", leny.clonePos - left_elevator, nil}
			if leny.direction == "RIGHT" { // need to block first
				block := Move{"BLOCK", 0, nil}
				block.nextMoves = append(block.nextMoves, wait)
				nextMoves = append(nextMoves, block)
			} else {
				nextMoves = append(nextMoves, wait)
			}
		}
		if right_elevator != width { // we have the nearest elevator @left
			wait := Move{"WAIT", right_elevator - leny.clonePos, nil}
			if leny.direction == "LEFT" { // need to block
				block := Move{"BLOCK", 0, nil}
				block.nextMoves = append(block.nextMoves, wait)
				nextMoves = append(nextMoves, block)
			} else {
				nextMoves = append(nextMoves, wait)
			}
		}
	} else { // we do not get elevators, we need to try to figure out where to put'em

		//  we need to known where to put those elevators
		// two cases:
		// if next level is an exit, try to construct the elevator just below the exit
		// if it is not an exit, we need to try to build the elevator below each possible elevator

		// first we try to know where are next level elevators
		nextFloorElevators := elevators[leny.cloneFloor + 1]
		if len(nextFloorElevators) == 0 {

			// maybe is it the exit floor, if it's the case, just fake an nextFloorElevator (smells like a bam):
			if leny.cloneFloor+1 == exitFloor {
				nextFloorElevators = append(nextFloorElevators, exitPos)
			} else {

				fmt.Fprintln(os.Stdout, "ça va pas être possible messieurs") // we could do something recursive, but not necessary now
				return nil
			}
		}

		// ok we try to generate moves by generating elevators just below those of next level (could be a bad idea)(or not)
		// at this time we can not know where we need to go, to we bourinely generate a move for all
		for _, elevator := range nextFloorElevators {
			elevator_command := Move{"ELEVATOR", 0, nil}
			if elevator < leny.clonePos {		// elevator @left
				wait := Move{"WAIT", leny.clonePos - elevator, nil}
				wait.nextMoves = append(wait.nextMoves, elevator_command)
				if leny.direction == "RIGHT" {		// if we go right, we need to block first
					block := Move{"BLOCK", 0, nil}
					block.nextMoves = append(block.nextMoves, wait)
					nextMoves = append(nextMoves, block)
				} else {
					nextMoves = append(nextMoves, wait) // otherwise, just add wait + elevator command
				}
			} else {
				if elevator > leny.clonePos {	// elevator @ right
					wait := Move{"WAIT", elevator - leny.clonePos, nil}
					wait.nextMoves = append(wait.nextMoves, elevator_command)
					if leny.direction == "LEFT" {		// if we go left, we need to block first
						block := Move{"BLOCK", 0, nil}
						block.nextMoves = append(block.nextMoves, wait)
						nextMoves = append(nextMoves, block)
					} else {
						nextMoves = append(nextMoves, wait)	// otherwise, just wait + elevator command
					}
				} else {
					nextMoves = append(nextMoves, elevator_command) // we are just below, only put elevator command
				}
			}
		}
	}
	return nextMoves
}

func main() {
	// nbFloors: number of floors
	// width: width of the area
	// nbRounds: maximum number of rounds
	// exitFloor: floor on which the exit is found
	// exitPos: position of the exit on its floor
	// nbTotalClones: number of generated clones
	// nbAdditionalElevators: number of additional elevators that you can build
	// nbElevators: number of elevators

	nbFloors= 2
	width= 13
	exitFloor= 1
	exitPos= 11
	nbTotalClones= 10
	nbAdditionalElevators= 1
	nbElevators= 0
	lemming.cloneFloor = 0
	lemming.clonePos= 2
	lemming.direction="RIGHT"
	/*nb possible moves for next level= 1*/

	//fmt.Scan(&nbFloors, &width, &nbRounds, &exitFloor, &exitPos, &nbTotalClones, &nbAdditionalElevators, &nbElevators)

	fmt.Fprintf(os.Stderr,"nbFloors= %d\n", nbFloors)
	fmt.Fprintf(os.Stderr,"width= %d\n", width)
	fmt.Fprintf(os.Stderr,"exitFloor= %d, exitPos= %d\n", exitFloor, exitPos)
	fmt.Fprintf(os.Stderr,"nbTotalClones= %d\n", nbTotalClones)
	fmt.Fprintf(os.Stderr,"nbAdditionalElevators= %d\n", nbAdditionalElevators)
	fmt.Fprintf(os.Stderr,"nbElevators= %d\n", nbElevators)

	// store elevators, to that elevators[i] contains the position of all elevators
	for i := 0; i < nbElevators; i++ {
		// elevatorFloor: floor on which this elevator is found
		// elevatorPos: position of the elevator on its floor
		var elevatorFloor, elevatorPos int
		fmt.Scan(&elevatorFloor, &elevatorPos)
		elevators[elevatorFloor] = append(elevators[elevatorFloor], elevatorPos)
	}
	for {
		// cloneFloor: floor of the leading clone
		// clonePos: position of the leading clone on its floor
		// direction: direction of the leading clone: LEFT or RIGHT
		//fmt.Scan(&lemming.cloneFloor, &lemming.clonePos, &lemming.direction)
		fmt.Fprintf(os.Stderr, "cloneFloor= %d, clonePos= %d, direction= %s\n", lemming.cloneFloor, lemming.clonePos, lemming.direction)

		moves := ComputeMoves(lemming)
		fmt.Fprintf(os.Stderr,"nb possible moves for next level= %d\n", len(moves))

		fmt.Println("WAIT") // action: WAIT or BLOCK
	}
}