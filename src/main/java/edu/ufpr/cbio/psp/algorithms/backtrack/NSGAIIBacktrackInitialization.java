/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.algorithms.backtrack;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.Ranking;

public class NSGAIIBacktrackInitialization extends NSGAIIBacktrakInitializationTemplate implements Algorithm {

    private static final long serialVersionUID = 5815971727148859507L;

    protected NSGAIIBacktrackInitialization(Builder builder) {
        super(builder);
    }

    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     *         solutions as a experimentoutput of the algorithm execution
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

                    Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);

                    mutationOperator.execute(offSpring[0]);
                    mutationOperator.execute(offSpring[1]);

                    offspringPopulation.add(offSpring[0]);
                    offspringPopulation.add(offSpring[1]);
                }
            }

            offspringPopulation = evaluatePopulation(offspringPopulation);

            Ranking ranking = new Ranking(population.union(offspringPopulation));
            crowdingDistanceSelection(ranking);
        }

        tearDown();

        return getNonDominatedSolutions(population);
    }

}
