package edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;

import edu.ufpr.cbio.psp.problem.custom.operators.HHMutation;
import edu.ufpr.cbio.psp.problem.custom.operators.IntegerTwoPointsCrossover;
import edu.ufpr.cbio.psp.problem.custom.operators.UniformCrossover;

public class LowLevelHeuristic extends Operator {

    public static final String CHOICE_FUNCTION = "ChoiceFunction";
    public static final String RANDOM = "Random";

    private static final long serialVersionUID = 1L;

    private String name;

    private Crossover crossover;
    private Mutation mutation;

    private double score;
    private int elapsedTime;

    private double alpha;
    private double beta;

    private int numberOfTimesApplied;

    private LowLevelHeuristic(Builder builder) {

        this.name = builder.name;
        this.crossover = builder.crossover;
        this.mutation = builder.mutation;
        this.alpha = builder.alpha;
        this.beta = builder.beta;
        this.score = 1.0;
        this.elapsedTime = 0;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Operator getCrossover() {

        return crossover;
    }

    public void setCrossover(Crossover crossover) {

        this.crossover = crossover;
    }

    public Operator getMutation() {

        return mutation;
    }

    public void setMutation(Mutation mutation) {

        this.mutation = mutation;
    }

    public double getScore() {

        return score;
    }

    public int getElapsedTime() {

        return elapsedTime;
    }

    public int getNumberOfTimesApplied() {

        return numberOfTimesApplied;
    }

    public void setNumberOfTimesApplied(int numberOfTimesApplied) {

        this.numberOfTimesApplied = numberOfTimesApplied;
    }

    public void executed() {

        this.numberOfTimesApplied++;
        updateElapsedTime(true);
    }

    public void notExecuted() {

        updateElapsedTime(false);
    }

    private void updateElapsedTime(boolean executed) {

        this.elapsedTime = (executed) ? 0 : this.elapsedTime + 1;
    }

    public void updateScore(Solution[] parents, Solution[] offsprings) {

        Comparator<Solution> comparator = new DominanceComparator();
        score = 0.0;
        for (Solution parent : parents) {
            for (Solution offspring : offsprings) {
                score += (comparator.compare(parent, offspring) + 1.0) / 2.0;
            }
        }
        score /= ((double) parents.length * (double) offsprings.length);
    }

    public double getChoiceFunction() {

        return (alpha * score) + (beta * elapsedTime);
    }

    @Override
    public Object execute(Object parents) throws JMetalException {

        Solution[] offSpring = (Solution[]) crossover.execute(parents);

        if (mutation != null) {
            for (Solution offSpringSolution : offSpring) {
                mutation.execute(offSpringSolution);
            }
        }

        executed();

        return offSpring;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof LowLevelHeuristic) {
            if (!this.crossover.equals(((LowLevelHeuristic) obj).crossover)) {
                return false;
            }
            if (mutation == null) {
                if (mutation != ((LowLevelHeuristic) obj).mutation) {
                    return false;
                }
            } else if (!this.mutation.equals(((LowLevelHeuristic) obj).mutation)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static class Builder {

        private String name;
        private Crossover crossover;
        private Mutation mutation;
        private double alpha;
        private double beta;

        public Builder() {

            this.name = null;
            this.crossover = null;
            this.mutation = null;
            this.alpha = 0.0;
            this.beta = 0.0;
        }

        public Builder setName(String name) {

            this.name = name;

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

        public Builder setAlpha(double alpha) {

            this.alpha = alpha;

            return this;
        }

        public Builder setBeta(double beta) {

            this.beta = beta;

            return this;
        }

        public LowLevelHeuristic build() {

            return new LowLevelHeuristic(this);
        }

        public static List<LowLevelHeuristic> generateLowLevelHeuristics(List<Crossover> listCrossover,
                                                                         List<Mutation> listMutation, double alpha,
                                                                         double beta) {

            List<LowLevelHeuristic> lowLevelHeuristics = new ArrayList<LowLevelHeuristic>();
            int lowLevelHeuristicNumber = 0;
            StringBuilder heuristicName = new StringBuilder("h");
            for (Crossover crossover : listCrossover) {
                for (Mutation mutation : listMutation) {
                    LowLevelHeuristic.Builder builder = new LowLevelHeuristic.Builder();
                    heuristicName.append(lowLevelHeuristicNumber);
                    builder.setCrossover(crossover);
                    builder.setMutation(mutation);
                    builder.setAlpha(alpha);
                    builder.setBeta(beta);
                    builder.setName(heuristicName.toString());
                    lowLevelHeuristics.add(builder.build());

                    heuristicName = new StringBuilder("h");
                    lowLevelHeuristicNumber++;
                }
            }

            return lowLevelHeuristics;
        }

        public static List<LowLevelHeuristic> generateLowLevelHeuristics(String[] listCrossover,
                                                                         double crossoverProbability,
                                                                         String[] listMutation,
                                                                         double mutationProbability, double alpha,
                                                                         double beta) {

            List<LowLevelHeuristic> lowLevelHeuristics = new ArrayList<LowLevelHeuristic>();
            int lowLevelHeuristicNumber = 0;
            StringBuilder heuristicName = new StringBuilder("h");
            for (String crossover : listCrossover) {
                for (String mutation : listMutation) {
                    LowLevelHeuristic.Builder builder = new LowLevelHeuristic.Builder();
                    heuristicName.append(lowLevelHeuristicNumber);
                    heuristicName.append(" [").append(crossover).append(",").append(mutation).append("]");
                    builder.setCrossover(getCrossover(crossover, crossoverProbability));

                    builder.setMutation(getMutation(mutation, mutationProbability));
                    builder.setAlpha(alpha);
                    builder.setBeta(beta);
                    builder.setName(heuristicName.toString());
                    lowLevelHeuristics.add(builder.build());

                    heuristicName = new StringBuilder("h");
                    lowLevelHeuristicNumber++;
                }
            }

            return lowLevelHeuristics;
        }
    }

    @Override
    public String toString() {

        return "LowLevelHeuristic [name=" + name + ", score=" + score + ", elapsedTime=" + elapsedTime + "]";
    }

    public void reinitialize() {

        this.score = 1;
        this.elapsedTime = 0;
        this.numberOfTimesApplied = 0;
    }

    private static Crossover getCrossover(String crossoverName, double crossoverProbability) {

        Crossover crossover = null;
        switch (crossoverName) {
            case "SinglePointCrossover": {
                crossover = new SinglePointCrossover.Builder().setProbability(crossoverProbability).build();
                break;
            }
            case "IntegerTwoPointsCrossover": {
                crossover =
                    new IntegerTwoPointsCrossover.Builder().setCrossoverProbability(crossoverProbability).build();
                break;
            }
            case "UniformCrossover": {
                crossover = new UniformCrossover.Builder().setCrossoverProbability(crossoverProbability).build();
                break;
            }

        }
        return crossover;
    }

    private static Mutation getMutation(String mutationName, double mutationProbability) {

        Mutation mutation = null;
        switch (mutationName) {
            case "BitFlipMutation": {
                mutation = new BitFlipMutation.Builder().setProbability(mutationProbability).build();
                break;
            }
            case "HHMutation": {
                mutation = new HHMutation.Builder().setProbability(mutationProbability).build();
                break;
            }
            case "null": {
                return null;
            }
        }
        return mutation;

    }

}
