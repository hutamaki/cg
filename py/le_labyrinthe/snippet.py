import sys
import math

class Plot:
    
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def __hash__(self):
        return (397* self.y) ^ self.x

    def __eq__(self, other):
        return hash(self) == hash(other)

    def __repr__(self):
        return str(self)
        
    def __str__(self):
        return "(%d,%d)" % (self.x, self.y)

        
class Unit(Plot):
    
    def __init__(self, x, y, discovered):
        super().__init__(x, y)
        self.isDiscovered = discovered
        
class Maze:
    
    WALL = '#'
    UNKNOWN = '?'
    CLEAR = '.'
    START = 'T'
    COMMAND = 'C'
    
    def __init__(self):
        self.command = None
        self.start = None
        self.current = None
        self.graph = None 
        self.width = 0
        self.height = 0
        self.rounds = 0
        self.already = []
    
    def buildGraph(self, stringMaze):
        self.graph = {}
        for y in range(self.height):
            row = stringMaze[y]
            for x in range(self.width):
                plot = Plot(x, y)
                moves = []
                if row[x] == self.WALL:
                    self.graph[plot] = None
                else:
                    if x - 1 > 0 and row[x - 1] != self.WALL:
                        moves.append(Unit(x - 1, y, row[x - 1] != self.UNKNOWN)) # LEFT
                    if x + 1 < self.width and row[x + 1] != self.WALL:
                        moves.append(Unit(x + 1, y, row[x + 1] != self.UNKNOWN)) # RIGTH
                    if y - 1 > 0 and stringMaze[y - 1][x] != self.WALL:  
                        moves.append(Unit(x, y - 1, stringMaze[y - 1][x] != self.UNKNOWN))  # UP
                    if y + 1 < self.height and stringMaze[y + 1][x] != self.WALL:
                        moves.append(Unit(x, y + 1, stringMaze[y + 1][x] != self.UNKNOWN)) # DOWN
                    self.graph[plot] = moves

    def toDirection(self, plot):
        if self.current.x < plot.x : return "RIGHT"
        elif self.current.x > plot.x: return "LEFT"
        elif self.current.y < plot.y: return "DOWN"
        else: return "UP"

    def recursiveFinding(self):
        pass

    def isWorthMoving(self, plot, stringMaze):
        for y in [plot.y - 2, plot.y - 1, plot.y, plot.y + 1, plot.y + 2]:
            if y < 0: continue
            if y >= self.height: continue            
            row = stringMaze[y]            
            for x in [plot.x - 2, plot.x - 1, plot.x, plot.x + 1, plot.x + 2]:
                if plot.x - 2 < 0: continue
                if plot.x + 2 >= self.width: continue
                if row[x] == self.UNKNOWN:
                    return True
        return False

    def backtrace(self, command):
        back_trace_dict = {
            "RIGHT" : "LEFT",
            "LEFT" : "RIGHT",
            "UP" : "DOWN",
            "DOWN" : "UP"}
        return back_trace_dict.get(command)
        
    def getNextCoordinates(self, stringMaze, stack):

        self.already.append(self.current)

        print("current position: ", self.current, file=sys.stderr)        
        nextMoves = self.graph[self.current]
        print("possible moves: ", nextMoves, file=sys.stderr)
        if nextMoves:
            for nextMove in nextMoves:
                if not nextMove in self.already:
                        direction = self.toDirection(nextMove)
                        stack.append(self.backtrace(direction))
                        return direction                        
        return None if len(stack) == 0 else stack.pop()

maze = Maze()

# r: number of rows.
# c: number of columns.
# a: number of rounds between the time the alarm countdown is activated and the time the alarm goes off.
r, c, a = [int(i) for i in input().split()]
maze.width = c
maze.height = r
maze.rounds = a

stack = []
# game loop
while True:
    kr, kc = [int(i) for i in input().split()]
    maze.current = Plot(kc, kr)
    stringMaze = []
    for i in range(r):
        row = input()  # C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
        if i != kr:
            print(row, file=sys.stderr)
        else:
            nrow = row[0:kc] + "X" + row[kc+1:]
            print(nrow, file=sys.stderr)
        if 'C' in row:
            x = row.find('C')
            maze.command = Plot(x, i)
        if 'T' in row:
            x = row.find('T')
            maze.start = Plot(x, i)            
        stringMaze.append(row)
    
    maze.buildGraph(stringMaze)    
    print("%s" % maze.getNextCoordinates(stringMaze, stack))
