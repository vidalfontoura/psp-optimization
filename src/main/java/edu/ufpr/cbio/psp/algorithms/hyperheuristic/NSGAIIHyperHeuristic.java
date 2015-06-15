package edu.ufpr.cbio.psp.algorithms.hyperheuristic;

import java.util.List;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.nsgaII.NSGAIITemplate;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.Distance;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.Ranking;
import org.uma.jmetal.util.evaluator.SolutionSetEvaluator;

public class NSGAIIHyperHeuristic extends NSGAIITemplate implements Algorithm  {

	private List<LowLevelHeuristic> lowLevelHeuristics;

	protected NSGAIIHyperHeuristic(Builder builder) {
		super(builder);
		this.lowLevelHeuristics = builder.lowLevelHeuristics;
	}

	/**
	 * Runs the NSGA-II algorithm.
	 *
	 * @return a <code>SolutionSet</code> that is a set of non dominated solutions
	 * as a experimentoutput of the algorithm execution
	 * @throws org.uma.jmetal.util.JMetalException
	 */
	public SolutionSet execute() throws JMetalException, ClassNotFoundException {
		createInitialPopulation();
		population = evaluatePopulation(population);

		// Main loop
		while (!stoppingCondition()) {
			offspringPopulation = new SolutionSet(populationSize);
			for (int i = 0; i < (populationSize / 2); i++) {
				if (!stoppingCondition()) {
					Solution[] parents = new Solution[2];
					parents[0] = (Solution) selectionOperator.execute(population);
					parents[1] = (Solution) selectionOperator.execute(population);

					//Escolha da LowLevelHeuristic
					LowLevelHeuristic lowLevelHeuristic = lowLevelHeuristics.get(0);
					
					//Executa a Low Level Heuristic (Cruzamento e Mutação)
					Solution[] offSpring = (Solution[]) lowLevelHeuristic.execute(parents);

					offspringPopulation.add(offSpring[0]);
					offspringPopulation.add(offSpring[1]);
				}
			}

			offspringPopulation = evaluatePopulation(offspringPopulation);

			Ranking ranking = new Ranking(population.union(offspringPopulation));
			crowdingDistanceSelection(ranking);
		}

		tearDown() ;

		return getNonDominatedSolutions(population) ;
	}

	public static class Builder extends org.uma.jmetal.metaheuristic.multiobjective.nsgaII.NSGAIITemplate.Builder{
		
		private List<LowLevelHeuristic> lowLevelHeuristics;

		public Builder(Problem problem, SolutionSetEvaluator evaluator) {
			super(problem, evaluator);
		}
		
		public Builder setEvaluator(SolutionSetEvaluator evaluator) {
			this.evaluator = evaluator;

			return this;
		}
		public Builder setPopulationSize(int populationSize) {
			this.populationSize = populationSize;

			return this;
		}
		public Builder setMaxEvaluations(int maxEvaluations) {
			this.maxEvaluations = maxEvaluations;

			return this;
		}
		public Builder setLowLevelHeuristics(List<LowLevelHeuristic> lowLevelHeuristics) {
			this.lowLevelHeuristics = lowLevelHeuristics;

			return this;
		}
		public Builder setSelectionOperator(Selection selectionOperator) {
			this.selectionOperator = selectionOperator;

			return this;
		}

		public NSGAIIHyperHeuristic build() {
			return new NSGAIIHyperHeuristic(this);
		}
	}

}
