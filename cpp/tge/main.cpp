#pragma GCC optimize "O3,omit-frame-pointer,inline"
#include <iostream>
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <map>

using namespace std;

struct Unit {
    int x;
    int y;

    Unit() {}

    Unit(int x_, int y_, int maze_width) {
        x = x_;
        y = y_;

        hashCode = maze_width * y + x;
    }

    bool operator<(const  Unit& unit) const {
        return hashCode < unit.hashCode;
    }

    int hashCode;
};

struct Player : public Unit {
    int wallsLeft; // number of walls available for the player
};

struct Board {
    int w; // width of the board
    int h; // height of the board

    std::map<Unit, std::vector<Unit> > themap;

    Board(int w_, int h_) {
        w = w_;
        h = h_;

        generatePaths();
    }

    void generatePaths() {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                auto& listOfMoves(themap[Unit(x, y, w)]);
                if (x - 1 >= 0) {
                    listOfMoves.push_back(Unit(x - 1, y, w)); // LEFT
                }
                if (x + 1 < w) {
                    listOfMoves.push_back(Unit(x + 1, y, w)); // RIGHT
                }
                if (y - 1 >= 0) {
                    listOfMoves.push_back(Unit(x, y - 1, w)); // UP
                }
                if (y + 1 <= h) {
                    listOfMoves.push_back(Unit(x, y + 1, w)); // DOWN
                }
            }
        }
    }
};

struct Wall : public Unit {

    Wall(int x, int y, int w, int v) : Unit(x, y, w) {
        V = H = false;
    }

    bool V;
    bool H;
};


struct Game {
    int count; // number of players (2 or 3)
    int myId; // id of my player (0 = 1st player, 1 = 2nd player, ...)
    std::vector<Player> players;
    std::map<Unit, std::vector<Wall> > walls;

    inline Player& getMe() {
        return players.at(myId);
    }

    inline Player& getOther() {
        if (myId == 0) {
            return players.at(1);
        } else {
            return players.at(0);
        }
    }

    void addWall(int x, int y, char orientation, int w) {
        walls[Unit(x, y, w)].push_back(Wall(x, y, w, orientation));
    }

    bool isBlocked(int a, int b, int c, int d, bool V) {

        auto& firstWallPart(walls[Unit(a, b, w)]);
        if (V) {
            if (firstWallPart.V) return true;
        } else {
            if (firstWallPart.H) return true;
        }

        auto& secondWallPart(walls[Unit(c, d)]);
        if (V) {
            if (secondWallPart.V) return true;
        } else {
            if (secondWallPart.H) return true;
        }
        return false;
   }

    bool isNextMoveValid(Unit& from, Unit& to) {
        bool isBlocked(false);

        int dx = to.x - from.x;
        int dy = to.y - from.y;
        if (dx == 0)
        {
            if (dy < 0) { // UP
                // H (x,y) (x-1,y)
               return checkWall(from.x, from.y, from.x-1, from.y, false); // false means H
            } else { // DOWN
                // H (x,y-1) (x-1,y-1)
                return checkWall(from.x, from.y-1, from.x-1, from.y-1, false);
            }
        }
        else // implies dy == 0
        {
            if (dx < 0) { // LEFT V (x+1,y)(x+1,y-1)
                return checkWall(from.x+1, from.y, from.x+1, from.y-1, true);
            } else { // RIGHT V (x,y)(x,y-1)
                return checkWall(from.x, from.y, from.x, from.y-1, true);
            }
        }
    }
};


int main()
{
    Game game;

    bool walled(false);
    bool first(true);

    int w, h;
    cin >> w >> h >> game.count >> game.myId; cin.ignore();

    Board board(w, h);
    game.players.resize(game.count);

    while (1) {
        for (int i = 0; i < game.count; i++) {
            auto& player(game.players.at(i));
            cin >> player.x >> player.y >> player.wallsLeft; cin.ignore();
        }
        int wallCount; // number of walls on the board
        cin >> wallCount; cin.ignore();
        for (int i = 0; i < wallCount; i++) {
            int wallx, wally;
            char orientation;
            cin >> wallx >> wally >> orientation; cin.ignore();
            game.addWall(wallx, wally, board.w, orientation);
        }

        // action: LEFT, RIGHT, UP, DOWN or "putX putY putOrientation" to place a wall
        if (game.myId == 0)  cout << "RIGHT" << endl;
        else {

            if (first) {
                std::cout << "LEFT" << std::endl;
                first = false;
            } else {
                auto& me(game.getMe());
                auto& other(game.getOther());

                // we start at right)
                if (me.x >= other.x && !walled)
                {
                    // try to know if we are above or below)
                    if (me.y <= other.y) {

                        // if we are near
                        if (other.y - me.y < 2) {
                            walled = true;
                            std::cout << me.x + 1 << " " << me.y + 1 <<  " V" << std::endl;
                        } else {
                            std::cout << "DOWN" << std::endl;
                        }
                    } else {
                        // if we are near
                        if (me.y - other.y < 2) {
                            walled = true;
                            std::cout << me.x - 1 << " " << me.y - 1 <<  " V" << std::endl;
                        } else {
                            std::cout << "UP" << std::endl;
                        }
                    }
                }
                std::cout << "LEFT" << std::endl;
            }

        }
        //}
}
}
