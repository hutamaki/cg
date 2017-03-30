
import static org.junit.Assert.*;

import org.junit.Test;

public class AstarTests {

	class Unit {
		int x;
		int y;
	}

	class Game implements Astarable<Unit> {
		public Node<Unit>[] getSuccessors(Node<Unit> node) {
			return null;
		}

		public boolean isGoal(Node<Unit> node) {
			return false;
		}

		public int getDistance(Node<Unit> current, Node<Unit> sucessor) {
			return 0;
		}

		public int getDistanceFromGoal(Node<Unit> current) {
			return 0;
		}
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
