package main

import (
	"fmt"
	"os"
)

func abs(a int) int {
	if a <= 0 {
		return -a
	} else {
		return a
	}
}

type Direction int
const (
	LEFT Direction = iota
	RIGHT Direction = iota
)

func opposite(direction Direction) Direction {
	if direction == LEFT {
		return RIGHT
	} else {
		return LEFT
	}
}

type Action int
const (
	WAIT Action =  iota
	ELEVATOR Action = iota
	BLOCK Action = iota
)

type Lemming struct {
	cloneFloor, clonePos int
	direction Direction
}

var (
	elevators map[int][]int
	lemming Lemming
	nbFloors, width, nbRounds, exitFloor, exitPos, nbTotalClones, nbAdditionalElevators, nbElevators int
)

type Move struct {
	action Action // action could be ELEVATOR, WAIT, BLOCK
	cost int // x pos to goto, only if WAIT, cost of the function otherwise
	nextMoves []*Move // possible moves after that
	Lemming // new position & floor after move
}

func NewMove(action Action, togo int, lemming Lemming) *Move {
	var cost, pos, floor int
	var direction Direction
	switch action {
		case WAIT : {
			cost = togo
			pos += lemming.clonePos
			floor = lemming.cloneFloor
		}
		case BLOCK : {
			cost = 3
			pos = lemming.clonePos
			floor = lemming.cloneFloor
			direction = opposite(lemming.direction)
		}
		case ELEVATOR : {
			cost = 1
			pos = lemming.clonePos
			floor = lemming.cloneFloor
		}
	}
	return &Move{action: action, cost: cost, Lemming:Lemming{floor, pos, direction}}
}

func ManhattanDistance(floor, pos int) int { // means h in A*, distance to goal (here the exit)
	return abs(exitFloor - pos) + abs(exitPos - floor)
}


/*func CostFunction(g int, move *Move) int { // means g in A*, the cost of the way to go here
	cost := g
	for ptr := move; move.nextMoves == nil;  {
		switch  {
		case move.nextMoves == nil: continue
		case len(move.nextMoves) > 1: fmt.Println("IMPOSSIBRU ! it's not a newly computed path !")
		}
	}
}

func Astar() {

}*/

func CreateElevator(lemming Lemming) *Move {
	elevator := NewMove(ELEVATOR, 0, lemming)
	wait := NewMove(WAIT, 3, elevator.Lemming)
	elevator.nextMoves = append(elevator.nextMoves, wait)
	return elevator
}

func debugTrace(move *Move) {
	count := move.cost
	ptr := move
	for i := 0; ptr != nil; i++ {
		switch ptr.action {
		case ELEVATOR:
			fmt.Fprintf(os.Stdout, "%d> ELEVATOR\n", i)
		case BLOCK:
			fmt.Fprintf(os.Stdout, "%d> BLOCK\n", i)
		case WAIT:
			{
				if count > 0 {
					fmt.Fprintf(os.Stdout, "%d> WAIT\n", i)
					count --
				}
			}
		}

		if ptr.action == WAIT && count > 0 {
			continue
		}

		if ptr.nextMoves != nil {
			ptr = ptr.nextMoves[0]
			count = ptr.cost // do not inverse lines !
		} else {
			ptr = nil
		}
	}
}

func ComputeMoves(leny Lemming) []*Move {

	var nextMoves []*Move

	// check if elevators
	currentFloorElevators := elevators[leny.cloneFloor]
	if len(currentFloorElevators) > 0 {
		// go to left & right nearest elevators since others are not reachable from this point
		var left_elevator, right_elevator int = -1, width
		for _, x := range currentFloorElevators {
			if x <= lemming.clonePos { // elevator is @ left
				if x >= left_elevator {
					left_elevator = x // ok take it
				}
			} else { // elevator is @right
				if x <= right_elevator {
					right_elevator = x
				}
			}
		}

		if left_elevator != -1 { // we have the nearest elevator @left
			var p *Move
			if leny.direction == RIGHT { // need to block first
				block := NewMove(BLOCK, 0, leny)
				wait := NewMove(WAIT, leny.clonePos - left_elevator, block.Lemming) // compute wait position using block one
				p.nextMoves = append(p.nextMoves, wait) // do linkage block -> wait
			} else {
				p = NewMove(WAIT, leny.clonePos - left_elevator, leny)
			}
			nextMoves = append(nextMoves, p)
		}
		if right_elevator != width { // we have the nearest elevator @left
			var p *Move
			if leny.direction == RIGHT { // need to block
				p = NewMove(BLOCK, 0, leny)
				wait := NewMove(WAIT, right_elevator - leny.clonePos, p.Lemming)
				p.nextMoves = append(p.nextMoves, wait)
			} else {
				p = NewMove(WAIT, right_elevator - leny.clonePos, leny)
			}
			nextMoves = append(nextMoves, p)
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
			if elevator < leny.clonePos { // elevator @left

				var p *Move
				if leny.direction == RIGHT { // if we go right, we need to block first
					block := NewMove(BLOCK, 0, leny)
					nextMoves = append(nextMoves, block)
					p = NewMove(WAIT, leny.clonePos-elevator, block.Lemming)
				} else {
					p = NewMove(WAIT, leny.clonePos-elevator, leny) // otherwise, just add wait + elevator command
					nextMoves = append(nextMoves, p)
				}
				p.nextMoves = append(p.nextMoves, CreateElevator(p.Lemming))
			} else {
				if elevator > leny.clonePos { // elevator @ right
					var p *Move
					if leny.direction == LEFT { // if we go left, we need to block first
						block := NewMove(BLOCK, 0, leny)
						nextMoves = append(nextMoves, block)
						p = NewMove(WAIT, elevator-leny.clonePos, block.Lemming)
					} else {
						p = NewMove(WAIT, elevator-leny.clonePos, leny) // otherwise, just wait + elevator command
						nextMoves = append(nextMoves, p)
					}
					p.nextMoves = append(p.nextMoves, CreateElevator(p.Lemming))
				} else {
					nextMoves = append(nextMoves, CreateElevator(leny)) // we are just below, only put elevator command
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
	lemming.direction=RIGHT
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
	//for {
		// cloneFloor: floor of the leading clone
		// clonePos: position of the leading clone on its floor
		// direction: direction of the leading clone: LEFT or RIGHT
		//fmt.Scan(&lemming.cloneFloor, &lemming.clonePos, &lemming.direction)
		fmt.Fprintf(os.Stderr, "cloneFloor= %d, clonePos= %d, direction= %s\n", lemming.cloneFloor, lemming.clonePos, lemming.direction)

		moves := ComputeMoves(lemming)
		fmt.Fprintf(os.Stderr,"nb possible moves for next level= %d\n", len(moves))
		debugTrace(moves[0])
	//}


		/*fmt.Println("WAIT") // action: WAIT or BLOCK**/
}