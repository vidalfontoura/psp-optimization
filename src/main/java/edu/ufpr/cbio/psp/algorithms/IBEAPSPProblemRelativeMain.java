package edu.ufpr.cbio.psp.algorithms;

import java.io.File;
import java.util.HashMap;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.ibea.IBEA;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.operator.selection.BinaryTournament;
import org.uma.jmetal.util.comparator.FitnessComparator;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.problem.PSPProblem;

public class IBEAPSPProblemRelativeMain {

    public static void main(String[] args) throws Exception {

        File file = new File("results");
        if (!file.exists()) {
            file.mkdir();
        }

        String path = file.getPath() + File.separator + "PSP";
        String algorithms = "IBEA";
        int executions = 30;

        PSPProblem problem; // The problem to solve
        Algorithm algorithm; // The algorithm to use

        HashMap<String, Double> parameters; // Operator parameters

        // String proteinChain =
        // "PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";
        String proteinChain = "PPPHHPPHHPPPPPHHHHHHHPPHHPPPPHHPPHPP";
        int numberOfObjectives = 2;

        int populationSize = 100;

        problem = new PSPProblem(proteinChain, numberOfObjectives, populationSize, System.out);

        IBEA.Builder builder = new IBEA.Builder(problem);

        builder.setPopulationSize(populationSize);

        int maxEvaluations = 25000;
        builder.setMaxEvaluations(maxEvaluations);

        int archiveSize = 100;
        builder.setArchiveSize(archiveSize);

        double crossoverProbability = 0.9;
        // double crossoverDistributionIndex = 20.0;
        Crossover crossover;
        crossover = new SinglePointCrossover.Builder().setProbability(crossoverProbability).build();// 1
        // crossover = new
        // IntegerTwoPointsCrossover.Builder().crossoverProbability(crossoverProbability).build();//2
        // crossover = new
        // UniformCrossover.Builder().setCrossoverProbability(crossoverProbability).build();//
        // 3
        builder.setCrossover(crossover);

        double mutationProbability = 0.01;
        // double mutationDistributionIndex = 20.0;
        Mutation mutation;
        mutation = new BitFlipMutation.Builder().setProbability(mutationProbability).build();
        // mutation = new
        // PolynomialMutation.Builder().setProbability(mutationProbability).build();
        builder.setMutation(mutation);

        builder.setSelection(new BinaryTournament.Builder().setComparator(new FitnessComparator()).build());

        algorithm = builder.build();

        // algorithms += "_" + crossover.getClass().getSimpleName() + "_" +
        // mutation.getClass().getSimpleName();

        File rootDir = createDir(path);
        File algorithmDir = createDir(rootDir.getPath() + File.separator + algorithms + File.separator);
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
