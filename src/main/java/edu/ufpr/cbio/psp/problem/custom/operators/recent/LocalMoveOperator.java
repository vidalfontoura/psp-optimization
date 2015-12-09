/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.problem.custom.operators.recent;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.Variable;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.random.PseudoRandom;

/**
 *
 * Troca a direções de dois genes aleatórios consecutivos. Existem alguma
 * condições a serem satisfeitas. Exemplo: As novas direções não podem criar
 * movimentos redundantes. Este operador introduz um “movimento de esquina”.
 * Mencionam que este operador foi introduzido por outro trabalho o qual,
 * utiliza uma buscal local utilizando um parâmetro T que serve para ajustar a
 * aceitação de bad moves ou não (possibilitando balancear convergência e
 * diversidade).
 *
 * @author vfontoura
 * 
 *         TODO: Verifiy Accpetance criterion
 * 
 */
public class LocalMoveOperator extends Mutation {

    private double mutationProbability;

    public LocalMoveOperator(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    public Solution applyLocalMove(double probability, Solution source) {

        Solution offspring = new Solution(source);

        if (PseudoRandom.randDouble() < probability) {
            int numberOfVariables = offspring.getDecisionVariables().length;

            int localMovePoint1 = PseudoRandom.randInt(0, numberOfVariables - 1);
            int localMovePoint2 = localMovePoint1 + 1;

            // TODO: Write a test for it
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

        return applyLocalMove(mutationProbability, solution);
    }

}
