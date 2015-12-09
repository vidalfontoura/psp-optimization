/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.algorithms.backtrack;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.ibea.IBEA;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.crossover.SBXCrossover;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.operator.mutation.PolynomialMutation;
import org.uma.jmetal.operator.selection.BinaryTournament;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.Ranking;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.FitnessComparator;

import edu.ufpr.cbio.psp.algorithms.backtrack.initialization.BacktrackInitialization;

/**
 *
 *
 * @author Vidal
 */
public class IBEABacktrackInitialization implements Algorithm {

    private static final long serialVersionUID = -1889165434725718813L;

    private Problem problem;

    public static final int TOURNAMENTS_ROUNDS = 1;

    private List<List<Double>> indicatorValues;
    private double maxIndicatorValue;

    private int populationSize;
    private int archiveSize;
    private int maxEvaluations;

    private Crossover crossover;
    private Mutation mutation;
    private Selection selection;

    private double percentageBacktrackPopulation;

    private BacktrackInitialization backtrack;

    @Deprecated
    public IBEABacktrackInitialization() {
        super();
    }

    /** Constructor */
    private IBEABacktrackInitialization(Builder builder) {
        this.problem = builder.problem;
        this.populationSize = builder.populationSize;
        this.archiveSize = builder.archiveSize;
        this.maxEvaluations = builder.maxEvaluations;
        this.crossover = builder.crossover;
        this.mutation = builder.mutation;
        this.selection = builder.selection;
        this.backtrack = new BacktrackInitialization(problem, builder.aminoAcidSequence);
        this.percentageBacktrackPopulation = builder.percentageBacktrackPopulation;
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

    public Crossover getCrossover() {

        return crossover;
    }

    public Mutation getMutation() {

        return mutation;
    }

    public Selection getSelection() {

        return selection;
    }

    /**
     * calculates the hypervolume of that portion of the objective space that is
     * dominated by individual a but not by individual b
     */
        double calculateHypervolumeIndicator(Solution solutionA, Solution solutionB, int d, double maximumValues[],
                                             double minimumValues[]) {

        double a, b, r, max;
        double volume = 0;
        double rho = 2.0;

        r = rho * (maximumValues[d - 1] - minimumValues[d - 1]);
        max = minimumValues[d - 1] + r;

        a = solutionA.getObjective(d - 1);
        if (solutionB == null) {
            b = max;
        } else {
            b = solutionB.getObjective(d - 1);
        }

        if (d == 1) {
            if (a < b) {
                volume = (b - a) / r;
            } else {
                volume = 0;
            }
        } else {
            if (a < b) {
                volume =
                    calculateHypervolumeIndicator(solutionA, null, d - 1, maximumValues, minimumValues) * (b - a) / r;
                volume += calculateHypervolumeIndicator(solutionA, solutionB, d - 1, maximumValues, minimumValues)
                    * (max - b) / r;
            } else {
                volume = calculateHypervolumeIndicator(solutionA, solutionB, d - 1, maximumValues, minimumValues)
                    * (max - a) / r;
            }
        }

        return (volume);
    }

    /**
     * This structure stores the indicator values of each pair of elements
     */
    public void computeIndicatorValuesHD(SolutionSet solutionSet, double[] maximumValues, double[] minimumValues)
        throws JMetalException {

        SolutionSet A, B;
        // Initialize the structures
        indicatorValues = new ArrayList<List<Double>>();
        maxIndicatorValue = -Double.MAX_VALUE;

        for (int j = 0; j < solutionSet.size(); j++) {
            A = new SolutionSet(1);
            A.add(solutionSet.get(j));

            List<Double> aux = new ArrayList<Double>();
            for (int i = 0; i < solutionSet.size(); i++) {
                B = new SolutionSet(1);
                B.add(solutionSet.get(i));

                int flag = (new DominanceComparator()).compare(A.get(0), B.get(0));

                double value = 0.0;
                if (flag == -1) {
                    value = -calculateHypervolumeIndicator(A.get(0), B.get(0), problem.getNumberOfObjectives(),
                        maximumValues, minimumValues);
                } else {
                    value = calculateHypervolumeIndicator(B.get(0), A.get(0), problem.getNumberOfObjectives(),
                        maximumValues, minimumValues);
                }

                // Update the max value of the indicator
                if (Math.abs(value) > maxIndicatorValue) {
                    maxIndicatorValue = Math.abs(value);
                }
                aux.add(value);
            }
            indicatorValues.add(aux);
        }
    }

    /**
     * Calculate the fitness for the individual at position pos
     */
    public void fitness(SolutionSet solutionSet, int pos) {

        double fitness = 0.0;
        double kappa = 0.05;

        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != pos) {
                fitness += Math.exp((-1 * indicatorValues.get(i).get(pos) / maxIndicatorValue) / kappa);
            }
        }
        solutionSet.get(pos).setFitness(fitness);
    }

    /**
     * Calculate the fitness for the entire population.
     */
    public void calculateFitness(SolutionSet solutionSet) throws JMetalException {

        // Obtains the lower and upper bounds of the population
        double[] maximumValues = new double[problem.getNumberOfObjectives()];
        double[] minimumValues = new double[problem.getNumberOfObjectives()];

        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            maximumValues[i] = -Double.MAX_VALUE;
            minimumValues[i] = Double.MAX_VALUE;
        }

        for (int pos = 0; pos < solutionSet.size(); pos++) {
            for (int obj = 0; obj < problem.getNumberOfObjectives(); obj++) {
                double value = solutionSet.get(pos).getObjective(obj);
                if (value > maximumValues[obj]) {
                    maximumValues[obj] = value;
                }
                if (value < minimumValues[obj]) {
                    minimumValues[obj] = value;
                }
            }
        }

        computeIndicatorValuesHD(solutionSet, maximumValues, minimumValues);
        for (int pos = 0; pos < solutionSet.size(); pos++) {
            fitness(solutionSet, pos);
        }
    }

    /**
     * Update the fitness before removing an individual
     */
    public void removeWorst(SolutionSet solutionSet) {

        // Find the worst;
        double worst = solutionSet.get(0).getFitness();
        int worstIndex = 0;
        double kappa = 0.05;

        for (int i = 1; i < solutionSet.size(); i++) {
            if (solutionSet.get(i).getFitness() > worst) {
                worst = solutionSet.get(i).getFitness();
                worstIndex = i;
            }
        }

        // Update the population
        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != worstIndex) {
                double fitness = solutionSet.get(i).getFitness();
                fitness -= Math.exp((-indicatorValues.get(worstIndex).get(i) / maxIndicatorValue) / kappa);
                solutionSet.get(i).setFitness(fitness);
            }
        }

        // remove worst from the indicatorValues list
        indicatorValues.remove(worstIndex);
        for (List<Double> anIndicatorValues_ : indicatorValues) {
            anIndicatorValues_.remove(worstIndex);
        }

        solutionSet.remove(worstIndex);
    }

    /** Execute() method */
    public SolutionSet execute() throws JMetalException, ClassNotFoundException {

        int evaluations;
        SolutionSet solutionSet, archive, offSpringSolutionSet;

        // Initialize the variables
        solutionSet = new SolutionSet(populationSize);
        archive = new SolutionSet(archiveSize);
        evaluations = 0;

        int amountOfSolutionsBacktrack = 0;
        SolutionSet backtrackSolutions = null;
        if (percentageBacktrackPopulation > 0) {
            amountOfSolutionsBacktrack = (int) (populationSize * percentageBacktrackPopulation) / 100;
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
            SolutionSet union = solutionSet.union(archive);
            calculateFitness(union);
            archive = union;

            while (archive.size() > populationSize) {
                removeWorst(archive);
            }
            // Create a new offspringPopulation
            offSpringSolutionSet = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            while (offSpringSolutionSet.size() < populationSize) {
                int j = 0;
                do {
                    j++;
                    parents[0] = (Solution) selection.execute(archive);
                } while (j < IBEA.TOURNAMENTS_ROUNDS);
                int k = 0;
                do {
                    k++;
                    parents[1] = (Solution) selection.execute(archive);
                } while (k < IBEA.TOURNAMENTS_ROUNDS);

                // make the crossover
                Solution[] offSpring = (Solution[]) crossover.execute(parents);
                mutation.execute(offSpring[0]);
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

    /** Builder class */
    public static class Builder {

        private Problem problem;
        private int populationSize;
        private int archiveSize;
        private int maxEvaluations;

        private Crossover crossover;
        private Mutation mutation;
        private Selection selection;
        private String aminoAcidSequence;
        private double percentageBacktrackPopulation;

        public Builder(Problem problem) {
            this.problem = problem;
            populationSize = 100;
            archiveSize = 100;
            maxEvaluations = 25000;

            crossover = new SBXCrossover.Builder().setProbability(0.9).setDistributionIndex(20.0).build();

            mutation = new PolynomialMutation.Builder().setProbability(1.0 / problem.getNumberOfVariables())
                .setDistributionIndex(20.0).build();

            selection = new BinaryTournament.Builder().setComparator(new FitnessComparator()).build();

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

        public Builder setCrossover(Crossover crossover) {

            this.crossover = crossover;

            return this;
        }

        public Builder setMutation(Mutation mutation) {

            this.mutation = mutation;

            return this;
        }

        public Builder setSelection(Selection selection) {

            this.selection = selection;

            return this;
        }

        public Builder setAminoAcidSequence(String sequence) {

            this.aminoAcidSequence = sequence;

            return this;
        }

        public Builder setPercentageBacktrackPopulation(double value) {

            this.percentageBacktrackPopulation = value;

            return this;
        }

        public IBEABacktrackInitialization build() {

            return new IBEABacktrackInitialization(this);
        }
    }

}
