package edu.ufpr.cbio.psp.algorithms.hyperheuristic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.spea2.Spea2Fitness;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.Ranking;
import org.uma.jmetal.util.random.PseudoRandom;

import edu.ufpr.cbio.psp.algorithms.hyperheuristc.comparators.LowLevelHeuristicComparatorFactory;
import edu.ufpr.cbio.psp.algorithms.hyperheuristic.loggers.ChoiceFunctionLogger;
import edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic.LowLevelHeuristic;

public class SPEA2HyperHeuristic implements Algorithm {

    private Problem problem;

    private static final int TOURNAMENTS_ROUNDS = 1;

    private int populationSize;
    private int archiveSize;
    private int maxEvaluations;
    private List<LowLevelHeuristic> lowLevelHeuristics;
    protected Operator selectionOperator;

    private String choiceFunctionLoggerFileName;
    private String llhComparator;

    /** Constructor */
    private SPEA2HyperHeuristic(Builder builder) {

        this.problem = builder.problem;

        this.populationSize = builder.populationSize;
        this.archiveSize = builder.archiveSize;
        this.maxEvaluations = builder.maxEvaluations;
        this.selectionOperator = builder.selectionOperator;
        this.lowLevelHeuristics = builder.lowLevelHeuristics;
        this.choiceFunctionLoggerFileName = builder.choiceFunctionLoggerFileName;
        this.llhComparator = builder.llhComparator;
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

    public List<LowLevelHeuristic> getLowLevelHeuristics() {

        return lowLevelHeuristics;
    }

    public Operator getSelectionOperator() {

        return selectionOperator;
    }

    public void clearLowLevelHeuristics() {

        for (LowLevelHeuristic llh : lowLevelHeuristics) {
            llh.reinitialize();
        }
    }

    /** Builder class */
    public static class Builder {

        private Problem problem;

        private int populationSize;
        private int archiveSize;

        private int maxEvaluations;

        private List<LowLevelHeuristic> lowLevelHeuristics;
        private Operator selectionOperator;

        private String choiceFunctionLoggerFileName;
        private String llhComparator;

        public Builder(Problem problem) {

            this.problem = problem;
            this.populationSize = 100;
            this.archiveSize = 100;
            this.maxEvaluations = 25000;
            this.choiceFunctionLoggerFileName = null;
            this.llhComparator = null;
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

        public Builder setLowLevelHeuristics(List<LowLevelHeuristic> lowLevelHeuristics) {

            this.lowLevelHeuristics = lowLevelHeuristics;

            return this;
        }

        public Builder setSelection(Operator selection) {

            this.selectionOperator = selection;

            return this;
        }

        public Builder setChoiceFunctionLoggerFileName(String fileName) {

            this.choiceFunctionLoggerFileName = fileName;

            return this;
        }

        public Builder setLLHComparator(String llhComparator) {

            this.llhComparator = llhComparator;
            return this;
        }

        public SPEA2HyperHeuristic build() {

            return new SPEA2HyperHeuristic(this);
        }
    }

    /**
     * Execute() method
     * 
     * @throws IOException
     */
    public SolutionSet execute() throws JMetalException, ClassNotFoundException, IOException {

        Comparator<LowLevelHeuristic> heuristicFunctionComparator;
        SolutionSet solutionSet;
        SolutionSet archive;
        SolutionSet offSpringSolutionSet;
        int evaluations;

        // Initialize the variables
        solutionSet = new SolutionSet(populationSize);
        archive = new SolutionSet(archiveSize);
        evaluations = 0;

        if (llhComparator.equals(LowLevelHeuristic.CHOICE_FUNCTION)) {
            heuristicFunctionComparator = LowLevelHeuristicComparatorFactory.createComparator("ChoiceFunction");
        } else {
            heuristicFunctionComparator = null;
        }

        // -> Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem);
            problem.evaluate(newSolution);
            problem.evaluateConstraints(newSolution);
            evaluations++;
            solutionSet.add(newSolution);
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
                } while (j < SPEA2HyperHeuristic.TOURNAMENTS_ROUNDS);
                int k = 0;
                do {
                    k++;
                    parents[1] = (Solution) selectionOperator.execute(archive);
                } while (k < SPEA2HyperHeuristic.TOURNAMENTS_ROUNDS);

                if (choiceFunctionLoggerFileName != null) {
                    ChoiceFunctionLogger.logHyperHeuristics(lowLevelHeuristics, choiceFunctionLoggerFileName);
                }
                // TODO Editar para hyper heuristica
                // LowLevelHeuristic lowLevelHeuristic =
                // lowLevelHeuristics.get(0);

                LowLevelHeuristic lowLevelHeuristic = getApplyingHeuristic(heuristicFunctionComparator);
                if (choiceFunctionLoggerFileName != null) {
                    ChoiceFunctionLogger.logSelectedHyperHeuristic(lowLevelHeuristic, choiceFunctionLoggerFileName);
                }

                Solution[] offSpring = (Solution[]) lowLevelHeuristic.execute(parents);
                offSpring = new Solution[] { offSpring[0] };
                // make the crossover
                // Solution[] offSpring = (Solution[])
                // crossoverOperator.execute(parents);
                // mutationOperator.execute(offSpring[0]);
                problem.evaluate(offSpring[0]);
                problem.evaluateConstraints(offSpring[0]);
                offSpringSolutionSet.add(offSpring[0]);

                // Atualizar score da Low Level Heuristic aplicada
                lowLevelHeuristic.updateScore(parents, offSpring);

                if (choiceFunctionLoggerFileName != null) {
                    ChoiceFunctionLogger
                        .logSelectedHyperHeuristicScore(lowLevelHeuristic, choiceFunctionLoggerFileName);
                }

                // Atualiza score das Low Level Heuristics n�o utilizadas
                for (LowLevelHeuristic heuristic : lowLevelHeuristics) {
                    if (!heuristic.equals(lowLevelHeuristic)) {
                        heuristic.notExecuted();
                    }
                }

                evaluations++;
            }
            // End Create a offSpring solutionSet
            solutionSet = offSpringSolutionSet;
        }

        Ranking ranking = new Ranking(archive);
        return ranking.getSubfront(0);
    }

    private LowLevelHeuristic getApplyingHeuristic(Comparator<LowLevelHeuristic> comparator) {

        // if
        // (getInputParameter("heuristicFunction").equals(LowLevelHeuristic.RANDOM)
        // || comparator == null) {
        // return lowLevelHeuristics.get(PseudoRandom.randInt(0,
        // lowLevelHeuristics.size() - 1));
        // } else {
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
        } else {
            // Random
            return lowLevelHeuristics.get(PseudoRandom.randInt(0, lowLevelHeuristics.size() - 1));
        }
    }
}
