package test;

import player.Player;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by xiaohouzi on 17/06/2016.
 */
public class TestDoublePasserelleRobuste01 {

    private int N = 8;
    private int L = 13;
    private int E = 2;

    private int[][] LINKS = { {6,2}, {7,3}, {6,3}, {5,3}, {3,4}, {7,1}, {2,0}, {0,1}, {0,3}, {1,3}, {2,3}, {7,4}, {6,5}};

    private int[] OUTPUT = {4, 5};

    @Test
    public void testDoublePasserelle() {
        Player p = new Player(N, L, E, LINKS, OUTPUT);
        p.gameLoop(0);
    }
}