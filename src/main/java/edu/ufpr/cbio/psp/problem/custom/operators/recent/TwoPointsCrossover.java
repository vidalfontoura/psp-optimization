/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.problem.custom.operators.recent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.Variable;
import org.uma.jmetal.encoding.solutiontype.IntSolutionType;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.random.PseudoRandom;

/**
 *
 *
 * @author Vidal
 */
public class TwoPointsCrossover extends Crossover {

    private double crossoverProbability;

    /** Constructor */
    private TwoPointsCrossover(Builder builder) {

        addValidSolutionType(IntSolutionType.class);

        crossoverProbability = builder.crossoverProbability;
    }

    public Solution[] doCrossover(double probability, Solution parent1, Solution parent2) throws JMetalException {

        Solution[] offspring = new Solution[2];

        offspring[0] = new Solution(parent1);
        offspring[1] = new Solution(parent2);

        if (PseudoRandom.randDouble() < probability) {

            int numberOfVariables = parent1.getDecisionVariables().length;
            int numberOfCrossPoints = 2;

            List<Integer> crossoverPoints = new ArrayList<>(numberOfCrossPoints);

            for (int i = 0; i < numberOfCrossPoints; i++) {
                int crosspoint = PseudoRandom.randInt(0, numberOfVariables - 1);
                if (!crossoverPoints.contains(crosspoint)) {
                    crossoverPoints.add(crosspoint);
                }

            }

            Collections.sort(crossoverPoints);

            int startPoint = 0;
            boolean exchangeValues = true;
            for (int j = 0; j < crossoverPoints.size(); j++) {

                Integer point = crossoverPoints.get(j);
                for (int i = startPoint; i < point; i++) {
                    Variable variableValue1 = parent1.getDecisionVariables()[i];
                    Variable variableValue2 = parent2.getDecisionVariables()[i];

                    if (exchangeValues) {
                        offspring[0].getDecisionVariables()[i] = variableValue2.copy();
                        offspring[1].getDecisionVariables()[i] = variableValue1.copy();
                    } else {
                        offspring[0].getDecisionVariables()[i] = variableValue1.copy();
                        offspring[1].getDecisionVariables()[i] = variableValue2.copy();
                    }

                }
                exchangeValues = exchangeValues ? false : true;
                startPoint = point;
            }

        }
        return offspring;
    }

    /*
     * (non-Javadoc)
     * @see org.uma.jmetal.core.Operator#execute(java.lang.Object)
     */
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

        public TwoPointsCrossover build() {

            return new TwoPointsCrossover(this);
        }
    }

}
