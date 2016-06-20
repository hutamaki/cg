// # 2895
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;

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

class Player {

    private static void displayNodeToDelete(ArrayList[] maze, int currentLevel, Node item, int successor) {
        Node ptr = item;
        while (ptr.getParent() != null) {
            Node back = ptr.getParent();
            int back_idx = ptr.getIndex();
            ptr = back;

            if (ptr.getParent() == null) {
                System.out.println(currentLevel + " " + back_idx);

                ArrayList current = maze[currentLevel];
                current.remove(current.indexOf(back_idx));

                ArrayList mirror = maze[back_idx];
                mirror.remove(mirror.indexOf(currentLevel));
            }
        }
    }

    private static void BreadthFirstSearch(ArrayList[] maze, ArrayList outputs, int currentLevel) {
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
                    displayNodeToDelete(maze, currentLevel, item, successor);
                    return ;
                }
                else {
                    queue.addLast(new Node(successor, item));
                }
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

        // game loop
        while (true) {
            int SI = in.nextInt();

            int destroyed = -1;
            ArrayList current = maze[SI];
            for(int path = 0; path < current.size(); path++) {
                // check if path directly connected to the output
                int value = (int)current.get(path);
                if (outputs.contains(value)) {
                    System.out.println(SI + " " + value);
                    destroyed = path;
                }
            }

            if (destroyed != -1) {
                int value = (int)current.get(destroyed);
                current.remove(current.get(destroyed));
                ArrayList mirror = maze[value];
                mirror.remove(mirror.indexOf(SI));
            } else {
                BreadthFirstSearch(maze, outputs, SI);
            }
        }
    }
}