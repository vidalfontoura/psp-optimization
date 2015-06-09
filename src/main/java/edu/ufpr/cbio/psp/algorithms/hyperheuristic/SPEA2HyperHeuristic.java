package edu.ufpr.cbio.psp.algorithms.hyperheuristic;

import java.util.Collections;
import java.util.List;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.spea2.Spea2Fitness;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.Ranking;

public class SPEA2HyperHeuristic implements Algorithm {

	private Problem problem ;

	private static final int TOURNAMENTS_ROUNDS = 1;

	private int populationSize ;
	private int archiveSize ;
	private int maxEvaluations ;	
	private List<LowLevelHeuristic> lowLevelHeuristics ;
	protected Operator selectionOperator ;

	/** Constructor */
	private SPEA2HyperHeuristic(Builder builder) {
		this.problem = builder.problem;

		this.populationSize = builder.populationSize ;
		this.archiveSize = builder.archiveSize ;
		this.maxEvaluations = builder.maxEvaluations ;
		this.selectionOperator = builder.selectionOperator ;
		this.lowLevelHeuristics = builder.lowLevelHeuristics ;
	}

	/* Getters */
	public int getPopulationSize() {
		return populationSize;
	}

	public int getArchiveSize() {
		return archiveSize;
	}

	public int getMaxEvaluations() {
		return maxEvaluations;
	}

	public List<LowLevelHeuristic> getLowLevelHeuristics() {
		return lowLevelHeuristics;
	}
	
	public Operator getSelectionOperator() {
		return selectionOperator;
	}

	/** Builder class */
	public static class Builder {
		private Problem problem;

		private int populationSize;
		private int archiveSize;

		private int maxEvaluations;

		private List<LowLevelHeuristic> lowLevelHeuristics;
		
		private Operator selectionOperator;

		public Builder(Problem problem) {
			this.problem = problem ;
			this.populationSize = 100;
			this.archiveSize = 100;
			this.maxEvaluations = 25000;
		}

		public Builder setPopulationSize(int populationSize) {
			this.populationSize = populationSize ;

			return this ;
		}

		public Builder setArchiveSize(int archiveSize) {
			this.archiveSize = archiveSize ;

			return this ;
		}

		public Builder setMaxEvaluations(int maxEvaluations) {
			this.maxEvaluations = maxEvaluations ;

			return this ;
		}

		public Builder setLowLevelHeuristics(List<LowLevelHeuristic> lowLevelHeuristics) {
			this.lowLevelHeuristics = lowLevelHeuristics;
			
			return this;
		} 

		public Builder setSelection(Operator selection) {
			this.selectionOperator = selection ;

			return this ;
		}

		public SPEA2HyperHeuristic build() {
			return new SPEA2HyperHeuristic(this) ;
		}
	}

	/** Execute() method */
	public SolutionSet execute() throws JMetalException, ClassNotFoundException {
		SolutionSet solutionSet ;
		SolutionSet archive ;
		SolutionSet offSpringSolutionSet;
		int evaluations;

		//Initialize the variables
		solutionSet = new SolutionSet(populationSize);
		archive = new SolutionSet(archiveSize);
		evaluations = 0;

		//-> Create the initial solutionSet
		Solution newSolution;
		for (int i = 0; i < populationSize; i++) {
			newSolution = new Solution(problem);
			problem.evaluate(newSolution);
			problem.evaluateConstraints(newSolution);
			evaluations++;
			solutionSet.add(newSolution);
		}

		while (evaluations < maxEvaluations) {
			SolutionSet union = ((SolutionSet) solutionSet).union(archive);
			Spea2Fitness spea = new Spea2Fitness(union);
			spea.fitnessAssign();
			archive = spea.environmentalSelection(archiveSize);
			// Create a new offspringPopulation
			offSpringSolutionSet = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];
			while (offSpringSolutionSet.size() < populationSize) {
				int j = 0;
				do {
					j++;
					parents[0] = (Solution) selectionOperator.execute(archive);
				} while (j < SPEA2HyperHeuristic.TOURNAMENTS_ROUNDS);
				int k = 0;
				do {
					k++;
					parents[1] = (Solution) selectionOperator.execute(archive);
				} while (k < SPEA2HyperHeuristic.TOURNAMENTS_ROUNDS);
				
				//TODO Editar para hyper heuristica
				LowLevelHeuristic lowLevelHeuristic = lowLevelHeuristics.get(0);
				
				Solution[] offSpring = (Solution[]) lowLevelHeuristic.execute(parents);
				offSpring = new Solution[]{offSpring[0]};
				//make the crossover 
				//Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
				//mutationOperator.execute(offSpring[0]);
				problem.evaluate(offSpring[0]);
				problem.evaluateConstraints(offSpring[0]);
				offSpringSolutionSet.add(offSpring[0]);
				
				//Atualizar score da Low Level Heuristic aplicada
				lowLevelHeuristic.updateScore(parents, offSpring);
				
				//Atualiza score das Low Level Heuristics não utilizadas
				for(LowLevelHeuristic heuristic : lowLevelHeuristics) {
					if(!heuristic.equals(lowLevelHeuristic)) {
						heuristic.notExecuted();
					}
				}
				
				//Reordenar vetor de Low Level Heuristics
				Collections.sort(lowLevelHeuristics, new LowLevelHeuristicComparator());
				
				//TODO Editar para hyper heuristica
				
				evaluations++;
			}
			// End Create a offSpring solutionSet
			solutionSet = offSpringSolutionSet;
		}

		Ranking ranking = new Ranking(archive);
		return ranking.getSubfront(0);
	}
}
