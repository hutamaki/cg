import sys
import math

"""
GLOBAL VARS
"""

globalMap = {}
myFactories = {}
theirFactories = {}

myTroops = {}
theirTroops = {}

graphset = {}
median = -1;

""" 
INITIALISATION PHASE
"""

def calcMedian():
    dicts = list(globalMap.values())
    result = []
    for item in dicts:
        result = result + list(item.values())
    result = sorted(result)
    median = result[int(len(result) / 2)];
    return median

def buildGraphcet():
    
    # get median
    median = 100;#calcMedian()
    print("median> ", median, file=sys.stderr)
    
    # ok we have a root, we can now build the graphset
    for entityId, items in globalMap.items():
        projection = [(k, v) for (k, v) in items.items() if v <= median]
        projection = sorted(projection, key=lambda x: x[1]);
        graphset[entityId] = projection.copy()
    print("graphset> ", graphset, file=sys.stderr)
    
"""
FACTORY
"""
class Factory:

    def __init__(self, entityId, nbCyborgs, production, waitNormal, isNeutral):
        self.entityId = entityId
        self.production = production
        self.nbCyborgs = nbCyborgs
        self.waitNormal = waitNormal
        self.isNeutral = isNeutral

    def __repr__(self):
        return self.__str__()
    
    def __str__(self):
        return "%d> production= %d, nbCyborg= %d, waitNormal= %d" % (self.entityId, self.production, self.nbCyborgs, self.waitNormal)

"""
TROOP
"""
class Troop:
    
    def __init__(self, entityId, start, target, nbCyborg, eta):
        self.entityId = entityId
        self.start = start
        self.target = target
        self.nbCyborg = nbCyborg
        self.eta = eta

    def __repr__(self):
        return self.__str__()
    
    def __str__(self):
        return "%d> start= %d, target= %d, nb= %d, eta=%d" % (self.entityId, self.start, self.target, self.nb, self.eta)        

"""
GAME
"""
def rate(id):
    fact = None;
    if id in myFactories:
        fact = myFactories[id]
    else:
        fact = theirFactories[id]
    return fact.production * 1000 - fact.nbCyborgs * 100

class Game:
   
    def mercyLess(self):
        
        commands = ["WAIT"]
        # now is time to perform a bfs, to SEND IT ALL
        done = []            
        targets = [v.entityId for (k,v) in myFactories.items()]
        while targets:
            
            print ("targets> ", targets, file=sys.stderr)
                        
            # pop first element
            current = targets[0]

            if current not in myFactories:
                continue

            done.append(current) #mark as done
            print("current> ", current, file=sys.stderr)
            targets = targets[1:]
            
            isUnderAttack, nb = needReinforcement(current)
            if isUnderAttack:
                print("isUnderAttack: ignonring %d" % current, file=sys.stderr);
                continue ;

            neighbours = sorted(graphset[current], key=lambda x : x[1] * 1000 - rate(x[0]))   # tuples of (factory_id, distance) sorted by rate
            for neib in neighbours:
                
                # id of target
                neibId = neib[0]
                print("considering ", neibId, file=sys.stderr)            
                
                # compute how many to send
                toSend= 0
                if neibId in myFactories:
                    attacked, nb = needReinforcement(neibId)
                    if attacked:
                        print("nb! ", nb, file=sys.stderr)
                        if nb >= 0: # means is attacked
                            print("base ",  neibId, " attacked by ", nb, " cyborgs" , file=sys.stderr)
                            toSend = nb + 1 
                            myFactories[current].nbCyborgs -= max(max(0, toSend), myFactories[current].nbCyborgs) # decreate pop
                            myFactories[neibId].nbCyborgs += max(max(0, toSend), myFactories[current].nbCyborgs) # increase dest pop
                    else:
                        if isUnderAttack < -13:
                            toSend = -(isUnderAttack + 13);
                            myFactories[current].nbCyborgs -= max(max(0, toSend), myFactories[current].nbCyborgs) # decreate pop
                    if (neibId not in done):
                        print("not in done", file=sys.stderr)
                        if (neibId not in targets):
                            print("not in targets", file=sys.stderr)
                            print ("done: ", done, file=sys.stderr)
                            print ("target: ", targets, file=sys.stderr)
                            #if not neib in targets):
                            print ("adding !!!!!!!!!!!!!!!!!!!!!!!", file=sys.stderr)
                            targets.append(neibId)
                else:
                    fac = theirFactories[neibId]                    
                    if fac.production == 0:
                        continue 
                    
                    
                    toSend = fac.nbCyborgs + 1
                    print("toSent1: ", toSend, file=sys.stderr)
                    if not fac.isNeutral:
                        toSend += (neib[1] + 1) * fac.production
                    else:
                        toSend -= OnTheWay(neibId) 
                        print("toSent2: ", toSend, file=sys.stderr)
                    toSend = max(toSend, 0)

                    # do not sent anything if not
                    if toSend > myFactories[current].nbCyborgs:
                        print("continue because not enough= ", myFactories[current], file=sys.stderr)
                        bombNearestBig(commands)
                        continue 

                    
                myFactories[current].nbCyborgs -= min(max(0, toSend), myFactories[current].nbCyborgs) # decreate pop
                    
                print("MOVE %d %d %d" % (current, neibId, toSend), file=sys.stderr)
                # add command    
                commands += "; MOVE %d %d %d" % (current, neibId, toSend)
                
                evolve(commands)
        
        return "".join(commands)  

"""
BOMB
"""
def bombNearestBig(commands):
    theirfactByRate = sorted(theirFactories.values(), key=lambda k: rate(k.entityId), reverse=True)
    target = [x for x in theirfactByRate if not x.isNeutral][0]
    print("BOMB target: ", target, file=sys.stderr)

    nodes = sorted(graphset[target.entityId], key=lambda x : x[1])
    print("NODES ", nodes, file=sys.stderr)
    filtered = [x for x in  nodes if x[0] in myFactories]    
    print("FILTERED ", filtered, file=sys.stderr)
    source = filtered[0]
    print("FROM: ", source, file=sys.stderr)
    commands += "; BOMB %d %d" % (source[0], target.entityId)


"""
EVOLVE
"""
def computeMyPopulation():
    nbCyborgs = 0
    for key, factory in myFactories.items():
        nbCyborgs += factory.nbCyborgs
    for k, troop in myTroops.items():
        nbCyborgs += troop.nbCyborg    
    return nbCyborgs

def computetheirPopulation():
    nbCyborgs = 0
    for key, factory in theirFactories.items():
        if factory.isNeutral: 
            continue
        nbCyborgs += factory.nbCyborgs        
    for k, troop in theirTroops.items():
        nbCyborgs += troop.nbCyborg    
    return nbCyborgs    
    
def evolve(commands):
    if (computeMyPopulation() - computetheirPopulation() < 10):
        return
    for k, base in myFactories.items():
        if base.nbCyborgs > 10:
            attacked, nb = needReinforcement(base.entityId)
            if not attacked or (attacked and nb <= -10):
                commands += "; INC %d" % k
            
"""
SIMU
"""
def OnTheWay(entityId):
    onWay = 0
    for k, troop in myTroops.items():
        if troop.target == entityId:
            onWay += troop.nbCyborg
    return onWay
    
def needReinforcement(entityId):
    onWay = 0
    attacked = False
    for k, troop in theirTroops.items():
        if troop.target == entityId:
            onWay += troop.nbCyborg
            attacked = True
    for k, troop in myTroops.items():
        if troop.target == entityId:
            onWay -= troop.nbCyborg
    onWay -= myFactories[entityId].nbCyborgs 
    return attacked, onWay

factory_count = int(input())  # the number of factories
link_count = int(input())  # the number of links between factories

for i in range(link_count):
    factory_1, factory_2, distance = [int(j) for j in input().split()]
    if not factory_1 in globalMap:
        globalMap[factory_1] = {}
    if not factory_2 in globalMap:
        globalMap[factory_2] = {}
    globalMap[factory_2][factory_1] = distance
    globalMap[factory_1][factory_2] = distance

    #print(factory_1, " -> ", factory_2, " = ", distance)
print("init phase...", file=sys.stderr)
calcMedian()    
buildGraphcet()

print("game creation...", file=sys.stderr)
game = Game()

# game loop
while True:
    
    myFactories = {}
    theirFactories = {}
    
    myTroops = {}
    theirTroops = {}
    
    print("getting entities...", file=sys.stderr)
    entity_count = int(input())  # the number of entities (e.g. factories and troops)
    for i in range(entity_count):
        entity_id, entity_type, arg_1, arg_2, arg_3, arg_4, arg_5 = input().split()
        entity_id = int(entity_id)
        arg_1 = int(arg_1)
        arg_2 = int(arg_2)
        arg_3 = int(arg_3)
        arg_4 = int(arg_4)
        arg_5 = int(arg_5)
        
        if entity_type == "FACTORY":        
            if arg_1 == -1:
                theirFactories[entity_id] = Factory(entity_id, arg_2, arg_3, arg_4, False)
            elif arg_1 == 0:
                theirFactories[entity_id] = Factory(entity_id, arg_2, arg_3, arg_4, True)
            elif arg_1 == 1:
                myFactories[entity_id] = Factory(entity_id, arg_2, arg_3, arg_4, False)
        elif entity_type == "TROOP":
            troop = Troop(entity_id, arg_2, arg_3, arg_4, arg_5)
            if arg_1 == -1:
                theirTroops[entity_id] = troop
            elif arg_1 == 1:
                myTroops[entity_id] = troop
    
    for _, fact in myFactories.items():
        print(fact, file=sys.stderr)

    print(game.mercyLess())
    # Write an action using print
    # To debug: print("Debug messages...", file=sys.stderr)


    # Any valid action, such as "WAIT" or "MOVE source destination cyborgs"
    #print("WAIT")
