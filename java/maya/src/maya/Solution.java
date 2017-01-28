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
		for (int i = 0; i < H; i++) {
			String numeral = in.next();
			for (int j = 0; j < nbNumbers; j++) {

				String partNumber = numeral.substring(j, j + L);
				strBuf[i].append(partNumber);
				strBufReversed[i].append(partNumber + "\n");			
			}
		}
		// build dictionaries
		for (int i = 0; i < nbNumbers; i++) {
			String str = strBuf[i].toString();
			dico.put(strBuf[i].toString(), i);
			reverse.put(i, str);
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
		return dico.get(sbf);
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
		int result = getResult(in, firstNumber, secondNumber);
		return display(Integer.toString(result));
	}
	
	/* return result in a displayable form */
	private String display(String result) {
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < result.length(); i++) {
			int c = Character.getNumericValue(result.charAt(i));
			strBuf.append(reverse.get(c) + "\n");			
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
