package edu.ufpr.cbio.psp.algorithms.tuning;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ufpr.cbio.psp.algorithms.loggers.ConfigurationExecutionLogger;
import edu.ufpr.cbio.psp.problem.PSPProblem;
import edu.ufpr.cbio.psp.problem.utils.ProteinChainUtils;

public class IBEAHHTuningMultiobjectiveMain {

    private static final String ALGORITHM_NAME = "IBEA";
    public static int executedTasks = 0;

    public static void main(String[] args) throws Exception {

        String[] populations = null;
        String[] maxEvaluations = null;
        String[] crossovers = null;
        String[] mutations = null;
        String[] auxPopulations = null;
        double crossoverProbability = 0.0;
        double mutationProbability = 0.0;
        String proteinChain = null;
        int numberOfThreads = 0;
        String resultsPath = null;
        String llhComparator = null;

        String[] alphas = null;
        String[] betas = null;
        int executions = 0;
        boolean logChoiceBehavior = false;
        if (args.length == 15) {
            populations = args[0].split(",");
            maxEvaluations = args[1].split(",");
            crossovers = args[2].split(",");
            mutations = args[3].split(",");
            auxPopulations = args[4].split(",");
            crossoverProbability = Double.valueOf(args[5]);
            mutationProbability = Double.valueOf(args[6]);
            executions = Integer.valueOf(args[7]);
            numberOfThreads = Integer.valueOf(args[8]);
            resultsPath = args[9];
            alphas = args[10].split(",");
            betas = args[11].split(",");
            llhComparator = args[12];
            proteinChain = args[13];
            logChoiceBehavior = Boolean.getBoolean(args[14]);

        } else {
            populations = new String[] { "200" };
            maxEvaluations = new String[] { /* "40000", */"40000" };
            crossovers = new String[] { "SinglePointCrossover", "IntegerTwoPointsCrossover", "UniformCrossover" };
            mutations = new String[] { "BitFlipMutation", "null" };
            auxPopulations = new String[] { "200" };
            crossoverProbability = 1;
            mutationProbability = 1;
            executions = 1;
            numberOfThreads = 10;
            resultsPath = "results-test-random-choice-function-log";
            alphas = new String[] { "1.0" };
            betas = new String[] { "0.00005", };
            proteinChain =
                "PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";
            llhComparator = "ChoiceFunction";
            logChoiceBehavior = true;
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
            Integer population = Integer.valueOf(populations[i]);
            for (int w = 0; w < auxPopulations.length; w++) {
                Integer auxPopulation = Integer.valueOf(auxPopulations[w]);
                for (int j = 0; j < maxEvaluations.length; j++) {
                    Integer maxEvaluation = Integer.valueOf(maxEvaluations[j]);

                    for (int t = 0; t < alphas.length; t++) {
                        double alpha = Double.valueOf(alphas[t]);
                        for (int h = 0; h < betas.length; h++) {
                            double beta = Double.valueOf(betas[h]);

                            // Output files
                            // All configurations log
                            ConfigurationExecutionLogger.logAllConfigurationHH(configuration, ALGORITHM_NAME, Integer
                                .valueOf(population), auxPopulation != null ? Integer.valueOf(auxPopulation) : null,
                                crossovers, crossoverProbability, mutations, mutationProbability, Integer
                                    .valueOf(maxEvaluation), algorithmDir.getPath() + File.separator
                                    + allConfigurationsFileName, alpha, beta);

                            // Creating the task to execute the configuration
                            IBEAHHExecutingTask ibeaExecutionTask =
                                new IBEAHHExecutingTask(problem, crossovers, Double.valueOf(crossoverProbability),
                                    mutations, Double.valueOf(mutationProbability), population, auxPopulation,
                                    maxEvaluation, proteinChain, algorithmDir.getPath(), configuration,
                                    configurationFileName, executions, alpha, beta, llhComparator, logChoiceBehavior);

                            executor.execute(ibeaExecutionTask);
                            configuration++;
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
        System.out.println("End of " + configuration + " configurations executions ");
        System.exit(0);

    }

    private static File createDir(String dir) {

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

}
