import java.util.Scanner;

class Solution {
	
	static final int maxPlayersIn = 6;
	
	public static int startAt(int nbSeconds, int nbPlayer) {
		return  nbSeconds - (int)(256 / (Math.pow(2, nbPlayer - 1)));
	}
	
	public static void displayResult(int nbSeconds) {
		System.out.format("%d:%02d\n", Math.max(0, nbSeconds / 60), Math.max(0, nbSeconds % 60));
	}

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();        
        in.nextLine();
        int back = 0;
        for (int i = 0; i < n; i++) {
        	String[] timestamp = in.nextLine().split(":");      	
    		int nbSeconds = Integer.parseInt(timestamp[0]) * 60 + Integer.parseInt(timestamp[1]);    		
    		if (i == maxPlayersIn) {
    		    displayResult(nbSeconds);
    		    return ;
    		}
        	if (nbSeconds < back) {
        		displayResult(back);
        		return ;
        	} else {        	    
        		back = startAt(nbSeconds, i + 1);
        		if (i == n - 1) {
        			displayResult(back);
        			return ;
        		}
        	}    	
        }
        System.out.println("NO GAME");
    }
}
