package maya;

import java.util.HashMap;
import java.util.Scanner;

public class Solution {

	final static int nbNumbers = 20;
	HashMap<String, Integer> dico = new HashMap<String, Integer>();
	HashMap<Integer, String> reverse = new HashMap<Integer, String>();
	int L;
	int H;

	public Solution(Scanner in) {
		L = in.nextInt();
		H = in.nextInt();
		readAndBuildDictionary(in);
	}

	/*
	 * incremental build of string representation of numbers in dico)
	 */
	public void readAndBuildDictionary(Scanner in) {
		
		StringBuffer[] strBuf = new StringBuffer[nbNumbers];
		StringBuffer[] strBufReversed = new StringBuffer[nbNumbers];
		for (int i = 0; i < nbNumbers; i++) {
			strBuf[i] = new StringBuffer();
			strBufReversed[i] = new StringBuffer();			
		}
		for (int i = 0; i < H; i++) {
			String numeral = in.next();
			for (int j = 0; j < nbNumbers; j++) {
				String partNumber = numeral.substring(j * L, (j * L) + L);
				strBuf[j].append(partNumber);
				strBufReversed[j].append(partNumber + "\n");			
			}
		}
		// build dictionaries
		for (int i = 0; i < nbNumbers; i++) {
			dico.put(strBuf[i].toString(), i);
			reverse.put(i, strBufReversed[i].toString());
		}
	}

	/*
	 * treat one char and return its value 
	 */
	private int readChar(Scanner in) {
		StringBuffer sbf = new StringBuffer();
		for (int i = 0; i < H; i++) {
			sbf.append(in.next());
		}
		return dico.get(sbf.toString());
	}

	/*
	 * what we want is to treat only flat strings
	 */
	private long readNumber(Scanner in) {
		int nb = in.nextInt() / H;		
		long result = 0;
		for (int i = 0; i < nb; i++) {
			result *= 20;
			result += readChar(in);
		}
		return result;
	}

	/*
	 * parse operation char & do the calculation
	 */
	private long getResult(Scanner in, long firstNumber, long secondNumber) throws Exception {
		String operation = in.next();
		switch (operation.charAt(0)) {
		case '+':
			return firstNumber + secondNumber;
		case '-':
			return firstNumber - secondNumber;
		case '*':
			return firstNumber * secondNumber;
		case '/':
			return firstNumber / secondNumber;
		}
		throw new Exception("unable to identify operation");
	}

	/*
	 * read numbers, read operations, do the calculation & return result
	 * in a displayable form.
	 */
	public String perform(Scanner in) throws Exception {
		long firstNumber = readNumber(in);
		long secondNumber = readNumber(in);
		long result = getResult(in, firstNumber, secondNumber);
		StringBuffer strBuf = new StringBuffer();
		return display(result, strBuf);
	}
	
	/* return result in a displayable form */
	private String display(long result, StringBuffer strBuf) {
		if (result > nbNumbers) {
			int mod = (int)(result % nbNumbers);
			display(result / nbNumbers, strBuf);
			strBuf.append(reverse.get(mod));
		} else {
			strBuf.append(reverse.get((int)result));	
		}					
		return strBuf.toString();
	}

	public static void main(String[] argv) {
		Scanner in = new Scanner(System.in);
		Solution solution = new Solution(in);
		try {
			System.out.println(solution.perform(in));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
