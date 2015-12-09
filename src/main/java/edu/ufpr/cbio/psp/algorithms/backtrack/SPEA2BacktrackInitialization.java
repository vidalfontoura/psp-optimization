/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.algorithms.backtrack;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.spea2.Spea2Fitness;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.Ranking;

import edu.ufpr.cbio.psp.algorithms.backtrack.initialization.BacktrackInitialization;

/**
 *
 *
 * @author Vidal
 */
public class SPEA2BacktrackInitialization implements Algorithm {

    private static final long serialVersionUID = -6552554169817006100L;

    private Problem problem;

    private static final int TOURNAMENTS_ROUNDS = 1;

    private int populationSize;
    private int archiveSize;
    private int maxEvaluations;

    protected Operator mutationOperator;
    protected Operator crossoverOperator;
    protected Operator selectionOperator;

    private double backtrackPercentage;

    private BacktrackInitialization backtrack;

    @Deprecated
    public SPEA2BacktrackInitialization() {
        super();
    }

    /** Constructor */
    private SPEA2BacktrackInitialization(Builder builder) {
        problem = builder.problem;

        populationSize = builder.populationSize;
        archiveSize = builder.archiveSize;
        maxEvaluations = builder.maxEvaluations;
        mutationOperator = builder.mutationOperator;
        crossoverOperator = builder.crossoverOperator;
        selectionOperator = builder.selectionOperator;

        backtrack = new BacktrackInitialization(problem, builder.aminoAcidSequence);
        backtrackPercentage = builder.backtrackPercentage;

    }

    /* Getters */
    public int getPopulationSize() {

        return populationSize;
    }

    public int getArchiveSize() {

        return archiveSize;
    }

    public int getMaxEvaluations() {

        return maxEvaluations;
    }

    public Operator getMutationOperator() {

        return mutationOperator;
    }

    public Operator getCrossoverOperator() {

        return crossoverOperator;
    }

    public Operator getSelectionOperator() {

        return selectionOperator;
    }

    /** Builder class */
    public static class Builder {

        private Problem problem;

        private int populationSize;
        private int archiveSize;

        private int maxEvaluations;

        private Operator mutationOperator;
        private Operator crossoverOperator;
        private Operator selectionOperator;

        private double backtrackPercentage;

        private String aminoAcidSequence;

        public Builder(Problem problem) {
            this.problem = problem;
        }

        public Builder setPopulationSize(int populationSize) {

            this.populationSize = populationSize;

            return this;
        }

        public Builder setArchiveSize(int archiveSize) {

            this.archiveSize = archiveSize;

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

        public Builder setAminoAcidSequence(String aminoAcidSequence) {

            this.aminoAcidSequence = aminoAcidSequence;

            return this;
        }

        public SPEA2BacktrackInitialization build() {

            return new SPEA2BacktrackInitialization(this);
        }
    }

    /** Execute() method */
    public SolutionSet execute() throws JMetalException, ClassNotFoundException {

        SolutionSet solutionSet;
        SolutionSet archive;
        SolutionSet offSpringSolutionSet;
        int evaluations;

        // Initialize the variables
        solutionSet = new SolutionSet(populationSize);
        archive = new SolutionSet(archiveSize);
        evaluations = 0;

        int amountOfSolutionsBacktrack = 0;
        SolutionSet backtrackSolutions = null;
        if (backtrackPercentage > 0) {
            amountOfSolutionsBacktrack = (int) (populationSize * backtrackPercentage) / 100;
            backtrackSolutions = backtrack.createPopulationAsIntegerSolution(amountOfSolutionsBacktrack);
        }

        // -> Create the initial solutionSet
        Solution newSolution;
        for (int i = amountOfSolutionsBacktrack; i < populationSize; i++) {
            newSolution = new Solution(problem);
            problem.evaluate(newSolution);
            problem.evaluateConstraints(newSolution);
            evaluations++;
            solutionSet.add(newSolution);
        }
        if (backtrackSolutions != null) {
            solutionSet = solutionSet.union(backtrackSolutions);
        }

        while (evaluations < maxEvaluations) {
            SolutionSet union = ((SolutionSet) solutionSet).union(archive);
            Spea2Fitness spea = new Spea2Fitness(union);
            spea.fitnessAssign();
            archive = spea.environmentalSelection(archiveSize);
            // Create a new offspringPopulation
            offSpringSolutionSet = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            while (offSpringSolutionSet.size() < populationSize) {
                int j = 0;
                do {
                    j++;
                    parents[0] = (Solution) selectionOperator.execute(archive);
                } while (j < SPEA2BacktrackInitialization.TOURNAMENTS_ROUNDS);
                int k = 0;
                do {
                    k++;
                    parents[1] = (Solution) selectionOperator.execute(archive);
                } while (k < SPEA2BacktrackInitialization.TOURNAMENTS_ROUNDS);

                // make the crossover
                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                mutationOperator.execute(offSpring[0]);
                problem.evaluate(offSpring[0]);
                problem.evaluateConstraints(offSpring[0]);
                offSpringSolutionSet.add(offSpring[0]);
                evaluations++;
            }
            // End Create a offSpring solutionSet
            solutionSet = offSpringSolutionSet;
        }

        Ranking ranking = new Ranking(archive);
        return ranking.getSubfront(0);
    }
}
