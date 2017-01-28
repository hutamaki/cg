import java.util.Vector;

import tree.Node;
import tree.Tree;

public class MainTree {

    public static void main(final String[] argv) {
        Tree<Integer> bt = new Tree<Integer>();
        
        Vector<Node<Integer>> level_0 = new Vector<Node<Integer>>();
        level_0.add(new Node<Integer>(1));
        level_0.add(new Node<Integer>(2));
        
        Vector<Node<Integer>> level_1 = new Vector<Node<Integer>>();
        level_1.add(new Node<Integer>(3));
        level_1.add(new Node<Integer>(4));
        
        bt.getMaze().add(level_0);
        bt.getMaze().add(level_1);
        
        System.out.println(bt.toString(new StringBuffer()));
    }
}