/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.problem.custom.operators.recent;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.Variable;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.util.JMetalException;

import jmetal.util.PseudoRandom;

/**
 *
 *
 * @author Vidal
 */
public class LoopMoveOperator extends Mutation {

    private double mutationProbability;

    public LoopMoveOperator(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    public Solution applyLoopMove(double probability, Solution source) {

        Solution offspring = new Solution(source);

        if (PseudoRandom.randDouble() < probability) {
            int numberOfVariables = offspring.getDecisionVariables().length;
            int localMovePoint1 = PseudoRandom.randInt(0, numberOfVariables - 1);
            int localMovePoint2 = localMovePoint1 + 5;

            if (localMovePoint2 >= numberOfVariables) {
                localMovePoint2 = numberOfVariables - 1;
            }

            Variable variableValue1 = offspring.getDecisionVariables()[localMovePoint1];
            Variable variableValue2 = offspring.getDecisionVariables()[localMovePoint2];

            offspring.getDecisionVariables()[localMovePoint1] = variableValue2.copy();
            offspring.getDecisionVariables()[localMovePoint2] = variableValue1.copy();

        }
        return offspring;

    }

    /*
     * (non-Javadoc)
     * @see org.uma.jmetal.core.Operator#execute(java.lang.Object)
     */
    @Override
    public Object execute(Object source) throws JMetalException {

        if (source == null) {
            throw new JMetalException("Null parameter");
        } else if (!(source instanceof Solution)) {
            throw new JMetalException("Invalid parameter class");
        }

        Solution solution = (Solution) source;

        return applyLoopMove(mutationProbability, solution);
    }
}
