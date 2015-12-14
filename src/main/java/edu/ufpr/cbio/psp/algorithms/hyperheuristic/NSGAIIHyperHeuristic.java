/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.algorithms.hyperheuristic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.Ranking;
import org.uma.jmetal.util.random.PseudoRandom;

import edu.ufpr.cbio.psp.algorithms.hyperheuristc.comparators.LowLevelHeuristicComparatorFactory;
import edu.ufpr.cbio.psp.algorithms.hyperheuristic.loggers.ChoiceFunctionLogger;
import edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic.LowLevelHeuristic;

public class NSGAIIHyperHeuristic extends NSGAIIHyperHeuristicTemplate implements Algorithm {

    private static final long serialVersionUID = 5815971727148859507L;

    protected NSGAIIHyperHeuristic(Builder builder) {
        super(builder);
    }

    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     *         solutions as a experimentoutput of the algorithm execution
     * @throws org.uma.jmetal.util.JMetalException
     * @throws IOException
     */
    public SolutionSet execute() throws JMetalException, ClassNotFoundException, IOException {

        Comparator<LowLevelHeuristic> heuristicFunctionComparator;
        if (llhComparator.equals(LowLevelHeuristic.CHOICE_FUNCTION)) {
            heuristicFunctionComparator = LowLevelHeuristicComparatorFactory.createComparator("ChoiceFunction");
        } else {
            heuristicFunctionComparator = null;
        }

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

                    if (choiceFunctionLoggerFileName != null) {
                        ChoiceFunctionLogger.logHyperHeuristics(lowLevelHeuristics, choiceFunctionLoggerFileName);
                    }

                    LowLevelHeuristic lowLevelHeuristic = getApplyingHeuristic(heuristicFunctionComparator);
                    if (choiceFunctionLoggerFileName != null) {
                        ChoiceFunctionLogger.logSelectedHyperHeuristic(lowLevelHeuristic, choiceFunctionLoggerFileName);
                    }

                    Solution[] offSpring = (Solution[]) lowLevelHeuristic.execute(parents);

                    problem.evaluate(offSpring[0]);
                    problem.evaluateConstraints(offSpring[0]);

                    // Atualizar score da Low Level Heuristic aplicada
                    lowLevelHeuristic.updateScore(parents, offSpring);

                    if (choiceFunctionLoggerFileName != null) {
                        ChoiceFunctionLogger.logSelectedHyperHeuristicScore(lowLevelHeuristic,
                            choiceFunctionLoggerFileName);
                    }

                    // Atualiza score das Low Level Heuristics n�o utilizadas
                    for (LowLevelHeuristic heuristic : lowLevelHeuristics) {
                        if (!heuristic.equals(lowLevelHeuristic)) {
                            heuristic.notExecuted();
                        }
                    }

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

    private LowLevelHeuristic getApplyingHeuristic(Comparator<LowLevelHeuristic> comparator) {

        if (comparator != null) {
            // Choice
            List<LowLevelHeuristic> allLowLevelHeuristics = new ArrayList<>(lowLevelHeuristics);
            Collections.sort(allLowLevelHeuristics, comparator);
            List<LowLevelHeuristic> applyingHeuristics = new ArrayList<>();

            // Find the best tied heuristics
            Iterator<LowLevelHeuristic> iterator = allLowLevelHeuristics.iterator();
            LowLevelHeuristic heuristic;
            LowLevelHeuristic nextHeuristic = iterator.next();
            do {
                heuristic = nextHeuristic;
                applyingHeuristics.add(heuristic);
            } while (iterator.hasNext() && comparator.compare(heuristic, nextHeuristic = iterator.next()) == 0);

            return applyingHeuristics.get(PseudoRandom.randInt(0, applyingHeuristics.size() - 1));
            // }
        }
        // Random
        return lowLevelHeuristics.get(PseudoRandom.randInt(0, lowLevelHeuristics.size() - 1));
    }

    public void clearLowLevelHeuristics() {

        for (LowLevelHeuristic llh : lowLevelHeuristics) {
            llh.reinitialize();
        }
    }

}
