from enum import Enum
import sys

grid_width = 23
grid_height = 21

ship_length = 3
ship_width = 1

class Actions(Enum):
    MOVE = 1,
    WAIT = 2,
    SLOWER = 3

rhum_quantity = [10, 20]

ships = []
myships = []
barrels = []
cannonballs = []
mines = []
target = {}

class Unit(object):

    def __init__(self, id, x, y):
        self.id = id
        self.x = x
        self.y = y

    def __str__(self):
        return "%d (%d, %d)" % (self.id, self.x, self.y)

class Ship(Unit):

    def __init__(self, id, x, y, orientation, speed, quantity, owner):
        super(Ship,self).__init__(id, x, y)

        self.orientation = orientation
        self.speed = speed
        self.quantity = quantity
        self.owner = owner

    def __str__(self):
        return "ship: %s or=%d, sp=%d, rhum=%d, owner=%d" % (super(Ship, self).__str__(), self.orientation, self.speed, self.quantity, self.owner)

class Barrel(Unit):

    def __init__(self, id, x, y, rhum_quantity):
        super(Barrel, self).__init__(id, x, y)

        self.quantity = rhum_quantity

    def __str__(self):
        return "barrel: %s rhum=%d" % (super(Barrel, self).__str__(), self.quantity)

class Canonbal(Unit):

    def __init__(self, id, x, y, source_entityId, eta):
        super(Canonbal, self).__init__(id, x ,y)

        self.source = source_entityId,
        self.eta = eta

    def __str__(self):
        return "canonbal: %s, src=%d, eta=%d" % (super(Canonbal, self).__str__(), self.source, self.eta)

class Mine(Unit):

    def __init__(self, id, x, y):
        super(Mine, self).__init__(id, x, y)

    def __str__(self):
        return "mine: %s" % super(Mine, self).__str__()


def distance(ship, barrel):
    xp1 = ship.x - (ship.y - (ship.y & 1)) / 2
    zp1 = ship.y
    yp1 = -(xp1 + zp1)

    xp2 = barrel.x - (barrel.y - (barrel.y & 1)) / 2
    zp2 = barrel.y
    yp2 = -(xp2 + zp2)

    return (abs(xp1 - xp2) + abs(yp1 - yp2) + abs(zp1 - zp2)) / 2


def getNearest(ship):
    print("computing ...", file=sys.stderr)
    if len(barrels) <= 0:
        return False, None
    result = sorted(barrels, key=lambda x: distance(ship, x))
    barrel = result[0]
    return (True, Unit(-1, barrel.x, barrel.y))

check_for_mines = { 0: Unit(-1, 2, 0),
                    1: Unit(-1, +1, -2),
                    2: Unit(-1, -1, -2),
                    3: Unit(-1, -2, 0),
                    4: Unit(-1, -1, +2),
                    5: Unit(-1, +1, +2)}

def nextMoveButMines(ship, target):
    unit = check_for_mines.get(ship.orientation)
    total_pox = Unit(-1, ship.x + unit.x, ship.y + unit.y)
    for mine in mines:
        if mine.x == total_pox.x and mine.y == total_pox.y:
            print("OOOOOOOOOONNNNNNNNNNOOOOOOOOOZZZZZZZZZ")




while True:
    my_ship_count = int(input())
    entity_count = int(input())

    ships = []
    barrels = []
    myships = []
    mines = []

    for i in range(entity_count):
        entity_id, entity_type, x, y, arg_1, arg_2, arg_3, arg_4 = input().split()
        entity_id = int(entity_id)
        x = int(x)
        y = int(y)
        arg_1 = int(arg_1)
        arg_2 = int(arg_2)
        arg_3 = int(arg_3)
        arg_4 = int(arg_4)

        print("entityType= %s", entity_type, file=sys.stderr)
        if entity_type == "SHIP":
            ship = Ship(entity_id, x, y, arg_1, arg_2, arg_3, arg_4)
            ships.append(ship)
            if ship.owner == 1:
                myships.append(ship)
            print(ship, file=sys.stderr)
        elif entity_type == "BARREL":
            barrel = Barrel(entity_id, x, y, arg_1)
            barrels.append(barrel)
            print(barrel, file=sys.stderr)
        elif entity_type == "CANONBALL":
            cannonball = Canonbal(entity_id, x, y, arg_1, arg_2)
            cannonballs.append(cannonball)
            print(cannonball, file=sys.stderr)
        elif entity_type == "MINE":
            mine = Mine(entity_id, x, y)
            mines.append(mine)
            print(mine, file=sys.stderr)

    for ship in myships:
        if ship.id not in target.keys():
            (isMove, unit) = getNearest(ship)
            if isMove:
                target[ship.id] = unit
            else:
                print("WAIT")
                continue
        unit = target.get(ship.id)
        print("unit: %s" % unit, file=sys.stderr)
        if ship.x == unit.x and ship.y == unit.y:
            (isMove, unit) = getNearest(ship)
            if isMove:
                target[ship.id] = unit
                nextMoveButMines(ship, unit)
                print("MOVE %d %d" % (unit.x, unit.y))
            else:
                print("WAIT")
        else:
            nextMoveButMines(ship, unit)
            print("MOVE %d %d" % (unit.x, unit.y))