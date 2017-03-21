#pragma GCC optimize("-O3")
#pragma GCC optimize("inline")
#pragma GCC optimize("omit-frame-pointer")
#pragma GCC optimize("unroll-loops")

#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <map>
#include <climits>
#include <stack>
#include <functional>
#include <unordered_map>
#include <set>
#include <queue>
#include <chrono>
#include <ctime>

using namespace std;

template <typename T> class Node
{
public:
    T* node;
    Node<T>* parent;
    int f, x, g, h;

    Node<T>(T& node_) : node(&node_), parent(nullptr), f(0), x(0), g(0), h(0) { }
};

class Plot
{
private:
    Plot(int x_, int y_, int width) : x(x_), y(y_), hash(y_ * width + x_) {}
public:
    static Plot* create(int x, int y, int width)
    {
        return new Plot(x, y, width);
    }
    int x, y;

    int Id() const
    {
        return hash;
    }

private:
    int hash;

};

bool operator==(const Plot& src, const Plot& dst)
{
    return (src.x == dst.x && src.y == dst.y);
}


typedef std::map<Plot, std::vector<Plot> > Path;

///
/// template<class T> using min_heap = priority_queue<T, std::vector<T>, std::greater<T>>;

class Maze
{
public:
    std::map<Plot*, std::vector<Plot*> > maze;
    std::vector<Plot*> cache;

    int width;
    int height;

    Maze(int w, int h) : width(w), height(h)
    {
        cache.resize(w * h);
    }

    ~Maze()
    {
        // TODO delete keys of map
        for (int i = 0; i < width * height; i++)
        {
            delete cache[i];
        }
    }

    int getMaxPlots()
    {
        return width * height;
    }

    void buildMaze()
    {
        for (int  y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {

                Plot *coord = buildAndCacheP(x, y);
                //std::cout << "creating: " << coord << " for (" << x  << ", " << y << ")" << std::endl;
                auto& line(maze[coord]);
                if (x - 1 >= 0)
                {
                    line.push_back(buildAndCacheP(x-1, y)); // LEFT
                }
                if (y - 1 >= 0)
                {
                    ;
                    line.push_back(buildAndCacheP(x, y-1)); // UP
                }
                if (x + 1 < width)
                {
                    line.push_back(buildAndCacheP(x+1, y)); // RIGHT
                }
                if (y + 1 < height)
                {
                    line.push_back(buildAndCacheP(x, y+1)); // DOWN
                }
                //std::cout << "line.size: " << line.size() << std::endl;
            }
        }
    }

    Plot* getPlot(int x, int y)
    {
        return cache[y * width + x];
    }

private:
    Plot* buildAndCacheP(int x, int y)
    {
        int linear_index = y * width + x;
        Plot* cachedPtr(cache[linear_index]);
        if (cachedPtr != nullptr)
        {
            //std::cout << "from cache: " << cachedPtr << std::endl;
            return cachedPtr;
        }
        Plot *p = Plot::create(x, y, width);
        cache[p->Id()] = p;
        //std::cout << "buildAndCache: " << cache[p->Id()]  << " => " << x << ", " << y << std::endl;
        return p;
    }
};

template <typename T> struct compare
{
    bool operator()(const Node<T>* l, const Node<T>* r)
    {
        return l->f < r->f;
    }
};

class BFS
{
public:
    Node<Plot>* search(std::map<Plot*, std::vector<Plot*> >& path, Plot& start, Plot& goal, int nbNodes)
    {
        std::queue<Node<Plot>* > queue;
        std::vector<Plot*> nodes(nbNodes, nullptr);

        Node<Plot>* nStart = new Node<Plot>(start);
        queue.push(nStart);

        while (!queue.empty())
        {
            Node<Plot> *current = queue.front();
            queue.pop();

            nodes[current->node->Id()] = current->node; // mark as visited

            std::vector<Plot*>& neighbours(path[current->node]);
            for (Plot *p : neighbours)
            {

                Node<Plot>* item = new Node<Plot>(*p);
                item->parent = current;
                item->x = current->x + 1;

                if (nodes[p->Id()] != nullptr)
                    continue;

                nodes[p->Id()] = p; // mark as visited

                if (p == &goal)
                {
                    return item;
                }
                queue.push(item);
            }
        }
        return nullptr;
    }
};

class DFS
{
public:
    Node<Plot>* search(std::map<Plot*, std::vector<Plot*> >& path, Plot& start, Plot& goal, int nbNodes)
    {
        std::stack<Node<Plot>* > queue;
        std::vector<Plot*> nodes(nbNodes, nullptr);

        Node<Plot>* nStart = new Node<Plot>(start);
        queue.push(nStart);

        while (!queue.empty())
        {
            Node<Plot> *current = queue.top();
            queue.pop();

            nodes[current->node->Id()] = current->node; // mark as visited

            std::vector<Plot*>& neighbours(path[current->node]);
            for (Plot *p : neighbours)
            {

                Node<Plot>* item = new Node<Plot>(*p);
                item->parent = current;
                item->x = current->x + 1;

                if (nodes[p->Id()] != nullptr)
                    continue;

                nodes[p->Id()] = p; // mark as visited

                if (p == &goal)
                {
                    return item;
                }
                queue.push(item);
            }
        }
        return nullptr;
    }
};




class Astar
{

    //typedef std::priority_queue< Node<Plot>*, std::vector<Node<Plot>*>, compare<Node<Plot>*> > PriorityQueue;
    typedef std::set<Node<Plot>*, compare<Plot> > PriorityQueue;

public:
    int manhattan_distance(Plot* a,  Plot* b)
    {
        return abs(b->x - a->x) + abs(b->y - a->y);
    }


    Node<Plot>* search(std::map<Plot*, std::vector<Plot*> >& path, Plot& start, Plot& goal)
    {
        Node<Plot>* node_goal = new Node<Plot>(goal);
        Node<Plot>* node_start = new Node<Plot>(start);

        PriorityQueue queue;
        std::unordered_map<Plot*, int> closeList;
        queue.insert(node_start);
        int iter = 0;

        while (!queue.empty())
        {
            iter++;
            Node<Plot> *current(*(queue.begin()));
            queue.erase(queue.begin());

            std::vector<Plot*>& neighbours(path[current->node]);
            for (Plot *p : neighbours)
            {

                Node<Plot>* next = new Node<Plot>(*p);
                next->g = current->g + manhattan_distance(current->node, p);
                next->h = manhattan_distance(p, &goal);
                next->f = next->g + next->h;
                next->x = current->x + 1; // nb turns

                next->parent = current;

                if (p == node_goal->node)
                {
                    std::cout << "nbIter: " << iter << std::endl;
                    return next;
                }

                if (isWorthTrying(next, queue, closeList))
                {
                    queue.insert(next);
                }
            }
            closeList[current->node] = current->f;
            delete current;
        }
        return nullptr;
    }

    bool isWorthTrying(Node<Plot>* node, PriorityQueue& pqueue, std::unordered_map<Plot*, int>& closeList)
    {
        auto found(pqueue.find(node));
        if (found != pqueue.end())
        {
            if ((*found)->f < node->f) return false;
            else
            {
                Node<Plot>* n(*found);
                pqueue.erase(found);
                delete n;
            }
        }

        auto cl_found(closeList.find(node->node));
        if (cl_found != closeList.end())
        {
            if (cl_found->second < node->f) return false;
            else closeList.erase(cl_found);
        }
        return true;
    }
};

void evaluate_astar()
{
    Maze maze(9, 9);
    maze.buildMaze();

    Astar   astar;
    Plot    *begin(maze.getPlot(0, 0));
    Plot    *goal(maze.getPlot(8, 8));

    std::cout << "AStar "<< std::endl << "begin: " << begin << std::endl;

    bool res = begin == goal;

    std::chrono::time_point<std::chrono::system_clock> start, end;
    start = std::chrono::system_clock::now();

    auto node = astar.search(maze.maze, *begin, *goal);

    end = std::chrono::system_clock::now();
    int elapsed_seconds = std::chrono::duration_cast<std::chrono::nanoseconds> (end-start).count();
    std::time_t end_time = std::chrono::system_clock::to_time_t(end);

    std::cout << "path.length: " << node->x << "=> (" << node->node->x << ", " << node->node->y << ")" << std::endl;
    std::cout << "finished computation at " << std::ctime(&end_time) << "elapsed time: " << elapsed_seconds << "s\n";
}

void evaluate_bfs()
{
    Maze maze(9, 9);
    maze.buildMaze();

    BFS     bfs;
    Plot    *begin(maze.getPlot(0, 0));
    Plot    *goal(maze.getPlot(8, 8));

    std::cout << "BFS" << std::endl << "begin: " << begin << std::endl;

    bool res = begin == goal;

    std::chrono::time_point<std::chrono::system_clock> start, end;
    start = std::chrono::system_clock::now();

    auto node = bfs.search(maze.maze, *begin, *goal, maze.getMaxPlots());

    end = std::chrono::system_clock::now();
    int elapsed_seconds = std::chrono::duration_cast<std::chrono::nanoseconds> (end-start).count();
    std::time_t end_time = std::chrono::system_clock::to_time_t(end);

    std::cout << "path.length: " << node->x << "=> (" << node->node->x << ", " << node->node->y << ")" << std::endl;
    std::cout << "finished computation at " << std::ctime(&end_time) << "elapsed time: " << elapsed_seconds << "s\n";
}

void evaluate_dfs()
{
    Maze maze(9, 9);
    maze.buildMaze();

    DFS     dfs;
    Plot    *begin(maze.getPlot(0, 0));
    Plot    *goal(maze.getPlot(8, 8));

    std::cout << "DFS" << std::endl << "begin: " << begin << std::endl;

    bool res = begin == goal;

    std::chrono::time_point<std::chrono::system_clock> start, end;
    start = std::chrono::system_clock::now();

    auto node = dfs.search(maze.maze, *begin, *goal, maze.getMaxPlots());

    end = std::chrono::system_clock::now();
    int elapsed_seconds = std::chrono::duration_cast<std::chrono::nanoseconds> (end-start).count();
    std::time_t end_time = std::chrono::system_clock::to_time_t(end);

    std::cout << "path.length: " << node->x << "=> (" << node->node->x << ", " << node->node->y << ")" << std::endl;
    std::cout << "finished computation at " << std::ctime(&end_time) << "elapsed time: " << elapsed_seconds << "s\n";
}



/*int main() {
    evaluate_astar();
    evaluate_bfs();
    evaluate_dfs();
    return 0;
}*/

int calcMin(std::vector<int>& elevators, int clonePos)
{
    int value(INT_MAX);
    int diff(0);

    for(auto elevator(elevators.begin()); elevator != elevators.end(); elevator++)
    {
        auto val(abs(*elevator - clonePos));
        if (val < value)
        {
            value = val;
            diff = *elevator - clonePos;
        }
    }
    return diff;
}



/*class Levels // the birst of all elevators
  {
  private:
  int id;
  std::map<int, std::vector<int> >& elevators;

  public:

  void addElevator(

  }

  void dfs(std::map<int, std::vector<int> >& elevators, clonePos)
  {
  std::deque<int>
*/
enum Direction
{
    LEFT = 0,
    RIGHT = 1
};

enum MoveType
{
    ELEVATOR,
    BLOCK,
    WALK,
    EXIT
};

class Unit
{
public:
    int x, y, nb;
    MoveType mt;
    Direction dn;

public:
    Unit(int x_, int y_, int width_) : x(x_), y(y_)
    {
        id = y * width_ + x;
        cost = -1;
    }

    inline Direction direction() const {
        return dn;
    }

    inline void setDirection(Direction direction_) {
        dn = direction_;
    }

    inline MoveType moveType() const {
        return mt;
    }

    inline setMoveType(MoveType moveType_) {
        mt = moveType_;
    }

    inline int getId() const
    {
        return id;
    }

    inline void setCost(int cost_)
    {
        cost = cost_;
    }

    inline int getCost() const
    {
        return cost;
    }
private:
    int id;
    int cost;
};


class Factory
{
public:
    int width, height;

private:
    std::vector<Unit*>  cachedUnits;
    static Factory *_instance;

    Factory(int width_, int height_) : width(width_), height(height_),
        cachedUnits(width_ * height_, nullptr)
    {
    }

public:
    static void instanceIt(int width_, int height_);
    static Unit* getUnit(int x, int y);
};

Factory* Factory::_instance = nullptr;

void Factory::instanceIt(int width_, int height_)
{
    _instance = new Factory(width_, height_);
}


Unit* Factory::getUnit(int x, int y)
{
    int index(y * _instance->width + x);
    if (_instance->cachedUnits[index] == nullptr)
    {
        _instance->cachedUnits[index] = new Unit(x, y, _instance->width);
    }
    return _instance->cachedUnits[index];
}


typedef std::unordered_map<int, std::vector<int> > Elevator;
typedef std::unordered_map<Unit*, std::vector<Unit*> > Map;

Direction getOpposite(Direction direction) {
    if (direction == LEFT) return RIGHT;
    return LEFT;
}

void addUnit(std::vector<Unit*>& localMoves, int x, int y, int cost, MoveType mt, Direction direction)
{
    Unit *unit = Factory::getUnit(x, y);
    unit->setCost(cost);
    unit->setMoveType(mt);
    unit->setDirection(direction);
    localMoves.push_back(unit);
}

void buildMap(Map& moves, Elevator& elevators, int x, int y, Direction direction, int exitFloor, int exitPos)
{
    std::cerr << "y: " << y << std::endl;
    if (y > exitFloor)
    {
       // std::cout << "level= " << y << " above exit floor, ignoring !" << std::endl;
        return ;
    }

    Unit *current = Factory::getUnit(x, y);
    std::vector<int>& elevatorPositions(elevators[y]);

    int left_min = std::numeric_limits<int>::max();
    int left_pos = -1;
    int right_min = std::numeric_limits<int>::min();
    int right_pos = -1;
    int just_above = -1;


    if (y == exitFloor)
    {
        std::cerr << "adding exit" << std::endl;
        int value = y - exitFloor;
        if (value < 0) // exitFloor is on the right
        {
            int cost = abs(value);
            Direction newDir = Direction::RIGHT;
            if (direction != Direction::RIGHT)
            {
             cost += 3;
             newDir = Direction::RIGHT;
            }
            addUnit(moves[current], exitPos, y + 1, cost, MoveType::EXIT, newDir);
        }
        else
        {
            int cost = value;
            Direction newDir = Direction::LEFT;
            if (direction != Direction::LEFT)
            {
                cost += 3;
                newDir = Direction::LEFT;
            }
            addUnit(moves[current], exitPos, y + 1, cost, MoveType::EXIT, newDir);
        }
        return ;
    }

    std::cout << "(.)=> level= " << y << " nbElevators= " << elevatorPositions.size() << std::endl;
    // find nearest elevators on left & right
    for (int i = 0; i < elevatorPositions.size(); i++)
    {
        int value = elevatorPositions[i] - x;
        if (value == 0)
        {
            std::cout << "value=0" << std::endl;
            just_above = 0;
            addUnit(moves[current], x, y + 1, 0, MoveType::WALK, direction);
        }
        if (value < 0)   // elevator is on the left
        {
            if (value < left_min)
            {
                left_min = value;
                left_pos = elevatorPositions[i];
            }
        }
        else
        {
            if (value > right_min)
            {
                right_min = value;
                right_pos = elevatorPositions[i];
            }
        }
    }

    std::cerr << "(.)=> " << x << ", " << y << " right= " << right_pos << " left= " << left_pos << std::endl;

    MoveType mt = MoveType::WALK;
    Direction dn = direction;
    if (left_pos != -1)   // elevator on left
    {
        int cost = x - left_pos;
        if (direction != LEFT)
        {
            dn = getOpposite(direction);
            mt = BLOCK;
            cost += 3; // add 3 if not the right direction)
        }
        addUnit(moves[current], left_pos, y + 1, cost, mt, dn);
    }

    if (right_pos != -1)   // elevator on right
    {
        int cost = right_pos - x;
        if (direction != RIGHT)
        {
            dn = getOpposite(direction);
            cost += 3;
            mt = BLOCK;
        }
        addUnit(moves[current], right_pos, y + 1, cost, mt, dn);
    }

    if (right_pos == -1 && left_pos == -1 && just_above == -1) // means no elevator, we need to put some
    {
        // no smart strategy yet. if there is no elevator here,
        // just build one where we are
        mt = MoveType::ELEVATOR;
        addUnit(moves[current], x, y + 1, 1, mt, dn); // building an elevator cost 1
        std::cerr << "adding elevator: " << x << ", " << y + 1 << std::endl;
    }


    auto& localMoves(moves[current]);

    std::vector<Unit *> cache(localMoves);
    for (Unit* unit : cache){
        buildMap(moves, elevators, unit->x, unit->y, unit->direction(), exitFloor, exitPos);
    }
}


int main()
{
    /*    int nbFloors; // number of floors
        int width; // width of the area
        int nbRounds; // maximum number of rounds
        int exitFloor; // floor on which the exit is found
        int exitPos; // position of the exit on its floor
        int nbTotalClones; // number of generated clones
        int nbAdditionalElevators; // number of additional elevators that you can build
        int nbElevators; // number of elevators

        cin >> nbFloors >> width >> nbRounds >> exitFloor >> exitPos
            >> nbTotalClones >> nbAdditionalElevators >> nbElevators;

        cin.ignore();

        cerr << "nbFloors: " << nbFloors << endl;
        cerr << "width: " << width << endl;
        cerr << "nbRounds: " << nbRounds << endl;
        cerr << "exitFloor: " << exitFloor << endl;
        cerr << "exit," << exitPos << endl;
        cerr << "nbTotalClones: " << nbTotalClones << endl;
        cerr << "nbAdditionalElevators: " << nbAdditionalElevators << endl;
        cerr << "nbElevators: " << nbElevators << endl;*/

    int nbFloors = 13;
    int width = 69;
    int nbRounds = 109;
    int exitFloor = 11;
    int exitPos = 47;
    int nbTotalClones = 100;
    int nbAdditionalElevators = 4;
    int nbElevators = 36;

    int in_elevators[][2] = {{11,45}, {1,36}, {2,43}, {3,17}, {11,50}, {3,24}, {10,3},
    {10,23}, {6,23}, {8,1}, {2,56}, {2,9}, {1,62}, {4,9}, {2,24}, {3,30}, {6,35}, {1,24}, {11,4},
    {5,4}, {8,63}, {1,4}, {8,9}, {6,3}, {1,17}, {9,2}, {10,45}, {6,9}, {9,17}, {1,50}, {8,23}, {2,3},
    {3,60}, {4,23}, {7,48}, {2,23}};

    Factory::instanceIt(width, nbFloors);

    int floor=0;
    int pos= 6;

    std::unordered_map<int, std::vector<int> > elevators;

    for (int i = 0; i < nbElevators; i++)
    {
        int elevatorFloor; // floor on which this elevator is found
        int elevatorPos; // position of the elevator on its floor
        //cin >> elevatorFloor >> elevatorPos; cin.ignore();
        elevatorFloor = in_elevators[i][0];
        elevatorPos = in_elevators[i][1];
        elevators[elevatorFloor].push_back(elevatorPos);
        cerr << "{ " << elevatorFloor << " ," << elevatorPos << "}" << endl;
    }




    /*  while (1) */
    {
        //int cloneFloor; // floor of the leading clone
        //int clonePos; // position of the leading clone on its floor
        //string direction; // direction of the leading clone: LEFT or RIGHT
        //cin >> cloneFloor >> clonePos >> direction; cin.ignore();

        int cloneFloor = floor;
        int clonePos = pos;
        string direction = "RIGHT";

        cerr << "Clone (floor=" << cloneFloor << ", pos= " << clonePos << "): direction << " << direction << endl;

        // determine if there is an elevator or not
        if (cloneFloor == -1)
        {
            cout << "WAIT" << endl;
        }
        else
        {
            /*if (elevators[cloneFloor].size() == 0 && cloneFloor != exitFloor)
            {
                elevators[cloneFloor].push_back(clonePos);   // constructs one
                cout << "ELEVATOR" << endl;
            }
            else
            {
                int directionToGo(0);
                if (cloneFloor == exitFloor) directionToGo = exitPos - clonePos;
                else directionToGo = calcMin(elevators[cloneFloor], clonePos);

                if (directionToGo < 0 && direction == "RIGHT") cout << "BLOCK" << endl;
                else if (directionToGo > 0 && direction == "LEFT") cout << "BLOCK" << endl;
                else cout << "WAIT" << endl;
            }*/
        }

        Map moves;
        buildMap(moves, elevators, clonePos, cloneFloor, RIGHT, exitFloor, exitPos);

        std::cout << "moves=>" << std::endl;
        std::vector<Unit*> level = moves[Factory::getUnit(6, 0)];
        for (Unit *unit : level) {
            std::cerr << "(.) => " << unit->x << ", " << unit->y << " cost= " << unit->getCost() << std::endl;
        }
    }

    int x;
    cin >> x;
    return 0;
}
