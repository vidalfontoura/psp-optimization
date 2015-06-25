package edu.ufpr.cbio.psp.algorithms;

import java.io.File;
import java.util.HashMap;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.spea2.SPEA2;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.problem.PSPProblem;
import edu.ufpr.cbio.psp.problem.custom.operators.UniformCrossover;

public class SPEA2PSPProblemRelativeMain {

    public static void main(String[] args) throws Exception {

        File file = new File("result");
        if (!file.exists()) {
            file.mkdir();
        }

        String path = "results/psp";
        String algorithms = "SPEA2-test-infesaible";
        int executions = 1;

        PSPProblem problem; // The problem to solve
        Algorithm algorithm; // The algorithm to use

        HashMap<String, Double> parameters; // Operator parameters

        String proteinChain =
            "PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";
        int numberOfObjectives = 2;
        problem = new PSPProblem(proteinChain, numberOfObjectives);

        SPEA2.Builder builder = new SPEA2.Builder(problem);

        int populationSize = 300;
        builder.setPopulationSize(populationSize);

        int maxEvaluations = 60000;
        builder.setMaxEvaluations(maxEvaluations);

        int archiveSize = 250;
        builder.setArchiveSize(archiveSize);

        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;

        Crossover crossover = // new
                              // SinglePointCrossover.Builder().setProbability(crossoverProbability).build();
            // new
            // PSPIntegerUniformCrossover.Builder().crossoverProbability(crossoverProbability).build();
            // new
            // IntegerTwoPointsCrossover.Builder().setCrossoverProbability(crossoverProbability).build();
            new UniformCrossover.Builder().setCrossoverProbability(crossoverProbability).build();
        builder.setCrossover(crossover);

        double mutationProbability = 0.01;
        double mutationDistributionIndex = 20.0;
        Mutation mutation = new BitFlipMutation.Builder().setProbability(mutationProbability).build();
        builder.setMutation(mutation);

        Selection selection = new BinaryTournament2.Builder().build();
        builder.setSelection(selection);

        algorithm = builder.build();

        File rootDir = createDir(path);
        File algorithmDir =
            createDir(rootDir.getPath() + File.separator + algorithms + "_" + crossover.getClass().getSimpleName()
                + "_" + mutation.getClass().getSimpleName() + File.separator);
        File objectivesDir = createDir(algorithmDir.getPath() + File.separator);

        String outputDir = objectivesDir.getPath() + File.separator;

        SolutionSet allRuns = new SolutionSet();

        long allExecutionTime = 0;
        System.out.println("Starting executions...");
        for (int i = 0; i < executions; i++) {

            // Execute the Algorithm
            System.out.println("Execution: " + (i + 1));
            long initTime = System.currentTimeMillis();
            SolutionSet population = algorithm.execute();
            long estimatedTime = System.currentTimeMillis() - initTime;

            allExecutionTime += estimatedTime;

            String executionDirectory = outputDir + "EXECUTION_" + i;
            createDir(executionDirectory);

            problem.removeDominateds(population);
            problem.removeDuplicates(population);

            SolutionSetOutput.printVariablesToFile(population, executionDirectory + File.separator + "VAR.txt");
            SolutionSetOutput.printObjectivesToFile(population, executionDirectory + File.separator + "FUN.txt");

            allRuns = allRuns.union(population);
        }

        System.out.println();
        System.out.println("End of execution for problem " + problem.getClass().getName() + ".");
        System.out.println("Total time (seconds): " + allExecutionTime / 1000);
        System.out.println("Writing results.");

        problem.removeDominateds(allRuns);
        problem.removeDuplicates(allRuns);

        SolutionSetOutput.printVariablesToFile(allRuns, outputDir + "VAR.txt");
        SolutionSetOutput.printObjectivesToFile(allRuns, outputDir + "FUN.txt");

    }

    private static File createDir(String dir) {

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
}
