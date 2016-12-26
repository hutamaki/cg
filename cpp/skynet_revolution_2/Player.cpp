    #include <iostream>
    #include <vector>
    #include <algorithm>
    #include <deque>
    #include <climits>

    // # 2895
    class Node 
    {
    public:    
        Node(int index_, Node& parent_) : index(index_), parent(parent_) { }
        inline int getIndex() const { return index;}
        inline Node& getParent() { return parent; }
    private:
        int index;
        Node& parent;
    };

    class Player 
    {
    public:
        Player(int n, int l, int e, std::vector<std::vector<int> >& maze_ , std::vector<bool>& outputs_) : maze(maze_), outputs(outputs_), arities(n, -1)
        {
            N = n;
            L = l;
            E = e;
            computeArity();
            displayArities();
        }

        void gameLoop(int SI) 
        {
        
            int destroyed = -1;
            std::vector<int>& current(maze[SI]);

            // check if one exit node is an output
            for (int value : current)
            {
                if (outputs[value]) 
                {
                    destroyed = value;
                    break ;
                }
            }

            std::cerr << "destroyed? " << destroyed << std::endl;
            if (destroyed != -1) { deletePath(SI, destroyed); }
           else 
           {
                displayArities();
                if (!DeleteIfNextAndArtityToobig(SI)) 
                {
                    std::cerr << "!DeleteIfNextAndArtityToobig(SI)" << std::endl;
                    int maxArity = getMaxArityExitNode();
                    std::vector<int>& possibles(maze[maxArity]);
                    int edge = possibles.at(0);
                    deletePath(edge, maxArity);
                }
            }
        }      

    private:
        void deletePath(int SI, int value) // updates arity
        {
            std::cout << SI << " " << value << std::endl;

            std::vector<int>& current(maze[SI]);
            current.erase(std::find(current.begin(), current.end(), value));
            std::vector<int>& mirror(maze[value]);
            mirror.erase(std::find(mirror.begin(), mirror.end(), SI));

            // update arities
            arities[SI]--;
            arities[value]--;
        }

        // we need to compute arity of exit nodes
        void computeArity() 
        {
            std::cerr << "size of arities: " << arities.size() << std::endl;
            for(int i(0); i < N; i++)
                if (outputs[i]) { arities[i] = maze[i].size(); }
        }

        void displayArities()
        {
            for (int i(0); i < N; i++) 
                if (arities[i] != - 1) std::cerr << "arity for edge " << i << " is " << arities[i] << std::endl; 
        }

        int getMaxArityExitNode() 
        {
            int arityIndex(0);
            int arityValue(-1);
            for(int i(0); i < N; i++)
            {
                if (arities[i] > arityValue)
                {
                    arityValue = arities[i];
                    arityIndex = i;
                }
            }
            return arityIndex;        
        }
        
        int getMaxArityAmongExitNode(std::vector<int>& nodes) 
        {
            int arityIndex(0);
            int arityValue(-1);
            for(int current : nodes)
            {
                if (outputs[current])
                {
                    if (arities[current] > arityValue)
                    {
                        arityValue = arities[current];
                        arityIndex = current;
                    }
                }
            }
            return arityIndex;        
        }

        int countNbOutputs(std::vector<int>& ways) 
        {
            int nbOutputs(0);
            for (int way : ways) { if (outputs[way]) nbOutputs++; }
            return nbOutputs;
        }    

        int BFSPathCount(int SI, int output) 
        {
            std::deque<int> current;
            std::deque<int> next;
            std::vector<bool> tested(N, false);

            std::deque<int>* stack(&current); 
            std::deque<int>* stack_next(&next);   
            
            int backParent(-1);
            int levelCounter(1); // SI != output 

            std::cerr << "BFS: " << SI << " output: " << output << std::endl;
            
            std::vector<int>& adjacencies(maze[output]);

            stack->emplace_back(output);
            while (!stack->empty())
            {            
                int currentNode(stack->front()); // first element is now current element

                std::vector<int>& level(maze[currentNode]); // current node is not the result, we need to consider successors            
                std::cerr << "level " << currentNode << std::endl;
                for(int node : level)
                {
                    std::cerr << "considering " << node << std::endl;
                    std::cerr << "backParent " << currentNode << std::endl;
                    
                    if (node == SI) return levelCounter; 
                    if (!tested[node])
                    {
                        stack_next->emplace_back(node);
                        tested[node] = true;
                    }
                }

                stack->pop_front(); // discard first element
                if (stack->empty())
                {
                    std::swap(stack, stack_next);
                    levelCounter++;    
                    std::cerr << "level up ! (" << levelCounter << ")" << std::endl;
                }                            
            }
            return -1; // means no exit
        }   

        /*
        * Algo: 
        * - Do not touch node with arity 1 unless next move
        * - @each pos compute BFS with every output. If arity > nb move => danger.
        *   => immediate action: eliminate one on the nearest in this case    
        */    
        int GetTheOne(int SI)
        {
            // for each output, we DFS output->SI to compute how many (at least) moves 
            // the virus needs to reach it

            // now we are able to create couples pathCount / arity for each nodes.
            // and thus apply the algo described
            // search for the nearest with arity >= pathCount

            std::vector<int> pathCount(N, -1);
            int minCount(INT_MAX);
            int minOutputId(-1);

            for (int i = 0; i < N; i++)
            {
                if (outputs[i])
                {
                    pathCount[i] = BFSPathCount(SI, i);
                    std::cerr << "pathCount for exit@pos: " << i << "@" << SI << " : " << pathCount[i] << std::endl;

                    if (pathCount[i] < minCount)
                    {
                        if (arities[i] >= pathCount[i]) // now we have a candidate
                        {
                            std::cerr << "hum !" << std::endl;
                            minOutputId = i;   
                        }
                    }
                }
            }
            std::cerr << "minOutputId " << minOutputId << std::endl;
            return minOutputId;
        }

        bool DeleteIfNextAndArtityToobig(int SI) 
        {
            // detect what nodes are at next level
            // if ondes wit arity > 2 -> treat them first
            std::vector<int>& nextLevels(maze[SI]);
            for (int level : nextLevels) 
            {
                std::vector<int>& areOutputs(maze[level]);            
                /*if (countNbOutputs(areOutputs) >= 2) 
                {            
                    /*for (int isOutput : areOutputs) 
                    {
                        if (outputs[isOutput]) 
                        {
                            std::cerr << ">> 1" << std::endl;
                            deletePath(level, isOutput);                        
                            return true;
                        }
                    }*/
                  /*  int idx(getMaxArityAmongExitNode(areOutputs));
                    deletePath(level, idx);
                    return true;
                }*/
                int res = GetTheOne(SI);
                if (res != -1)
                {
                    std::cerr << "deletePath" << std::endl;
                    deletePath(SI, res);
                    return true;
                }

                for (int isOutput : areOutputs) 
                {
                    if (outputs[isOutput]) 
                    {
                        if (arities[isOutput] >= 2) 
                        { 
                             std::cerr << ">> 2" << std::endl;
                            deletePath(level, isOutput); 
                            return true;
                        }
                    }
                }
            }
            std::cerr << "return false" << std::endl;
            return false;
        }


    private:
        int N;
        int L;
        int E;
        std::vector<std::vector<int> >&  maze;
        std::vector<bool>& outputs;
        std::vector<int> arities;
    };
      
    int main()
    {
        int N; // the total number of nodes in the level, including the gateways
        int L; // the number of links
        int E; // the number of exit gateways
        std::cin >> N >> L >> E; std::cin.ignore();

        std::vector<std::vector<int> > maze(N);
        for (int i = 0; i < L; i++) {
            int N1; // N1 and N2 defines a link between these nodes
            int N2;
            std::cin >> N1 >> N2; std::cin.ignore();        
            
            maze[N1].emplace_back(N2);
            maze[N2].emplace_back(N1);
        }
        
        std::vector<bool> outputs(N, false);
        for (int i = 0; i < E; i++) {
            int EI; // the index of a gateway node
            std::cin >> EI; std::cin.ignore();
            outputs[EI] = true;
        }

        Player player(N, L, E, maze, outputs);    
        while (1) 
        {
            int SI; // The index of the node on which the Skynet agent is positioned this turn
             std::cin >> SI;  std::cin.ignore();
            player.gameLoop(SI);
        }
    }