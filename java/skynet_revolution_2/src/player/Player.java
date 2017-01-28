package player;

// # 2895
import java.util.*;

class Node {
    private final int index;
    private final Node parent;

    public Node(int index, Node parent) {
        this.index = index;
        this.parent = parent;
    }

    public int getIndex() {
        return index;
    }

    public Node getParent() {
        return parent;
    }
}

public class Player {

    private int N;
    private int L;
    private int E;
    private ArrayList[] maze;
    private ArrayList outputs;

    private Hashtable<Integer, Integer> arities = new Hashtable<>();

    public Player(int n, int l, int e, ArrayList[] maze, ArrayList outputs) {
        N = n;
        L = l;
        E = e;
        this.maze = maze;
        this.outputs = outputs;
        computeArity();
    }

    public Player(int n, int l, int e, int[][] maze, int[] outputs) {
        N = n;
        L = l;
        E = e;

        this.maze = new ArrayList[N];
        for (int i = 0; i < N; i++) {
            this.maze[i] = new ArrayList();
        }

        for (int i = 0; i < L; i++) {
            int N1 = maze[i][0];
            int N2 = maze[i][1];

            this.maze[N1].add(N2);
            this.maze[N2].add(N1);
        }

        this.outputs = new ArrayList(E);
        for(int output : outputs) {
            this.outputs.add(output);
        }
        computeArity();
    }

    /*private void displayNodeToDelete(int currentLevel, Node item, int successor) {
        Node ptr = item;
        while (ptr.getParent() != null) {
            Node back = ptr.getParent();
            int back_idx = ptr.getIndex();
            ptr = back;

            if (ptr.getParent() == null) {
                System.out.println(currentLevel + " " + back_idx);

                ArrayList current = maze[currentLevel];
     a           current.remove(current.indexOf(back_idx));

                ArrayList mirror = maze[back_idx];
                mirror.remove(mirror.indexOf(currentLevel));
            }
        }
    }

    private void BreadthFirstSearch(int currentLevel) {
        ArrayDeque queue = new ArrayDeque();
        ArrayDeque marked = new ArrayDeque();

        queue.addLast(new Node(currentLevel, null));
        marked.addLast(currentLevel);

        while (!queue.isEmpty()) {
            Node item = (Node)queue.pollFirst();
            marked.addLast(item.getIndex());

            ArrayList<Integer> successors = maze[item.getIndex()];
            for (int successor : successors) {
                if (marked.contains(successor)) {
                    continue ;
                }

                if (outputs.contains(successor)) {
                    displayNodeToDelete(currentLevel, item, successor);
                    return ;
                }
                else {
                    queue.addLast(new Node(successor, item));
                }
            }
        }
    }*/

    // we need to compute arity of exit nodes
    private void computeArity() {
        for (int i = 0; i < E; i++) {
            // arity for each exit node is length of maze[i].arraylist
            int exitNode = (int)outputs.get(i);
            arities.put(exitNode, maze[exitNode].size());
        }
    }

    private int getMaxArityExitNode() {
        Enumeration<Integer> arit = arities.keys();
        int maxArit = 0;
        int maxAritIndex = 0;
        while (arit.hasMoreElements()) {
            int element = arit.nextElement();
            if (maxArit < arities.get(element)) {
                maxArit = arities.get(element);
                maxAritIndex = element;
            }
        }
        return maxAritIndex;
    }

    private int countNbOutputs(ArrayList<Integer> ways) {
        int nbOutputs = 0;
        for (int way : ways) {
            if (outputs.contains(way)) nbOutputs++;
        }
        return nbOutputs;
    }

    private void deletePath(int nodeIndex, int output) {

        System.out.println(nodeIndex + " " + output);

        ArrayList tmp = maze[nodeIndex];
        tmp.remove(tmp.indexOf(output));

        ArrayList mirror = maze[output];
        mirror.remove(mirror.indexOf(nodeIndex));

        // update arities output
        arities.put(output, arities.get(output) - 1);
    }

    private boolean DeleteIfNextAndArtityToobig(int SI) {
        // detect what nodes are at next level
        // if ondes wit arity > 2 -> treat them first
        ArrayList<Integer> nextLevels = maze[SI];
        for (int level : nextLevels) {
            ArrayList<Integer> areOutputs = maze[level];

            if (countNbOutputs(areOutputs) >= 2) {
                for (int isOutput : areOutputs) {
                    if (outputs.contains(isOutput)) {
                        deletePath(level, isOutput);
                        return true;
                    }
                }
            }

           /* for (int isOutput : areOutputs) {
                if (outputs.contains(isOutput)) {
                    /*if (arities.get(isOutput) >= 2) {
                            deletePath(level, isOutput);
                        return true;*/

        }
        return false;
    }

    public void gameLoop(int SI) {
        int destroyed = -1;
        ArrayList current = maze[SI];



        for(int path = 0; path < current.size(); path++) {
            // check if path directly connected to the output
            int value = (int)current.get(path);
            if (outputs.contains(value)) {
                destroyed = path;
            }
        }

        if (destroyed != -1) {
            int value = (int)current.get(destroyed);
            deletePath(SI, value);
        }
        else {
            if (!DeleteIfNextAndArtityToobig(SI)) {
                int maxArity = getMaxArityExitNode();
                ArrayList possibles = maze[maxArity];
                int edge = (int) possibles.get(0);
                deletePath(edge, maxArity);
            }
        }
    }

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // the total number of nodes in the level, including the gateways
        int L = in.nextInt(); // the number of links
        int E = in.nextInt(); // the number of exit gateways

        ArrayList[] maze = new ArrayList[N];
        for (int i = 0; i < N; i++) {
            maze[i] = new ArrayList();
        }

        for (int i = 0; i < L; i++) {
            int N1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int N2 = in.nextInt();
            maze[N1].add(N2);
            maze[N2].add(N1);
        }

        ArrayList outputs = new ArrayList();
        for (int i = 0; i < E; i++) {
            int EI = in.nextInt(); // the index of a gateway node
            outputs.add(EI);
        }

        Player p = new Player(N, L, E, maze, outputs);

        // game loop
        while (true) {
            int SI = in.nextInt();
            p.gameLoop(SI);
        }
    }
}