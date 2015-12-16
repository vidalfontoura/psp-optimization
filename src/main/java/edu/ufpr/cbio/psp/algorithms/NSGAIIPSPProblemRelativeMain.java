package edu.ufpr.cbio.psp.algorithms;

import java.io.File;
import java.util.HashMap;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.nsgaII.NSGAIITemplate;
import org.uma.jmetal.operator.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.util.evaluator.SequentialSolutionSetEvaluator;
import org.uma.jmetal.util.evaluator.SolutionSetEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.problem.PSPProblem;

public class NSGAIIPSPProblemRelativeMain {

    public static void main(String[] args) throws Exception {

        File file = new File("psp_experiments");
        if (!file.exists()) {
            file.mkdir();
        }

        String path = "psp_experiments/tmp_experiments";
        String algorithms = "NSGAII";
        int executions = 30;

        PSPProblem problem; // The problem to solve
        Algorithm algorithm; // The algorithm to use
        Operator crossover; // Crossover operator
        Operator mutation; // Mutation operator
        Operator selection; // Selection operator

        HashMap<String, Double> parameters; // Operator parameters

        String proteinChain =
            "PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";
        int populationSize = 100;
        int numberOfObjectives = 2;
        problem = new PSPProblem(proteinChain, numberOfObjectives, populationSize, System.out);

        SolutionSetEvaluator evaluator = new SequentialSolutionSetEvaluator();
        NSGAIITemplate.Builder builder = new NSGAIITemplate.Builder(problem, evaluator);

        builder.setPopulationSize(populationSize);

        int maxEvaluations = 25000;
        builder.setMaxEvaluations(maxEvaluations);

        double crossoverProbability = 0.9;
        crossover = new SinglePointCrossover.Builder().setProbability(crossoverProbability).build();
        builder.setCrossover(crossover);

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        mutation = new BitFlipMutation.Builder().setProbability(mutationProbability).build();
        builder.setMutation(mutation);

        selection = new BinaryTournament2.Builder().build();
        builder.setSelection(selection);

        algorithm = builder.build("NSGAII");

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
            System.out.println(mutation.toString());
            System.out.println(crossover.toString());
            long initTime = System.currentTimeMillis();
            SolutionSet population = algorithm.execute();
            long estimatedTime = System.currentTimeMillis() - initTime;

            allExecutionTime += estimatedTime;

            String executionDirectory = outputDir + "EXECUTION_" + i;
            createDir(executionDirectory);

            problem.removeDominateds(population);
            problem.removeDuplicates(population);

            population.printObjectives();

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
