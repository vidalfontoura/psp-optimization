package edu.ufpr.cbio.psp.algorithms.tuning;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.mutation.Mutation;

import edu.ufpr.cbio.psp.algorithms.loggers.ConfigurationExecutionLogger;
import edu.ufpr.cbio.psp.problem.PSPProblem;
import edu.ufpr.cbio.psp.problem.custom.operators.IntegerTwoPointsCrossover;
import edu.ufpr.cbio.psp.problem.custom.operators.UniformCrossover;
import edu.ufpr.cbio.psp.problem.utils.ProteinChainUtils;

public class IBEATuningMultiobjectiveMain {

    private static final String ALGORITHM_NAME = "IBEA";
    public static int executedTasks = 0;

    public static void main(String[] args) throws Exception {

        String[] populations = null;
        String[] maxEvaluations = null;
        String[] crossovers = null;
        String[] mutations = null;
        String[] auxPopulations = null;
        String[] crossoverProbabilities = null;
        String[] mutationProbabilities = null;
        String proteinChain = null;
        int numberOfThreads = 0;
        String resultsPath = null;
        int executions = 0;
        if (args.length == 11) {
            populations = args[0].split(",");
            maxEvaluations = args[1].split(",");
            crossovers = args[2].split(",");
            mutations = args[3].split(",");
            auxPopulations = args[4].split(",");
            crossoverProbabilities = args[5].split(",");
            mutationProbabilities = args[6].split(",");
            executions = Integer.valueOf(args[7]);
            numberOfThreads = Integer.valueOf(args[8]);
            resultsPath = args[9];
            proteinChain = args[10];

        } else {
            populations = new String[] { "200", "300" };
            maxEvaluations = new String[] { /* "40000", */"60000" };
            crossovers = new String[] { "SinlgePointCrossover", "IntegerTwoPointsCrossover", "UniformCrossover" };
            mutations = new String[] { "BitFlipMutation" };
            auxPopulations = new String[] { "200" };
            crossoverProbabilities = new String[] { "0.9" };
            mutationProbabilities = new String[] { "0.01" };
            executions = 30;
            numberOfThreads = 10;
            resultsPath = "results";
            proteinChain =
                "PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";

        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        int numberOfObjectives = 2;

        String configurationFileName = "Configuration.txt";
        String executionLog = "AllExecutions.log";
        String allConfigurationsFileName = "AllConfigurations.txt";

        PSPProblem problem = new PSPProblem(proteinChain, numberOfObjectives);

        int configuration = 0;

        File rootDir = createDir(resultsPath);
        File proteinChainDir =
            createDir(rootDir.getPath() + File.separator + ProteinChainUtils.getNotationByProteinChain(proteinChain));
        File algorithmDir = createDir(proteinChainDir.getPath() + File.separator + ALGORITHM_NAME);

        for (int i = 0; i < populations.length; i++) {
            String population = populations[i];
            for (int w = 0; w < auxPopulations.length; w++) {
                String auxPopulation = auxPopulations[w];
                for (int j = 0; j < maxEvaluations.length; j++) {
                    String maxEvaluation = maxEvaluations[j];
                    for (int k = 0; k < crossovers.length; k++) {
                        for (int u = 0; u < crossoverProbabilities.length; u++) {
                            double crossoverProbability = Double.valueOf(crossoverProbabilities[u]);
                            String crossoverName = crossovers[k];
                            Crossover crossover = getCrossover(crossoverName, crossoverProbability);
                            for (int t = 0; t < mutations.length; t++) {
                                for (int h = 0; h < mutationProbabilities.length; h++) {
                                    double mutationProbability = Double.valueOf(mutationProbabilities[h]);
                                    String mutationName = mutations[t];
                                    Mutation mutation = getMutation(mutationName, mutationProbability);

                                    // Output files

                                    // All configurations log
                                    ConfigurationExecutionLogger.logAllConfiguration(configuration, ALGORITHM_NAME,
                                        Integer.valueOf(population),
                                        auxPopulation != null ? Integer.valueOf(auxPopulation) : null, crossoverName,
                                        crossoverProbability, mutationName, mutationProbability,
                                        Integer.valueOf(maxEvaluation), algorithmDir.getPath() + File.separator
                                            + allConfigurationsFileName);

                                    // Creating the task to execute the
                                    // configuration
                                    IBEAExecutingTask ibea2ExecutingTask =
                                        new IBEAExecutingTask(problem, crossover, Double.valueOf(crossoverProbability),
                                            crossoverName, mutation, Double.valueOf(mutationProbability), mutationName,
                                            Integer.valueOf(population), auxPopulation != null
                                                ? Integer.valueOf(auxPopulation) : null,
                                            Integer.valueOf(maxEvaluation), proteinChain, algorithmDir.getPath(),
                                            configuration, configurationFileName, executions);
                                    executor.execute(ibea2ExecutingTask);
                                    configuration++;

                                }
                            }
                        }
                    }
                }
            }
        }

        while (executedTasks < configuration) {
            ConfigurationExecutionLogger.logMessage("Total tasks config " + configuration + " executed "
                + executedTasks, algorithmDir + File.separator + executionLog);
            System.out.println("Total tasks config " + configuration + " executed " + executedTasks);
            Thread.sleep(30000);

        }
        ConfigurationExecutionLogger.logMessage("End of " + configuration + "configurations executions ", algorithmDir
            + File.separator + executionLog);
        System.out.println("End of " + configuration + "configurations executions ");
        System.exit(0);

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
        }
        return mutation;

    }

    private static File createDir(String dir) {

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

}
