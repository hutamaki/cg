#pragma GCC optimize "O3,omit-frame-pointer,inline"
#include <iostream>
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <map>

using namespace std;

struct Unit
{
    int x;
    int y;

    Unit() {}

    Unit(int x_, int y_, int maze_width)
    {
        x = x_;
        y = y_;

        hashCode = maze_width * y + x;
    }

    void recomputeHash(int maze_width)
    {
        hashCode = maze_width * y + x;
    }

    bool operator<(const  Unit& unit) const
    {
        return hashCode < unit.hashCode;
    }

    int hashCode;
};

struct Player : public Unit
{
    int wallsLeft; // number of walls available for the player
};

struct Board
{
    int w; // width of the board
    int h; // height of the board

    std::map<Unit, std::vector<Unit> > themap;

    Board(int w_, int h_)
    {
        w = w_;
        h = h_;

        generatePaths();
    }

    void generatePaths()
    {
        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {

                auto& listOfMoves(themap[Unit(x, y, w)]);
                if (x - 1 >= 0)
                {
                   // std::cerr << "LEFT" << std::endl;
                    listOfMoves.push_back(Unit(x - 1, y, w)); // LEFT
                }
                if (x + 1 < w)
                {
                    //std::cerr << "RIGHT" << std::endl;
                    listOfMoves.push_back(Unit(x + 1, y, w)); // RIGHT
                }
                if (y - 1 >= 0)
                {
                    //std::cerr << "UP" << std::endl;
                    listOfMoves.push_back(Unit(x, y - 1, w)); // UP
                }
                if (y + 1 <= h)
                {
                    //std::cerr << "DOWN" << std::endl;
                    listOfMoves.push_back(Unit(x, y + 1, w)); // DOWN
                }

                if (x == 0 && y == 2) {
                    std::cerr << "size: " << listOfMoves.size() << std::endl;
                }
            }
        }
    }
};

struct Wall : public Unit
{
    Wall() : Unit(0, 0, 0)
    {
        V = H = false;
    }

    Wall(int x, int y, int w, int v) : Unit(x, y, w)
    {
        V = H = false;
    }

    bool V;
    bool H;
};


struct Game
{
    int nbPlayers; // number of players (2 or 3)
    int myId; // id of my player (0 = 1st player, 1 = 2nd player, ...)
    Board board;
    std::vector<Player> players;
    std::map<Unit, Wall> walls;

    Game(int w, int h, int nbPlayers_, int myId_) : board(w, h)
    {
        players.resize(nbPlayers_);

        nbPlayers = nbPlayers_;
        myId = myId_;
    }

    inline Player& getMe()
    {
        return players.at(myId);
    }

    inline Player& getOther()
    {
        if (myId == 0)
        {
            return players.at(1);
        }
        else
        {
            return players.at(0);
        }
    }

    void addWall(int x, int y, char orientation)
    {
        auto& wall(walls[Unit(x, y, board.w)]);
        wall.x = x;
        wall.y = y;
        wall.recomputeHash(board.w);
        if (orientation == 'H') wall.H = true;
        else wall.V = true;
    }

    bool isBlocked(int a, int b, int c, int d, bool V)
    {
        auto& firstWallPart(walls[Unit(a, b, board.w)]);
        if (V)
        {
            if (firstWallPart.V) return true;
        }
        else
        {
            if (firstWallPart.H) return true;
        }

        auto& secondWallPart(walls[Unit(c, d, board.w)]);
        if (V)
        {
            if (secondWallPart.V) return true;
        }
        else
        {
            if (secondWallPart.H) return true;
        }
        return false;
    }

    bool isNextMoveValid(const Unit& from, const Unit& to)
    {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        if (dx == 0)
        {
            if (dy < 0)   // UP
            {
                // H (x,y) (x-1,y)
                return isBlocked(from.x, from.y, from.x-1, from.y, false); // false means H
            }
            else     // DOWN
            {
                // H (x,y-1) (x-1,y-1)
                return isBlocked(from.x, from.y-1, from.x-1, from.y-1, false);
            }
        }
        else // implies dy == 0
        {
            if (dx < 0)   // LEFT V (x,y)(x,y-1)
            {
                return isBlocked(from.x, from.y, from.x, from.y-1, true);
            }
            else     // RIGHT V (x+1,y)(x+1,y-1)
            {
                return isBlocked(from.x+1, from.y, from.x+1, from.y-1, true);
            }
        }
    }
};


int main()
{
    bool walled(false);
    bool first(true);

    int w, h, nbPlayers, myId;
    cin >> w >> h >> nbPlayers >> myId;
    cin.ignore();

    Game game(w, h, nbPlayers, myId);
    while (1)
    {
        for (int i = 0; i < game.nbPlayers; i++)
        {
            auto& player(game.players.at(i));
            cin >> player.x >> player.y >> player.wallsLeft;
            player.recomputeHash(game.board.w);
            cin.ignore();
        }
        int wallCount; // number of walls on the board
        cin >> wallCount;
        cin.ignore();
        for (int i = 0; i < wallCount; i++)
        {
            int wallx, wally;
            char orientation;
            cin >> wallx >> wally >> orientation;
            cin.ignore();
            game.addWall(wallx, wally, orientation);
        }

       // if (game.myId == 0)  cout << "RIGHT" << endl;
        //else
        {
            Player& me(game.getMe());
            std::cerr << "mypos: " << me.x << " " << me.y << std::endl;
            std::vector<Unit>& possibleMoves(game.board.themap[me]);

            std::cerr << "possible moves: " << possibleMoves.size() << std::endl;
            for (const Unit& move : possibleMoves)
            {
                std::cerr << "x= " << move.x << " y= " << move.y << std::endl;
                std::cerr << "isIt a possibleMove: " << game.isNextMoveValid(me, move)  << std::endl;
            }

            if (game.myId == 0)  {


                cout << "RIGHT" << endl;
            }
            else {
                std::cout << "LEFT" << std::endl;
            }

            /*else
            {
                auto& me(game.getMe());
                auto& other(game.getOther());

                // we start at right)
                if (me.x >= other.x && !walled)
                {
                    // try to know if we are above or below)
                    if (me.y <= other.y)
                    {

                        // if we are near
                        if (other.y - me.y < 2)
                        {
                            walled = true;
                            std::cout << me.x + 1 << " " << me.y + 1 <<  " V" << std::endl;
                        }
                        else
                        {
                            std::cout << "DOWN" << std::endl;
                        }
                    }
                    else
                    {
                        // if we are near
                        if (me.y - other.y < 2)
                        {
                            walled = true;
                            std::cout << me.x - 1 << " " << me.y - 1 <<  " V" << std::endl;
                        }
                        else
                        {
                            std::cout << "UP" << std::endl;
                        }
                    }
                }
                std::cout << "LEFT" << std::endl;
            }*/

        }
    }
}
