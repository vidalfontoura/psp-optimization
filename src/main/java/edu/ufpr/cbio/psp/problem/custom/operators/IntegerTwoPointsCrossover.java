package edu.ufpr.cbio.psp.problem.custom.operators;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.Variable;
import org.uma.jmetal.encoding.solutiontype.IntSolutionType;
import org.uma.jmetal.encoding.solutiontype.PermutationSolutionType;
import org.uma.jmetal.encoding.solutiontype.wrapper.XInt;
import org.uma.jmetal.encoding.variable.Int;
import org.uma.jmetal.encoding.variable.Permutation;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.crossover.TwoPointsCrossover.Builder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.random.PseudoRandom;

public class IntegerTwoPointsCrossover extends Crossover{

	private double crossoverProbability ;

	/** Constructor */
	private IntegerTwoPointsCrossover(Builder builder) {
		addValidSolutionType(IntSolutionType.class);

		crossoverProbability = builder.crossoverProbability ;
	}

	public Solution[] doCrossover(double probability,
			Solution parent1,
			Solution parent2) throws JMetalException{

		Solution[] offspring = new Solution[2];

		offspring[0] = new Solution(parent1);
		offspring[1] = new Solution(parent2);

		if (parent1.getType().getClass() == IntSolutionType.class) {
			if (PseudoRandom.randDouble() < probability) {
				int crosspoint1;
				int crosspoint2;
				int permutationLength;
				Variable parent1Vector[];
				Variable parent2Vector[];
				Variable offspring1Vector[];
				Variable offspring2Vector[];

				permutationLength = parent1.getDecisionVariables().length;//tamanho do individuo
				parent1Vector = parent1.getDecisionVariables();//vetor do pai 1
				parent2Vector = parent2.getDecisionVariables();//vetor do pai 2
				offspring1Vector = offspring[0].getDecisionVariables();//vetor filho 1
				offspring2Vector = offspring[1].getDecisionVariables();//vetor filho 2

				// STEP 1: Get two cutting points
				crosspoint1 = PseudoRandom.randInt(0, permutationLength - 1);
				crosspoint2 = PseudoRandom.randInt(0, permutationLength - 1);

				while (crosspoint2 == crosspoint1) {
					crosspoint2 = PseudoRandom.randInt(0, permutationLength - 1);
				}

				//Garante que ponto1 sempre vai ser <= que ponto2
				if (crosspoint1 > crosspoint2) {
					int swap;
					swap = crosspoint1;
					crosspoint1 = crosspoint2;
					crosspoint2 = swap;
				}
				
				//Troca apenas o meio de cada filho, pois o resto ja foi copiado do pai
				for (int i = crosspoint1; i <= crosspoint2; i++) {
					offspring1Vector[i] = parent2Vector[i];
					offspring2Vector[i] = parent1Vector[i];
				}
				
			}
		} else {
			JMetalLogger.logger.severe("TwoPointsCrossover.doCrossover: invalid " +
					"type" +
					parent1.getType().getClass());
			Class<String> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMetalException("Exception in " + name + ".doCrossover()");
		}

		return offspring;
	}

	@Override
	public Object execute(Object object) throws JMetalException {
		if (null == object) {
			throw new JMetalException("Null parameter") ;
		} else if (!(object instanceof Solution[])) {
			throw new JMetalException("Invalid parameter class") ;
		}

		Solution[] parents = (Solution[]) object;

		if (!solutionTypeIsValid(parents)) {
			throw new JMetalException("PolynomialMutation.execute: the solutiontype " +
					"type " + parents[0].getType() + " is not allowed with this operator");
		}

		if (parents.length < 2) {
			JMetalLogger.logger.severe("TwoPointsCrossover.execute: operator needs two " +
					"parents");
			Class<String> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMetalException("Exception in " + name + ".execute()");
		}

		Solution[] offspring = doCrossover(crossoverProbability,
				parents[0],
				parents[1]);

		return offspring;
	}

	/** Builder class */
	public static class Builder {
		private double crossoverProbability;

		public Builder() {
			crossoverProbability = 0 ;
		}

		public Builder crossoverProbability(double crossoverProbability) {
			this.crossoverProbability = crossoverProbability ;

			return this ;
		}

		public IntegerTwoPointsCrossover build() {
			return new IntegerTwoPointsCrossover(this) ;
		}
	}
}
