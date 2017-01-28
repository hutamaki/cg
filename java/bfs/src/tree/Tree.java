package tree; 

import java.util.Vector;

/**
 * Created by pierre on 20/01/2017.
 */
public class Tree<T> {

    private Vector<Vector<Node<T>>> maze;
    
    public Tree() {
    	maze = new Vector<Vector<Node<T>>>();
    }

    public Vector<Vector<Node<T>>> getMaze() {
        return maze;
    }

    public String toString(StringBuffer stringBuffer) {
        for (int j = 0; j < maze.size(); j++) {
            Vector<Node<T>> level = maze.elementAt(j);
            stringBuffer.append("> " + j + "\n{");
            for (int i = 0; i < level.size(); i++) {
                stringBuffer.append("\n\t" + i + ": ");
                level.elementAt(i).toString(stringBuffer);
            }
            stringBuffer.append("\n}\n");
        }
        return stringBuffer.toString();
    }

}
