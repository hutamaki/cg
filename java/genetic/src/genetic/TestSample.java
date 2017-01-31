package genetic;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestSample {

	@Test
	public void testSubstringLengthEven() {
		String testedString = "1234";
		String father = testedString.substring(0, testedString.length() >> 1);
		String mother = testedString.substring(testedString.length() >> 1);
		System.err.format("%s%s\n", mother, father);
		assertEquals(testedString, father + mother);
	}
	
	@Test
	public void testSubstringLengthOdd() {
		String testedString = "12345";
		String father = testedString.substring(0, testedString.length() >> 1);
		String mother = testedString.substring(testedString.length() >> 1);
		System.err.format("%s%s\n", mother, father);
		assertEquals(testedString, father + mother);
	}

}
