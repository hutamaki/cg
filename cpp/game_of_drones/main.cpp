#pragma GCC optimize("-O3")
#pragma GCC optimize("inline")
#pragma GCC optimize("omit-frame-pointer")
#pragma GCC optimize("unroll-loops")

#include <iostream>
#include <string>
#include <vector>
#include <algorithm>

using namespace std;

constexpr int HEIGHT = 1800;
constexpr int WIDTH = 4000;

// ****************************************************************************************

static unsigned int g_seed;
inline void fast_srand(int seed) {
    //Seed the generator
    g_seed = seed;
}
inline int fastrand() {
    //fastrand routine returns one integer, similar output value range as C lib.
    g_seed = (214013*g_seed+2531011);
    return (g_seed>>16)&0x7FFF;
}
inline int fastRandInt(int maxSize) {
    return fastrand() % maxSize;
}
inline int fastRandInt(int a, int b) {
    return(a + fastRandInt(b - a));
}
inline double fastRandDouble() {
    return static_cast<double>(fastrand()) / 0x7FFF;
}
inline double fastRandDouble(double a, double b) {
    return a + (static_cast<double>(fastrand()) / 0x7FFF)*(b-a);
}

// ****************************************************************************************

class Point {
public:
    double x;
    double y;

    Point() {

    }

    Point(double x, double y) {
        this->x = x;
        this->y = y;
    }
};

inline double dist(double x1, double y1, double x2, double y2) {
    return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
}

inline double dist2(double x1, double y1, double x2, double y2) {
    return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
}

inline double dist(Point *p, double x2, double y2) {
    return dist(p->x, p->y, x2, y2);
}

inline double dist2(Point *p, double x2, double y2) {
    return dist2(p->x, p->y, x2, y2);
}

inline double dist(Point *u1, Point *u2) {
    return dist(u1->x, u1->y, u2->x, u2->y);
}

inline double dist2(Point *u1, Point *u2) {
    return dist2(u1->x, u1->y, u2->x, u2->y);
}


// ****************************************************************************************

class Drones {
public:
    int owner;

    int numberOfDrones = 0;
    Point *points = nullptr;

    Drones(int owner, int numberOfDones) {
        points = new Point[numberOfDones];
    }

    inline void addDrone(int x, int y) {
        Point &p(points[numberOfDrones]);
        p.x = x;
        p.y = y;
        ++numberOfDrones;
    }

    ~Drones() {
        delete[] points;
    }
};


// ****************************************************************************************

class Zone
{
public:
    int x;
    int y;
    int owner;

    Zone() {}

    Zone(int x, int y, int owner) {
        this->x = x;
        this->y = y;
        this->owner = owner;
    }
};



// ****************************************************************************************

class Zones
{
public:
    int nbZones = 0;
    Zone* zones = nullptr;

    Zones(int nbZones) {
        zones = new Zone[nbZones];
    }

    void addZone(int x, int y) {
        Zone& zone(zones[nbZones]);
        zone.x = x;
        zone.y = y;
        nbZones++;
    }

    void updateOwner(int i, int owner) {
        Zone& zone(zones[i]);
        zone.owner = owner;
    }
};

// ****************************************************************************************

class Player
{
public:
    int id;
    Drones *drones;
};

// ****************************************************************************************

Player* players;
Player* myPlayer;
Zones *zones;

int NB_PLAYERS;
int MY_PLAYER_ID;
int NB_DRONES_BY_TEAM;
int NB_ZONES;


// ****************************************************************************************

int main() {

    cin >> NB_PLAYERS >> MY_PLAYER_ID >> NB_DRONES_BY_TEAM >> NB_ZONES;
    cin.ignore();

    players = new Player[NB_PLAYERS];
    zones = new Zones(NB_ZONES);

    for (int i = 0; i < NB_ZONES; i++) {
        int X; // corresponds to the position of the center of a zone. A zone is a circle with a radius of 100 units.
        int Y;
        cin >> X >> Y;
        cin.ignore();

        zones->addZone(X, Y);
    }

    // game loop
    while (1) {
        for (int i = 0; i < NB_ZONES; i++) {
            int TID; // ID of the team controlling the zone (0, 1, 2, or 3) or -1 if it is not controlled. The zones are given in the same order as in the initialization.
            cin >> TID;
            cin.ignore();

            zones->updateOwner(i, TID);
        }

        for (int i = 0; i < NB_PLAYERS; i++) {

            players[i].id = i;
            if (i == MY_PLAYER_ID) {
                myPlayer = &(players[i]);
            }

            players[i].drones = new Drones(i, NB_DRONES_BY_TEAM);
            for (int j = 0; j < NB_DRONES_BY_TEAM; j++) {
                int DX; // The first D lines contain the coordinates of drones of a player with the ID 0, the following D lines those of the drones of player 1, and thus it continues until the last player.
                int DY;
                cin >> DX >> DY;
                cin.ignore();
                players[i].drones->addDrone(DX, DY);

                std::cerr << "player: " << i << " DX= " << DX << "DY= " << DY << std::endl;
            }
        }
        for (int i = 0; i < NB_DRONES_BY_TEAM; i++) {

            // Write an action using cout. DON'T FORGET THE "<< endl"
            // To debug: cerr << "Debug messages..." << endl;


            // output a destination point to be reached by one of your drones.
            // The first line corresponds to the first of your drones that you were provided as input, the next to the second, etc.
            cout << "20 20" << endl;
        }
    }
}