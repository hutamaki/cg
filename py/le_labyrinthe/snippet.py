import sys
from enum import Enum
import heapq
import time
from collections import deque

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

class Node:
    
    def __init__(self, plot):
        self.plot = plot
        self.parent = None
        self.f = 0
        self.x = 0
        self.g = 0
        self.x = 0
    
    def __hash__(self):
        return hash(self.plot)
    
    def __eq__(self, other):
        return self.plot == other.plot
    
    def __lt__(self, other):
        if self.f > other.f:
            return False
        return True
    
    def __repr__(self):
        return repr(self.plot)
    
    def __str__(self):
        return str(self.plot)

class Astar:
      
    def __init__(self):
        self.iter = 0
          
    def manhattan_distance(self, a, b):
        return abs(b.x - a.x) + abs(b.y - a.y)
      
    def search(self, graph, start, goal):
          
        self.iter = 0
          
        node_goal = Node(goal)
        closeList = {}
          
        current = Node(start)
        nodeScores = {current : 0}
          
        heapl = []
        heapq.heappush(heapl, (0, current))
          
        while heapl:   
            
            f, current = heapq.heappop(heapl)
            del nodeScores[current]         
            
            self.iter += 1
              
            neighbours = graph[current]
            for neighbour in neighbours:
                if neighbour.isDiscovered:                   
                      
                    node = Node(neighbour)                      
                    node.g = current.g + self.manhattan_distance(current.plot, node.plot)
                    node.h = self.manhattan_distance(node.plot, node_goal.plot)
                    node.f = node.h + node.g
                    node.x = current.x + 1                
                    node.parent = current           
  
                    if node == node_goal:                         
                        return node
                  
                    if self.worthTrying(node, nodeScores, closeList, heapl):
                        nodeScores[node] = node.f
                        heapq.heappush(heapl, (node.f, node))
                      
            closeList[current] = current.f
              
    def worthTrying(self, newNode, nodeScores, closeList, heapl):
          
        if newNode in nodeScores:
            f = nodeScores[newNode]
            if f < newNode.f:
                return False
            else:
                heapl.remove((f, newNode))
                heapq.heapify(heapl)
                del nodeScores[newNode]
              
        if newNode in closeList:
            f = closeList[newNode]
            if f < newNode.f:
                return False
            else:
                del closeList[newNode]            
        return True
  
    def getPath(self, node, path):
        ptr = node
        while ptr:            
            path.appendleft(ptr)            
            ptr = ptr.parent
                            
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
                    self.graph[plot] = []
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

    def toDirection(self, current, plot):
        if current.x < plot.x : return "RIGHT"
        elif current.x > plot.x: return "LEFT"
        elif current.y < plot.y: return "DOWN"
        else: return "UP"

    def backtrace(self, command):
        back_trace_dict = {
            "RIGHT" : "LEFT",
            "LEFT" : "RIGHT",
            "UP" : "DOWN",
            "DOWN" : "UP"}
        return back_trace_dict.get(command)
        
    def getNextCoordinates(self, stringMaze, stack):
        self.already.append(self.current)
 
        nextMoves = self.graph[self.current]
        for nextMove in nextMoves:
            if not nextMove in self.already:
                if stringMaze[nextMove.y][nextMove.x] == self.COMMAND: #we don't want to go onto the command center at this time
                    self.already.append(nextMove)
                    continue
                direction = self.toDirection(self.current, nextMove)
                stack.append(self.backtrace(direction))
                return direction        
        return None if len(stack) == 0 else stack.pop()    

class Phases(Enum):
    DISCOVER = 1
    PATH = 2
    DISPLAY = 3
    
maze = Maze()

# r: number of rows.
# c: number of columns.
# a: number of rounds between the time the alarm countdown is activated and the time the alarm goes off.
r, c, a = [int(i) for i in input().split()]
maze.width = c
maze.height = r
maze.rounds = a

stack = []
path = None
phase = Phases.DISCOVER
astar = Astar()


# game loop
while True:
    
    start = time.time()              
    kr, kc = [int(i) for i in input().split()]    

    maze.current = Plot(kc, kr)
    stringMaze = []    
    for i in range(r):
        row = input()  # C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
        x = row.find('C')
        if x != -1:
            maze.command = Plot(x, i)
        x = row.find('T')
        if x != -1:
            maze.start = Plot(x, i)
        stringMaze.append(row)
    
    maze.buildGraph(stringMaze)
    
    current = time.time()  
    if phase == Phases.DISCOVER:

        # check if C is reachable
        # compute way length from command center to start
        if maze.command:
            path = deque()
            node = astar.search(maze.graph, maze.start, maze.command)            
            if node: # not path yet
                
                if node.x <= maze.rounds: # means we have short path
                    astar.getPath(node, path) #
                    path.pop()        
                    
                    print(path, file=sys.stderr)        
                
                    # path from current to command
                    node_to_goal = astar.search(maze.graph, maze.command, maze.current)
                    togoal = deque()
                    astar.getPath(node_to_goal, togoal)
                    togoal.pop()
                    
                    path.extend(togoal)

                    #join both pathes                    
                    phase = Phases.DISPLAY


        if phase == Phases.DISCOVER: # if phase is still discover, means we do not have found shortest path
            direction = maze.getNextCoordinates(stringMaze, stack)
            if direction:    
                print(direction)
    
    if phase == Phases.DISPLAY:
        print(maze.toDirection(maze.current, path.pop().plot))        

    end = time.time()
    print("partial = ", (end-current) * 1000.0, file=sys.stderr)
    print("total = ", (end-start) * 1000.0, file=sys.stderr)