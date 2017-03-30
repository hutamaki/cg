import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Vector;

class Node<T> {
	Node<T> parent;
	T node;
	int f, g, h;

	@Override
	public boolean equals(Object other) {
		if (other instanceof Node<?>) {
			if (((Node<?>) other).node.equals(node)) {
				return true;
			}
		}
		return false;
	}
}

interface Astarable<T> {
	Node<T>[] getSuccessors(Node<T> node);

	boolean isGoal(Node<T> node);

	int getDistance(Node<T> current, Node<T> sucessor);

	int getDistanceFromGoal(Node<T> current);
}

class Astar<T> implements Runnable {
	private PriorityQueue<Node<T>> openList;
	private Vector<Node<T>> closeList = new Vector<>();
	private Astarable<T> astarable;

	public Astar(int initialCapacity, Astarable<T> astarable) {
		openList = new PriorityQueue<>(initialCapacity, new Comparator<Node<T>>() {
			@Override
			public int compare(Node<T> arg0, Node<T> arg1) {
				return arg0.f - arg1.f;
			}
		});
		this.astarable = astarable;
	}

	void start(Node<T> startingPoint) {
		openList.add(startingPoint);
	}

	/**
	 * successor is interesting whenever it is not present or if its f is below
	 * than a previous one
	 */
	boolean removeIfPresentAndLess(AbstractCollection<Node<T>> list, Node<T> successor) {
		for (Node<T> element : list) {
			if (element.equals(successor)) {
				if (element.f <= successor.f) {
					return false;
				} else {
					list.remove(element);
					return true;
				}
			}
		}
		return true;
	}

	boolean considerWorthTrying(Node<T> successor) {
		if (!removeIfPresentAndLess(openList, successor)) {
			return false;
		}
		if (!removeIfPresentAndLess(closeList, successor)) {
			return false;
		}
		return true;
	}

	@Override
	public void run() {
		while (!openList.isEmpty()) {
			Node<T> current = openList.poll();
			Node<T>[] successors = astarable.getSuccessors(current);
			for (Node<T> successor : successors) {

				if (astarable.isGoal(successor)) {
					successor.parent = current;
					break;
				}
				successor.g = current.g + astarable.getDistance(current, successor);
				successor.h = astarable.getDistanceFromGoal(successor);
				successor.f = successor.g + successor.h;

				if (considerWorthTrying(successor)) {
					openList.add(successor);
				}
			}
			closeList.add(current);
		}
	}
}
