package tree;

import java.util.Vector;
import tree.Node;

/**
 * Created by pierre on 20/01/2017.
 */
public class BFS {

	public <T> void sTrace(Tree<T> tree) {
		int levels = tree.getMaze().size();	
		for (Vector<Node<T>> level : tree.getMaze()) {
			for (Node<T> node : level) {
				
			}
		}
	}

}
