package edu.ufpr.cbio.psp.algorithms.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.algorithms.hyperheuristic.SPEA2HyperHeuristic;
import edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic.LowLevelHeuristic;
import edu.ufpr.cbio.psp.problem.PSPProblem;
import edu.ufpr.cbio.psp.problem.custom.operators.IntegerTwoPointsCrossover;
import edu.ufpr.cbio.psp.problem.custom.operators.UniformCrossover;

public class SPEA2HyperHeuristicPSPMain {

    public static void main(String[] args) throws Exception {

        File file = new File("results");
        if (!file.exists()) {
            file.mkdir();
        }

        // Setting Parameters
        String path = file.getPath() + File.separator + "PSP";
        String algorithms = "SPEA2HH-CGiovani-Cross1-Mut0_1";
        int executions = 30;
        String choiceFunctionFileName = "choiceFunctionBehavior.log";
        double alpha = 1.0;
        double beta = 0.00005;
        int populationSize = 300;
        int maxEvaluations = 60000;
        int archiveSize = 200;
        double crossoverProbability = 1;
        double mutationProbability = 0.01;
        String proteinChain =
            "PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";
        int numberOfObjectives = 2;

        // Creating Output Dirs
        File rootDir = createDir(path);
        File algorithmDir = createDir(rootDir.getPath() + File.separator + algorithms + File.separator);
        File objectivesDir = createDir(algorithmDir.getPath() + File.separator);
        String outputDir = objectivesDir.getPath() + File.separator;

        PSPProblem problem = new PSPProblem(proteinChain, numberOfObjectives);
        SPEA2HyperHeuristic.Builder builder = new SPEA2HyperHeuristic.Builder(problem);

        builder.setPopulationSize(populationSize);
        builder.setMaxEvaluations(maxEvaluations);
        builder.setArchiveSize(archiveSize);

        Selection selection = new BinaryTournament2.Builder().build();
        builder.setSelection(selection);

        List<Crossover> listCrossover = new ArrayList<Crossover>();
        listCrossover.add(new SinglePointCrossover.Builder().setProbability(crossoverProbability).build());
        listCrossover
            .add(new IntegerTwoPointsCrossover.Builder().setCrossoverProbability(crossoverProbability).build());
        listCrossover.add(new UniformCrossover.Builder().setCrossoverProbability(crossoverProbability).build());

        List<Mutation> listMutation = new ArrayList<Mutation>();
        listMutation.add(new BitFlipMutation.Builder().setProbability(mutationProbability).build());
        listMutation.add(null);

        List<LowLevelHeuristic> lowLevelHeuristics =
            LowLevelHeuristic.Builder.generateLowLevelHeuristics(listCrossover, listMutation, alpha, beta);

        builder.setLowLevelHeuristics(lowLevelHeuristics);

        SolutionSet allRuns = new SolutionSet();

        long allExecutionTime = 0;
        System.out.println("Starting executions...");
        for (int i = 0; i < executions; i++) {

            String executionDirectory = outputDir + "EXECUTION_" + i;
            createDir(executionDirectory);

            builder.setLowLevelHeuristics(LowLevelHeuristic.Builder.generateLowLevelHeuristics(listCrossover,
                listMutation, alpha, beta));

            builder.setChoiceFunctionLoggerFileName(executionDirectory + File.separator + choiceFunctionFileName);
            Algorithm algorithm = builder.build();

            // Execute the Algorithm
            System.out.println("Execution: " + (i + 1));
            long initTime = System.currentTimeMillis();
            SolutionSet population = algorithm.execute();
            long estimatedTime = System.currentTimeMillis() - initTime;

            allExecutionTime += estimatedTime;

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
