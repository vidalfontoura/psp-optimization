/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.algorithms.backtrack;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.util.Distance;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.Ranking;
import org.uma.jmetal.util.comparator.CrowdingComparator;
import org.uma.jmetal.util.evaluator.SolutionSetEvaluator;

import edu.ufpr.cbio.psp.algorithms.backtrack.initialization.BacktrackInitialization;

/**
 *
 *
 * @author Vidal
 */
public abstract class NSGAIIBacktrakInitializationTemplate implements Algorithm {

    protected SolutionSetEvaluator evaluator;

    protected Problem problem;

    protected int populationSize;
    protected int maxEvaluations;
    protected int evaluations;

    protected SolutionSet population;
    protected SolutionSet offspringPopulation;

    protected Operator mutationOperator;
    protected Operator crossoverOperator;
    protected Operator selectionOperator;
    protected double backtrackPercentage;

    private BacktrackInitialization backtrackInitialization;

    private Distance distance;

    /** Constructor */
    protected NSGAIIBacktrakInitializationTemplate(Builder builder) {
        problem = builder.problem;
        evaluator = builder.evaluator;
        populationSize = builder.populationSize;
        maxEvaluations = builder.maxEvaluations;
        mutationOperator = builder.mutationOperator;
        crossoverOperator = builder.crossoverOperator;
        selectionOperator = builder.selectionOperator;
        distance = new Distance();
        backtrackPercentage = builder.backtrackPercentage;

        backtrackInitialization = new BacktrackInitialization(problem, builder.aminoAcidSequence);

        evaluations = 0;
    }

    /* Getters */
    public Operator getCrossoverOperator() {

        return crossoverOperator;
    }

    public Operator getMutationOperator() {

        return mutationOperator;
    }

    public Operator getSelectionOperator() {

        return selectionOperator;
    }

    public int getPopulationSize() {

        return populationSize;
    }

    public int getMaxEvaluations() {

        return maxEvaluations;
    }

    public int getEvaluations() {

        return evaluations;
    }

    /** Builder class */
    public static class Builder {

        protected SolutionSetEvaluator evaluator;
        protected Problem problem;

        protected int populationSize;
        protected int maxEvaluations;

        protected Operator mutationOperator;
        protected Operator crossoverOperator;
        protected Operator selectionOperator;

        protected String aminoAcidSequence;
        protected double backtrackPercentage;

        public Builder(Problem problem, SolutionSetEvaluator evaluator) {
            this.evaluator = evaluator;
            this.problem = problem;
        }

        public Builder setPopulationSize(int populationSize) {

            this.populationSize = populationSize;

            return this;
        }

        public Builder setMaxEvaluations(int maxEvaluations) {

            this.maxEvaluations = maxEvaluations;

            return this;
        }

        public Builder setCrossover(Operator crossover) {

            crossoverOperator = crossover;

            return this;
        }

        public Builder setMutation(Operator mutation) {

            mutationOperator = mutation;

            return this;
        }

        public Builder setSelection(Operator selection) {

            selectionOperator = selection;

            return this;
        }

        public Builder setBacktrackPercentage(double backtrackPercentage) {

            this.backtrackPercentage = backtrackPercentage;

            return this;
        }

        public Builder setAminoAcidSequence(String aminoacidSequence) {

            this.aminoAcidSequence = aminoacidSequence;

            return this;
        }

        public NSGAIIBacktrakInitializationTemplate build(String nsgaIIvariant) {

            NSGAIIBacktrackInitialization algorithm;
            if ("NSGAIIBacktrackInitialization".equals(nsgaIIvariant)) {
                algorithm = new NSGAIIBacktrackInitialization(this);
            } /*
               * else if ("SteadyStateNSGAII".equals(nsgaIIvariant)) { algorithm
               * = new SteadyStateNSGAII(this); }
               */else {
                throw new JMetalException(nsgaIIvariant + " variant unknown");
            }

            return algorithm;
        }
    }

    protected void createInitialPopulation() throws ClassNotFoundException, JMetalException {

        population = new SolutionSet(populationSize);
        int amountOfSolutionsBacktrack = 0;
        SolutionSet backtrackSolutions = null;
        if (backtrackPercentage > 0) {
            amountOfSolutionsBacktrack = (int) (populationSize * backtrackPercentage) / 100;
            backtrackSolutions = backtrackInitialization.createPopulationAsIntegerSolution(amountOfSolutionsBacktrack);

            population = population.union(backtrackSolutions);
        }

        Solution newSolution;
        for (int i = amountOfSolutionsBacktrack; i < populationSize; i++) {
            newSolution = new Solution(problem);
            population.add(newSolution);
        }

    }

    protected SolutionSet evaluatePopulation(SolutionSet population) throws JMetalException {

        evaluations += population.size();

        return evaluator.evaluate(population, problem);
    }

    protected boolean stoppingCondition() {

        return evaluations >= maxEvaluations;
    }

    protected Ranking rankPopulation() throws JMetalException {

        SolutionSet union = population.union(offspringPopulation);

        return new Ranking(union);
    }

    protected void addRankedSolutionsToPopulation(Ranking ranking, int rank) throws JMetalException {

        SolutionSet front;

        front = ranking.getSubfront(rank);

        for (int i = 0; i < front.size(); i++) {
            population.add(front.get(i));
        }
    }

    protected void computeCrowdingDistance(Ranking ranking, int rank) throws JMetalException {

        SolutionSet currentRankedFront = ranking.getSubfront(rank);
        distance.crowdingDistanceAssignment(currentRankedFront);
    }

    protected void addLastRankedSolutions(Ranking ranking, int rank) throws JMetalException {

        SolutionSet currentRankedFront = ranking.getSubfront(rank);

        currentRankedFront.sort(new CrowdingComparator());

        int i = 0;
        while (population.size() < populationSize) {
            population.add(currentRankedFront.get(i));
            i++;
        }
    }

    protected boolean populationIsNotFull() {

        return population.size() < populationSize;
    }

    protected boolean subfrontFillsIntoThePopulation(Ranking ranking, int rank) {

        return ranking.getSubfront(rank).size() < (populationSize - population.size());
    }

    protected SolutionSet getNonDominatedSolutions(SolutionSet solutionSet) throws JMetalException {

        return new Ranking(solutionSet).getSubfront(0);
    }

    protected void crowdingDistanceSelection(Ranking ranking) {

        population.clear();
        int rankingIndex = 0;
        while (populationIsNotFull()) {
            if (subfrontFillsIntoThePopulation(ranking, rankingIndex)) {
                addRankedSolutionsToPopulation(ranking, rankingIndex);
                rankingIndex++;
            } else {
                computeCrowdingDistance(ranking, rankingIndex);
                addLastRankedSolutions(ranking, rankingIndex);
            }
        }
    }

    protected void tearDown() {

        evaluator.shutdown();
    }
}
