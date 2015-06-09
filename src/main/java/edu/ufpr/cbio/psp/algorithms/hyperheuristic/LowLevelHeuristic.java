package edu.ufpr.cbio.psp.algorithms.hyperheuristic;

import java.util.Comparator;

import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;

public class LowLevelHeuristic extends Operator{
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private Crossover crossover;
	private Mutation mutation;
	
	private double score;
	private int elapsedTime;
	
	private double alpha = 1;
	private double beta = 1;
	
	public LowLevelHeuristic() {
		this.name = "";
		this.crossover = null;
		this.mutation = null;
		this.score = 1.0;
		this.elapsedTime = 0;
	}
	
	public LowLevelHeuristic(String name, Crossover crossover, Mutation mutation) {
		this.name = name;
		this.crossover = crossover;
		this.mutation = mutation;
		this.score = 0.0;
		this.elapsedTime = 0;
	}
	
	public LowLevelHeuristic(Crossover crossover, Mutation mutation) {
		this.name = crossover.getClass().getName() + mutation.getClass().getName();
		this.crossover = crossover;
		this.mutation = mutation;
		this.score = 0.0;
		this.elapsedTime = 0;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Operator getCrossover() {
		return crossover;
	}
	
	public void setCrossover(Crossover crossover) {
		this.crossover = crossover;
	}
	
	public Operator getMutation() {
		return mutation;
	}
	
	public void setMutation(Mutation mutation) {
		this.mutation = mutation;
	}
	
	public double getScore() {
		return score;
	}
	
	public int getElapsedTime() {
		return elapsedTime;
	}
	
	public void executed() {
		updateElapsedTime(true);
	}
	
	public void notExecuted() {
		updateElapsedTime(false);
	}
	
	private void updateElapsedTime(boolean executed) {
		this.elapsedTime = (executed) ? 0 : this.elapsedTime+1;
	}
	
	public void updateScore(Solution[] parents, Solution[] offsprings) {
		Comparator<Solution> comparator = new DominanceComparator();
		for (Solution parent : parents) {
			for(Solution offspring : offsprings) {
				score += ( comparator.compare(parent, offspring) + 1.0 ) / 2.0;
			}
		}
		score /= ((double) parents.length * (double) offsprings.length);
	}
	
	public double getChoiceFunction() {
		return (alpha * score) + (beta * elapsedTime);
	}
	
	@Override
	public Object execute(Object parents) throws JMetalException {
		
		Solution[] offSpring = (Solution[]) crossover.execute(parents);
		
		if(mutation != null) {
			for(Solution offSpringSolution : offSpring) {
				mutation.execute(offSpringSolution);
			}
		}
		
		executed();
		
		return offSpring;
	}
	
}
