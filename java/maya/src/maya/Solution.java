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
				System.err.println("partNumber: " + partNumber);
				strBuf[j].append(partNumber);
				strBufReversed[j].append(partNumber + "\n");			
			}
		}
		// build dictionaries
		for (int i = 0; i < nbNumbers; i++) {
			String str = strBuf[i].toString();
			System.err.println(i + " > " + str);
			dico.put(strBuf[i].toString(), i);
			reverse.put(i, strBufReversed[i].toString());
		}
	}

	/*
	 * what we want is to treat only flat strings
	 */
	private int readNumber(Scanner in) {
		int S = in.nextInt();
		StringBuffer sbf = new StringBuffer();
		for (int i = 0; i < S; i++) {
			sbf.append(in.next());
		}
		System.err.println("sbf " + sbf.toString());
		return dico.get(sbf.toString());
	}

	/*
	 * parse operation char & do the calculation
	 */
	private int getResult(Scanner in, int firstNumber, int secondNumber) throws Exception {
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
		int firstNumber = readNumber(in);
		int secondNumber = readNumber(in);
		System.err.println("firstNumber : " + firstNumber);
		System.err.println("secondNumber: " + secondNumber);
		int result = getResult(in, firstNumber, secondNumber);
		System.err.println("result: " + result);
		StringBuffer strBuf = new StringBuffer();
		return display(result, strBuf);
	}
	
	/* return result in a displayable form */
	private String display(int result, StringBuffer strBuf) {
		if (result > nbNumbers) {
			int mod = result % nbNumbers;
			display(result / nbNumbers, strBuf);
			strBuf.append(reverse.get(mod));
		} else {
			strBuf.append(reverse.get(result));	
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
