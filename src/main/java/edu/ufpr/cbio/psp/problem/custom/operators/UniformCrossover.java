package edu.ufpr.cbio.psp.problem.custom.operators;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.Variable;
import org.uma.jmetal.encoding.solutiontype.IntSolutionType;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.random.PseudoRandom;

public class UniformCrossover extends Crossover {

    private double crossoverProbability;

    /** Constructor */
    private UniformCrossover(Builder builder) {

        addValidSolutionType(IntSolutionType.class);

        crossoverProbability = builder.crossoverProbability;
    }

    public Solution[] doCrossover(double probability, Solution parent1, Solution parent2) throws JMetalException {

        Solution[] offspring = new Solution[2];

        offspring[0] = new Solution(parent1);
        offspring[1] = new Solution(parent2);

        if (PseudoRandom.randDouble() < probability) {
            if (parent1.getType().getClass() == IntSolutionType.class) {
                int crosspoint1;
                int crosspoint2;
                int solutionLenght;
                Variable parent1Vector[];
                Variable parent2Vector[];
                Variable offspring1Vector[];
                Variable offspring2Vector[];

                solutionLenght = parent1.getDecisionVariables().length;// tamanho
                                                                       // do
                                                                       // individuo
                parent1Vector = parent1.getDecisionVariables();// vetor do pai 1
                parent2Vector = parent2.getDecisionVariables();// vetor do pai 2
                offspring1Vector = offspring[0].getDecisionVariables();// vetor
                                                                       // filho
                                                                       // 1
                offspring2Vector = offspring[1].getDecisionVariables();// vetor
                                                                       // filho
                                                                       // 2

                for (int i = 0; i < solutionLenght; i++) {

                    if (PseudoRandom.randDouble(0, 1) > 0.5) {
                        offspring1Vector[i] = parent2Vector[i].copy();
                        offspring2Vector[i] = parent1Vector[i].copy();
                    }

                }
            } else {
                JMetalLogger.logger.severe("UniformCrossover.doCrossover: invalid " + "type"
                    + parent1.getType().getClass());
                Class<String> cls = java.lang.String.class;
                String name = cls.getName();
                throw new JMetalException("Exception in " + name + ".doCrossover()");
            }
        }

        return offspring;
    }

    @Override
    public Object execute(Object object) throws JMetalException {

        if (null == object) {
            throw new JMetalException("Null parameter");
        } else if (!(object instanceof Solution[])) {
            throw new JMetalException("Invalid parameter class");
        }

        Solution[] parents = (Solution[]) object;

        if (!solutionTypeIsValid(parents)) {
            throw new JMetalException("PolynomialMutation.execute: the solutiontype " + "type " + parents[0].getType()
                + " is not allowed with this operator");
        }

        if (parents.length < 2) {
            JMetalLogger.logger.severe("TwoPointsCrossover.execute: operator needs two " + "parents");
            Class<String> cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMetalException("Exception in " + name + ".execute()");
        }

        Solution[] offspring = doCrossover(crossoverProbability, parents[0], parents[1]);

        return offspring;
    }

    /** Builder class */
    public static class Builder {

        private double crossoverProbability;

        public Builder() {

            crossoverProbability = 0;
        }

        public Builder setCrossoverProbability(double crossoverProbability) {

            this.crossoverProbability = crossoverProbability;

            return this;
        }

        public UniformCrossover build() {

            return new UniformCrossover(this);
        }
    }
}
