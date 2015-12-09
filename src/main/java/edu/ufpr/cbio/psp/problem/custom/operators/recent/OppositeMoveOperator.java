/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.problem.custom.operators.recent;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.encoding.solutiontype.IntSolutionType;
import org.uma.jmetal.encoding.solutiontype.wrapper.XInt;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.util.JMetalException;

import jmetal.util.PseudoRandom;

/**
 *
 *
 * @author Vidal
 */
public class OppositeMoveOperator extends Mutation {

    private double mutationProbability;

    public OppositeMoveOperator(double mutationProbability) {
        addValidSolutionType(IntSolutionType.class);
        this.mutationProbability = mutationProbability;
    }

    @Override
    public Object execute(Object object) throws JMetalException {

        if (null == object) {
            throw new JMetalException("Null parameter");
        } else if (!(object instanceof Solution)) {
            throw new JMetalException("Invalid parameter class");
        }

        Solution source = (Solution) object;

        return applyOppositeOperator(mutationProbability, source);
    }

    public Solution applyOppositeOperator(double probability, Solution source) {

        Solution offspring = new Solution(source);

        for (int i = 0; i < source.getDecisionVariables().length; i++) {
            offspring.getDecisionVariables()[i].copy();
        }

        if (PseudoRandom.randDouble() < probability) {
            int numberOfVariables = offspring.getDecisionVariables().length;

            int startPoint = PseudoRandom.randInt(0, numberOfVariables - 1);
            int endPoint = startPoint;
            do {
                endPoint = PseudoRandom.randInt(0, numberOfVariables - 1);
            } while (startPoint == endPoint);

            int aux = -1;
            if (startPoint > endPoint) {
                aux = endPoint;
                endPoint = startPoint;
                startPoint = aux;
            }
            XInt vars = new XInt(offspring);
            for (int i = startPoint; i < endPoint; i++) {
                int oldDirection = (int) vars.getValue(i);

                int oppositeDirection = oldDirection;
                switch (oldDirection) {
                    case 0:
                        oppositeDirection = 2;
                        break;
                    case 1:
                        oppositeDirection = 1;
                        break;
                    case 2:
                        oppositeDirection = 0;
                        break;

                    default:
                        throw new JMetalException(
                            "The direction " + oldDirection + " isn't supported invalid parent chromossome");
                }
                vars.setValue(i, oppositeDirection);

            }

        }
        return offspring;

    }

}
