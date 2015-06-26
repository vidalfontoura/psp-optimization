package edu.ufpr.cbio.psp.algorithms;

import java.io.File;
import java.util.HashMap;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.singleobjective.geneticalgorithm.GenerationalGeneticAlgorithm;
import org.uma.jmetal.metaheuristic.singleobjective.geneticalgorithm.GenerationalGeneticAlgorithm.Builder;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.evaluator.SequentialSolutionSetEvaluator;
import org.uma.jmetal.util.evaluator.SolutionSetEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.problem.PSPProblem;
import edu.ufpr.cbio.psp.problem.custom.operators.UniformCrossover;

public class SingleObjetivePSPProblemRelativaMain {

    public static void main(String[] args) throws Exception {

        File file = new File("result");
        if (!file.exists()) {
            file.mkdir();
        }

        PSPProblem problem;
        Algorithm algorithm;
        Crossover crossover;
        Mutation mutation;
        Selection selection;
        SolutionSetEvaluator evaluator;

        String path = "results/psp";
        String algorithms = "SingleObjetive_100";
        int executions = 30;

        HashMap<String, Double> parameters; // Operator parameters

        String proteinChain =
            "PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";
        int numberOfObjectives = 1;
        problem = new PSPProblem(proteinChain, numberOfObjectives);

        Builder builder = new GenerationalGeneticAlgorithm.Builder(problem);

        int populationSize = 100;
        builder.setPopulationSize(populationSize);

        int maxEvaluations = 25000;
        builder.setMaxEvaluations(maxEvaluations);

        double crossoverProbability = 0.9;
        crossover = new UniformCrossover.Builder().setCrossoverProbability(crossoverProbability).build();
        builder.setCrossover(crossover);

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        mutation = new BitFlipMutation.Builder().setProbability(mutationProbability).build();
        builder.setMutation(mutation);

        selection = new BinaryTournament2.Builder().build();
        builder.setSelection(selection);

        evaluator = new SequentialSolutionSetEvaluator();

        algorithm =
            builder.setPopulationSize(populationSize).setMaxEvaluations(maxEvaluations).setCrossover(crossover)
                .setMutation(mutation).setSelection(selection).setEvaluator(evaluator).build();

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

            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
            SolutionSet population = algorithmRunner.getSolutionSet();
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

        // AlgorithmRunner algorithmRunner = new
        // AlgorithmRunner.Executor(algorithm).execute();
        //
        // SolutionSet population = algorithmRunner.getSolutionSet();
        // long computingTime = algorithmRunner.getComputingTime();
        //
        // new SolutionSetOutput.Printer(population).setSeparator("\t")
        // .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
        // .setFunFileOutputContext(new
        // DefaultFileOutputContext("FUN.tsv")).print();

    }

    private static File createDir(String dir) {

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
}
