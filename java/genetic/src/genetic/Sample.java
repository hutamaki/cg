/*
 * https://skyduino.wordpress.com/2015/07/16/tutorielpython-les-algorithmes-genetiques-garantis-sans-ogm/
 */
package genetic;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator.Fitness;

class Pair implements Comparable<Pair>
{
	int 	fitness;
	String	individual;
	
	@Override
	public int compareTo(Pair pair) {
		return pair.fitness - fitness; // descending order
	}
}

public class Sample {
	
	public static final double CHANCE_TO_MUTATE = 0.1;
	public static final double GRADED_RETAIN_PERCENT = 0.2;
	public static final double CHANCE_RETAIN_NONGRATED = 0.05;	
	
	public static final int POPULATION_COUNT = 100;
	public static final int GENERATON_COUNT_MAX = 100000;
	
	public static final int GRADED_INDIVIDUAL_RETAIN_COUNT = (int) (POPULATION_COUNT * GRADED_RETAIN_PERCENT);
		
	public static final String EXPECTED_STR = "Fuck fucking fucked fucker fucking fuckups fuck fucking fucked fucking fuckup fucking fucker's fucking fuckup.";
	public static final int MAXIMUM_FITNESS = EXPECTED_STR.length();
	
	public static final Random random = new Random();
	public static final String asciiLetters = "abcdefjhijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ! '.";
	
	public char getRandomChar() {
		return asciiLetters.charAt((int)random.nextDouble() * asciiLetters.length());
	}
	
	public String getRandomInvidivual() {
		char[] individual = new char[EXPECTED_STR.length()];
		for (int i = 0; i < EXPECTED_STR.length(); i++) {
			individual[i] = getRandomChar();
		}
		return new String(individual);
	}
	
	public String[] getRandomPopulation() {
		String[] population = new String[POPULATION_COUNT];
		for (int i = 0; i < POPULATION_COUNT; i++) {
			population[i] = getRandomInvidivual();
		}
		return population;
	}
	
	public int getIndividualFitness(String individual) {
		int fitness = 0;
		for (int i = 0; i < EXPECTED_STR.length(); i++) {
			if (individual.charAt(i) == EXPECTED_STR.charAt(i)) {
				fitness++;
			}
		}
		return fitness;
	}
	
	public double getAverageGradePopulation(String[] population) {
		long populationFitness = 0;
		for (String individual : population) {
			populationFitness += getIndividualFitness(individual);
		}
		return populationFitness / POPULATION_COUNT;
	}
	
	public Pair[] gradePopulation(String[] population) {
		Pair[] graded_individuals = new Pair[POPULATION_COUNT];
		for (int i = 0 ; i < POPULATION_COUNT; i++) {
			graded_individuals[i] = new Pair();
			graded_individuals[i].fitness = getIndividualFitness(population[i]);
			graded_individuals[i].individual = population[i];
		}
		Arrays.sort(graded_individuals);
		return graded_individuals;
	}
	
	public int evolvePopulation(Vector<String> solutions, String[] population) {
		
		// get individuals sorted by grade (top first), the average grade and the solution (if any)
		Pair[] evaluatedPopulation = gradePopulation(population);
		int average_grade = 0;
		for (Pair pair : evaluatedPopulation) {
			average_grade += pair.fitness;
			if (pair.fitness == MAXIMUM_FITNESS) {
				solutions.add(pair.individual);
			}
		}
		average_grade /= POPULATION_COUNT;		
		
		// return if solution has been found
		if (solutions.size() > 0) {
			return average_grade;
		}
		
		// filter top grades individuals
		Vector<String> parents = new Vector<String>();
		for (int i = 0; i < GRADED_INDIVIDUAL_RETAIN_COUNT; i++) {
			parents.add(evaluatedPopulation[i].individual);
		}
		
		// randomly add other individuals to promote genetic diversity
		for (int i = GRADED_INDIVIDUAL_RETAIN_COUNT; i < evaluatedPopulation.length; i++) {
			if (random.nextDouble() < CHANCE_RETAIN_NONGRATED) {
				parents.add(evaluatedPopulation[i].individual);
			}
		}
		
		// mutate some individuals
		for (String individual : parents) {
			if (random.nextDouble() < CHANCE_TO_MUTATE) {
				int index_to_modify = random.nextInt(EXPECTED_STR.length());
				char[] tmpStr = individual.toCharArray();
				tmpStr[index_to_modify] = getRandomChar();
				individual = String.valueOf(tmpStr);				
			}
		}
		
		// crossover parents to create children
		int desiredLen = POPULATION_COUNT - parents.size();
		Vector<String> children = new Vector<String>();
		while (children.size() < desiredLen) {
			String father = parents.elementAt(random.nextInt(parents.size()));
			String mother = parents.elementAt(random.nextInt(parents.size()));
			//if (father != mother) {
			if (true) {
				String child = father.substring(0, EXPECTED_STR.length() >> 1) + mother.substring(EXPECTED_STR.length() >> 1); 
				children.addElement(child);
			}
		}		
		parents.addAll(children);
		
		return average_grade;
	}
}
