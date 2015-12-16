/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.problem.custom.operators.recent;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.encoding.solutiontype.wrapper.XInt;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.util.JMetalException;

import jmetal.util.PseudoRandom;

/**
 *
 * Segment Mutation (SMUT): Muda um numero randomico de genes consecutivos (5 a
 * 7) para novas direções. Este operador introduz grandes mudanças na
 * conformação, e tem uma grande probabilidade de criar colisões, portanto um
 * mecanismo de reparação é aplicado no filho gerado caso necessário.
 *
 *
 * 
 * @author Vidal
 */
public class SegmentMutationOperator extends Mutation {

    private double mutationProbability;

    public SegmentMutationOperator(double mutationProbability) {
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

        return applySegmentMutation(mutationProbability, source);
    }

    public Solution applySegmentMutation(double probability, Solution source) {

        Solution offspring = new Solution(source);

        if (PseudoRandom.randDouble() < probability) {
            int numberOfVariables = offspring.getDecisionVariables().length;

            int startPoint = PseudoRandom.randInt(0, numberOfVariables - 1);
            int numberOfGenes = PseudoRandom.randInt(5, 7);
            int endPoint = startPoint + numberOfGenes;

            // TODO: Write a test for it
            if (endPoint >= numberOfVariables) {
                endPoint = numberOfVariables - 1;
            }

            XInt vars = new XInt(source);
            for (int i = startPoint; i < endPoint; i++) {

                int oldDirection = (int) vars.getValue(i);
                int newDirection = oldDirection;
                do {
                    newDirection = PseudoRandom.randInt(0, 2);
                } while (oldDirection == newDirection);

                vars.setValue(i, newDirection);
            }

        }
        return offspring;

    }

    public static void main(String[] args) {

        while (true) {
            System.out.println(PseudoRandom.randInt(0, 2));
        }
    }

}
