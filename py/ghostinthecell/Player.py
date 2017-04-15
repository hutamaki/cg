import sys

"""
GLOBAL VARS
"""
from copy import deepcopy

#global Map is a map of map of distance
# ie [] globalMap[factory_id1][factory_id2] = distance

# map of map of (factory, distance) for each factory, sorted by distance  
graphset = {}

globalMap = {}
myFactories = {}
theirFactories = {}
allFactories = {}

myTroops = {}
theirTroops = {}

bombs = {}


""" 
INITIALISATION PHASE
"""

def buildGraphcet():
        
    # ok we have a root, we can now build the graphset
    for factoryIdSrc, items in globalMap.items():
        projection = [(factoryIdDst, distance) for (factoryIdDst, distance) in items.items()]
        projection = sorted(projection, key=lambda x: x[1]); # sorted by distance
        graphset[factoryIdSrc] = projection.copy()
        
    print("graphset> ", graphset, file=sys.stderr)
    
"""
FACTORY
"""
class Factory:

    def __init__(self, entityId, unitCount, production, waitNormal, owner):
        self.entityId = entityId
        self.production = production
        self.unitCount = unitCount
        self.waitNormal = waitNormal
        self.isNeutral = (owner == 0)
        self.owner = owner
        self.disabled = waitNormal
        
    def __eq__(self, other):
        return self.entityId == other.entityId
        
    def __repr__(self):
        return self.__str__()
    
    def __str__(self):
        return "%d> production= %d, unitCount= %d, waitNormal= %d, owner=%d, disable?=%d" % (self.entityId, self.production, self.unitCount, self.waitNormal, self.owner, self.disabled)

"""
TROOP
"""
class Troop:
    
    def __init__(self, entityId, start, target, unitCount, eta, owner):
        self.entityId = entityId
        self.start = start
        self.target = target
        self.unitCount = unitCount
        self.eta = eta
        self.owner = owner 
        
    # method for simulation
    def move(self):
        if self.eta > 0:
            self.eta -= 1

    def __repr__(self):
        return self.__str__()
    
    def __str__(self):
        return "%d> start= %d, target= %d, nb= %d, eta=%d owner=%d" % (self.entityId, self.start, self.target, self.unitCount, self.eta, self.owner)
    
class Bomb:
    
    def __init__(self, entityId, start, target, eta, owner):
        self.entityId = entityId
        self.start = start
        self.target = target
        self.eta = eta
        self.owner = owner
        
    def move(self):
        if self.eta > 0:
            self.eta -= 1
    
    def __repr__(self):
        return self.__str__()
    
    def __str__(self):
        return "%d> start= %d, target= %d, eta=%d, self.owner=%d" % (self.entityId, self.start, self.target, self.eta, self.owner)

"""
GAME
"""
def rate(fid):
    fact = None;
    if fid in myFactories:
        fact = myFactories[fid]
    else:
        fact = theirFactories[fid]
    return fact.unitCount

def OnTheWay(entityId):
    onWay = 0
    for _, troop in myTroops.items():
        #print("OnTheWay::troop= ", troop, file=sys.stderr)
        if troop.target == entityId:
            onWay += troop.unitCount
    return onWay
    
def needReinforcement(entityId):
    onWay = 0
    attacked = False

    for _, troop in theirTroops.items():
        if troop.target == entityId:
            onWay += troop.unitCount
            attacked = True
    for _, troop in myTroops.items():
        if troop.target == entityId:
            onWay -= troop.unitCount
    onWay -= myFactories[entityId].unitCount 
    return attacked, onWay

"""
**********************
SIMU
**********************

Simulation @ end
"""
def simulate():
    
    # for each turn    
    localallFactories = deepcopy(allFactories)
    
    localMyTroops = deepcopy(myTroops)
    localTheirTroops = deepcopy(theirTroops)
    allTroops = {**localMyTroops, **localTheirTroops}
    
    localbombs = deepcopy(bombs)
    players = {}
    
    while len(allTroops) > 0:
    
        readyToFight = {factoryId : {-1 : 0, 1 : 0}  for (factoryId, _) in allFactories.items()} 
        players = {1: { "factories": 0, "troops": 0}, 0: {"factories": 0, "troops": 0}, -1 : { "factories": 0, "troops": 0}}
    
        # 1 - production for factory        
        for _, factory in localallFactories.items():
            if factory.disabled > 0:
                factory.disabled -= 1                
            if (factory.disabled <= 0):
                factory.unitCount += factory.production
        
        # 2 - solve battles
        
        # move troops & populate all arriving troops and remove them from moving troops
        for _, troop in allTroops.items():
            #print("troop move: ", troop, file=sys.stderr)
            troop.move()
            if troop.eta <= 0:  
                readyToFight[troop.target][troop.owner] += troop.unitCount                
        allTroops = {troopId : troop for (troopId, troop) in allTroops.items() if troop.eta > 0}
        
        for factoryId, fightingTroops in readyToFight.items():
            units =  min(fightingTroops[-1], fightingTroops[1])
            fightingTroops[-1] -= units
            fightingTroops[1] -= units
            
            #print("factoryID: ", factoryId, " / ", fightingTroops[-1] , " / ",  fightingTroops[1], file=sys.stderr)
            
            # remaining units fight on the factory
            factory = localallFactories[factoryId]
            for player in [-1, 1]:
                unitCount = fightingTroops[player]                                
                if factory.owner == player: # if its our factory, juse accumulates
                    factory.unitCount += unitCount
                else:
                    if unitCount > factory.unitCount:
                        print("factory %d changes its owner !" % factory.entityId, file=sys.stderr)
                        factory.owner = 1 if (player == 1) else -1
                        factory.unitCount = unitCount - factory.unitCount
                    else:
                        factory.unitCount -= unitCount
                
        # handle bombs   
        for _, bomb in localbombs.items():
            bomb.move()
            if bomb.eta <= 0 and bomb.target != -1:
                factory = localallFactories[bomb.target] 
                currentCyborgs = factory.unitCount
                factory.unitCount =  max(0, currentCyborgs - max(10, int(currentCyborgs / 2)))
                factory.disabled = 5
        localbombs = { bombId : bomb for (bombId, bomb) in localbombs.items() if bomb.eta > 0 or bomb.target != -1}
        
        # update scores
        for playerId in [-1, 1]:
            players[playerId]["factories"] = 0
            players[playerId]["troops"] = 0
            
        for _, factory in localallFactories.items(): 
            players[factory.owner]["factories"] += 1        
            players[factory.owner]["troops"] += factory.unitCount
            
    print(players, file=sys.stderr)
    return localallFactories

"""
Simulates situation for a given base
    - returns a couple (t, nbUnits), that means we need to put nbUnit troops before t time not to loose it 
"""
def ToProtectFactory(factory):
   
    allTroops = {**myTroops, **theirTroops}
    allTroopsOnWay = sorted([troop for (_, troop) in allTroops.items() if troop.target == factory.entityId], key=lambda x: x.eta)
    
    eta = 0    
    print("ToProtectFactory: ", factory.entityId, file=sys.stderr)
    troopsOnFactory = factory.unitCount
    factoryDisabled = factory.disabled
    print("> troops on factory: ", troopsOnFactory, file=sys.stderr)
    print("> all troops on the way: ", allTroopsOnWay, file=sys.stderr)
    for troop in allTroopsOnWay:
        print("> troop on way: ", troop, file=sys.stderr)
        localEta = troop.eta - eta   
        if localEta - factoryDisabled < 0:
            factoryDisabled -= localEta
        else:
            troopsOnFactory += (localEta - factoryDisabled)  * factory.production
            factoryDisabled = 0
        print("> troops on factory at T: ", troopsOnFactory, file=sys.stderr)
        if troop.owner == 1: # accumulates
            troopsOnFactory += troop.unitCount
        else: # fight
            if troopsOnFactory - troop.unitCount < 0: # at this time, we loose factory
                return (troop.eta, (troop.unitCount - troopsOnFactory) + 1)
            else:
                troopsOnFactory -= troop.unitCount
        eta = troop.eta
    print("we should not ge here", file=sys.stderr)
    return (eta, 0)

def ToEvolve(factory):

    if factory.unitCount < 10:
        return 
    if factory.disabled:
        return 

    allTroops = {**myTroops, **theirTroops }
    allTroopsOnWay = sorted([troop for (_, troop) in allTroops.items() if troop.target == factory.entityId], key=lambda x: x.eta)
        
    eta = 0    
    print("ToEvolveFactory: ", factory.entityId, file=sys.stderr)
    troopsOnFactory = factory.unitCount - 10 
    for troop in allTroopsOnWay:
        localEta = troop.eta - eta   
        troopsOnFactory += (localEta  * factory.production)                    
        print("> troops on factory at T: ", troopsOnFactory, file=sys.stderr)
        if troop.owner == 1: # accumulates
            troopsOnFactory += troop.unitCount
        else: # fight
            if troopsOnFactory - troop.unitCount < 0: # at this time, we loose factory
                return False
            else:
                troopsOnFactory -= troop.unitCount
        eta = troop.eta
    return True

def ToAttack(factory, target):
    
    if factory.disabled:
        return -1
        
    allTroops = {**myTroops, **theirTroops }
    allTroopsOnWay = sorted([troop for (_, troop) in allTroops.items() if troop.target == factory.entityId], key=lambda x: x.eta)

    to_target_eta = globalMap[factory.entityId][target]
    max_eta = to_target_eta
            
    targetFactory = theirFactories[target]
    if not targetFactory.isNeutral:
        sure_to_get_the_base_at_t0 = to_target_eta * targetFactory.production  + targetFactory.unitCount + 1 - min(targetFactory.disabled, max_eta) * targetFactory.production
    else:
        sure_to_get_the_base_at_t0 = targetFactory.unitCount + 1
     
    print("factory % to attack %d= ", (factory.entityId, target), file=sys.stderr)
    print("max eta= ", max_eta, file=sys.stderr)
    for troop in allTroopsOnWay:
        
        if troop.eta >= max_eta:
            return sure_to_get_the_base_at_t0
                
        if troop.owner == 1: # accumulates
            sure_to_get_the_base_at_t0 -= troop.unitCount # troops already on way
            print("update: sure_to_get_the_base_at_t0= ", sure_to_get_the_base_at_t0, file=sys.stderr)
        else: # fight
            sure_to_get_the_base_at_t0 += troop.unitCount        
    
    return sure_to_get_the_base_at_t0    

class Game:
    
    def init(self):
        self.simulation = simulate()
        self.commands = ["WAIT"]
        
        self.ourBases = [factory for (_,factory) in myFactories.items()]
        self.onRisk = sorted([factory for (_, factory) in self.simulation.items() if factory in self.ourBases and factory.owner == -1], key=lambda x : x.unitCount) # means we loose it        
        self.isInRisk = True if len(self.onRisk) > 0 else False         
        self.toProtect = { factory.entityId : ToProtectFactory(myFactories[factory.entityId]) for factory in self.onRisk }
        
    def rateOfInterest(self, fid, eta):
        factory = myFactories[fid] if fid in myFactories else theirFactories[fid]
        #if factory.isNeutral:
        return -eta
        #else:
        #return -((fact.production * eta) + factory.unitCount - (fact.disabled * eta))            
        
    def findProtection(self):
        
        res = ""
        for protectId, troopInfo in self.toProtect.items():
            neighbours = [x for x in graphset[protectId] if x[0] in myFactories and x[0] not in self.toProtect.keys() and x[1] <= troopInfo[0]] # select nearest my factories to get available resources
            needed_xi = troopInfo[1]
            for neighbour in neighbours:
                factoryId = neighbour[0]
                tosend = min(needed_xi, myFactories[factoryId].unitCount)
                res += ("; MOVE %d %d %d" % (factoryId, protectId, max(0, tosend)))
                print("findProtection=> MOVE %d %d %d" % (factoryId, protectId, max(0, tosend)), file=sys.stderr)
                needed_xi -= tosend
                myFactories[factoryId].unitCount -= tosend
                if needed_xi == 0:
                    break
        return res  
            
    def evolve(self, commands):
        for k, base in myFactories.items():   
            print("k:", k, file=sys.stderr)         
            if k not in self.toProtect.keys():
                if ToEvolve(base):
                    print("evolve: ", k, file=sys.stderr)
                    commands += "; INC %d" % k
                    
    def availableResources(self, myBasesId):   
    
        allTroops = {**myTroops, **theirTroops}
        availableResourcesById =  dict()
        
        for myBaseId in myBasesId:                
            allTroopsOnWay = sorted([troop for (_, troop) in allTroops.items() if troop.target == myBaseId], key=lambda x: x.eta)
            
            myBase = myFactories[myBaseId]
            localMinimums = []
            eta = 0    
            print("compute>availableResources> for ", myBaseId, file=sys.stderr)
            troopsOnFactory = myBase.unitCount
            factoryDisabled = myBase.disabled
            print("compute>availableResources>", myBaseId , "> ", troopsOnFactory, file=sys.stderr)
            
            if len(allTroopsOnWay) == 0:
                localMinimums.append(troopsOnFactory)
        
            for troop in allTroopsOnWay:
                print("compute>availableResources>", myBaseId , ">on way> ", troop, file=sys.stderr)
                localEta = troop.eta - eta   
                if localEta - factoryDisabled < 0:
                    factoryDisabled -= localEta
                else:
                    troopsOnFactory += (localEta - factoryDisabled)  * myBase.production
                    factoryDisabled = 0
                print("compute>availableResources>", myBaseId , "> troops @ T: ", troopsOnFactory, file=sys.stderr)
        
                if troop.owner == 1: # accumulates
                    troopsOnFactory += troop.unitCount
                    localMinimums.append(troopsOnFactory)
                else: # fight
                    if troopsOnFactory - troop.unitCount < 0: # at this time, we loose factory
                        localMinimums.append(0)
                        break 
                    else:
                        troopsOnFactory -= troop.unitCount
                        localMinimums.append(troopsOnFactory)            
                eta = troop.eta
            availableResourcesById[myBaseId] = min(localMinimums)         
        return availableResourcesById
    
       
    def computePrenablesAtRate(self, targetsByRate):
        
        allTroops = {**myTroops, **theirTroops} 
        prenableAtRate = dict()     
        
        for targetId in targetsByRate:

            allTroopsOnWay = sorted([troop for (_, troop) in allTroops.items() if troop.target == targetId], key=lambda x: x.eta, reverse=True)
            
            tBase = theirFactories[targetId]
            eta = 0    
            print("compute>computePrenablesAtRate> for ", targetId, file=sys.stderr)
            troopsOnFactory = tBase.unitCount
            factoryDisabled = tBase.disabled
            print("compute>computePrenablesAtRate>", targetId , "> ", troopsOnFactory, file=sys.stderr)
            
            minimum = troopsOnFactory + 1
        
            for troop in allTroopsOnWay:
                print("compute>computePrenablesAtRate>", targetId , ">on way> ", troop, file=sys.stderr)
                localEta = troop.eta - eta   
                if localEta - factoryDisabled < 0:
                    factoryDisabled -= localEta
                else:
                    troopsOnFactory += 0 if tBase.isNeutral else (localEta - factoryDisabled)  * tBase.production
                    factoryDisabled = 0
                print("compute>computePrenablesAtRate>", targetId , "> troops @ T: ", troopsOnFactory, file=sys.stderr)
        
                if troop.owner == -1: # accumulates
                    troopsOnFactory += troop.unitCount
                    minimum += troop.unitCount
                else: # fight
                    minimum -= troop.unitCount
                    troopsOnFactory -= troop.unitCount
                    if troopsOnFactory < 0: # at this time, we loose factory
                        prenableAtRate[targetId] = minimum 
                        break                     
                                                    
                eta = troop.eta
            prenableAtRate[targetId] = minimum  
        return prenableAtRate
       
#     def ratingTheirFactory(self, factoryId):  #### seems not good
#         tf = theirFactories[factoryId]
#         #if tf.isNeutral or tf.disabled > 0:
#         #    return tf.unitCount
#         rating = 100 * tf.production - tf.unitCount
#         print("rating for ", tf.entityId, "= ", rating, file=sys.stderr)
#         return rating 
#         
    def desertStorm(self):        
        
        # mutual protection first
        print("inRisk= ", self.isInRisk, file=sys.stderr)
        if self.isInRisk:
            self.commands += self.findProtection()            
        
        myBases = [entityId for (entityId,_) in myFactories.items() if entityId not in self.toProtect.keys()]
        targetsByRate = [entityId for (entityId,_) in theirFactories.items()]       
        
        soldiersAvailableByBase = self.availableResources(myBases) 
        print("soldiersAvailableByBase> ", soldiersAvailableByBase, file=sys.stderr)
        
        prenableAtRate = self.computePrenablesAtRate(targetsByRate)
        print("whatToTake> ", prenableAtRate, file=sys.stderr)
        print("orderOfRating-> ", targets, file=sys.stderr)
        
        for myBaseId in myBases:
            
            print("current> ", myBaseId, file=sys.stderr)
            
            if myFactories[myBaseId].unitCount == 0:
                continue                     
        
            ennemyTargets = [ x for x in neighbours if x[0] in targetsByRate]                                    
            print("ennemyTargets: ", ennemyTargets, file=sys.stderr)
            for ennemyBase in ennemyTargets:
                                
                ennemyBaseId = ennemyBase[0]
                print("target>> ", ennemyBaseId, file=sys.stderr)
                                                                            
                src = myFactories[myBaseId]                            
                prepareCommando = ToAttack(src, ennemyBaseId)                                
                    
                print("src pop= ", src.unitCount, file=sys.stderr)
                print("prepareCommando: ", prepareCommando, file=sys.stderr)
                          
                if prepareCommando > 0 and src.unitCount >= prepareCommando: # forbid mutual attack !!! think about it
                    tosend = min(src.unitCount, prepareCommando) # decrease pop
                    src.unitCount -= tosend
                    if tosend > 0: 
                        print("tosend: ", tosend, file=sys.stderr)
                        self.commands += "; MOVE %d %d %d" % (myBaseId, ennemyBaseId, tosend)
                        #simu.add(neibId)
                
                    
        bombNearestBig(self.commands)
        self.evolve(self.commands)
        self.commands += "; MSG (╯°□°）╯︵ ┻━┻"
        
        return "".join(self.commands)
     

"""
BOMB
"""
def bombNearestBig(commands):
    
    
    theirfactByRate = sorted(theirFactories.values(), key=lambda k: rate(k.entityId), reverse=True)
    targets = [x for x in theirfactByRate if (not x.isNeutral and x.production >= 2)]
    if len(targets) == 0: 
        return    
    for target in targets:
        print("ooo> target: ", target, file=sys.stderr)
        
        if target.disabled > 0:
            print("factory disabled: %d" % target.disabled, file=sys.stderr)
            continue             
        
        for _, bomb in bombs.items():
            print("xxx> bomb: ", bomb, file=sys.stderr)
            if bomb.target == -1:
                continue
            if bomb.target == target.entityId:
                print("avoid %d because already target", bomb.target, file=sys.stderr)
                return 
            
        #print("BOMB target: ", target, file=sys.stderr)

        nodes = sorted(graphset[target.entityId], key=lambda x : x[1])
        #print("NODES ", nodes, file=sys.stderr)
        filtered = [x for x in  nodes if x[0] in myFactories]    
        #print("FILTERED ", filtered, file=sys.stderr)
        source = filtered[0]
        #print("FROM: ", source, file=sys.stderr)
        commands += "; BOMB %d %d" % (source[0], target.entityId)
    
        break


"""
EVOLVE
"""
def computeMyPopulation():
    unitCount = 0
    for _, factory in myFactories.items():
        unitCount += factory.unitCount
    for _, troop in myTroops.items():
        unitCount += troop.unitCount    
    return unitCount

def computetheirPopulation():
    unitCount = 0
    for _, factory in theirFactories.items():
        if factory.isNeutral: 
            continue
        unitCount += factory.unitCount        
    for _, troop in theirTroops.items():
        unitCount += troop.unitCount    
    return unitCount

def evolveFactory(factory, commands):
    if factory.unitCount > 10:
        #attacked, nb = needReinforcement(factory.entityId)
        #if not attacked or (attacked and nb <= -10):
        commands += "; INC %d" % factory.entityId
        print("INC %d" % factory.entityId, file=sys.stderr)
        return True
    return False

    
'''
*******************
Main program
*******************
'''

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
buildGraphcet()

print("game creation...", file=sys.stderr)
game = Game()

# game loop
while True:
    
    myFactories = {}
    theirFactories = {}
    
    myTroops = {}
    theirTroops = {}
    
    bombs = {}
    
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
                theirFactories[entity_id] = Factory(entity_id, arg_2, arg_3, arg_4, -1)
                allFactories[entity_id] = theirFactories[entity_id];
            elif arg_1 == 0:
                theirFactories[entity_id] = Factory(entity_id, arg_2, arg_3, arg_4, 0)
                allFactories[entity_id] = theirFactories[entity_id];
            elif arg_1 == 1:
                myFactories[entity_id] = Factory(entity_id, arg_2, arg_3, arg_4, 1)
                allFactories[entity_id] = myFactories[entity_id];
        elif entity_type == "TROOP":
            troop = Troop(entity_id, arg_2, arg_3, arg_4, arg_5, arg_1)
            if arg_1 == -1:
                theirTroops[entity_id] = troop
            elif arg_1 == 1:
                myTroops[entity_id] = troop
        elif entity_type == "BOMB":
            bombs[entity_id] = Bomb(entity_id, arg_2, arg_3, arg_4, arg_1)
    
    for _, fact in myFactories.items():
        print(fact, file=sys.stderr)

    print("-- simulation --", file=sys.stderr)
    #simulate()
    game.init()
    print(game.desertStorm())
