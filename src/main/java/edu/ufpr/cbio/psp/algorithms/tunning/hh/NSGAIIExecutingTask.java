package edu.ufpr.cbio.psp.algorithms.tunning.hh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.evaluator.SequentialSolutionSetEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.algorithms.hyperheuristic.NSGAIIHyperHeuristic;
import edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic.LowLevelHeuristic;
import edu.ufpr.cbio.psp.algorithms.loggers.ConfigurationExecutionLogger;
import edu.ufpr.cbio.psp.problem.PSPProblem;

public class NSGAIIExecutingTask implements Runnable {

    private static final String ALGORITHM_NAME = "NSGAIIHH";

    private PSPProblem problem;
    private double crossoverProbability;
    private double mutationProbability;
    private int population;
    private int maxEvaluation;
    private String proteinChain;
    private String algorithmPath;
    private int configuration;
    private String configurationFileName;
    private int executions;

    private String[] crossovers;
    private String[] mutations;

    private double alpha;
    private double beta;

    private String llhComparator;
    private boolean logChoiceFunctionBehavior;

    private double backtrackPercentage;

    public NSGAIIExecutingTask(String[] crossovers, double crossoverProbability, String[] mutations,
        double mutationProbability, int population, int maxEvaluation, String proteinChain, String algorithmPath,
        int configuration, String configurationFileName, int executions, double alpha, double beta,
        String llhComparator, boolean logChoiceFunctionBehavior, double backtrackPercentage) {

        this.population = population;
        this.maxEvaluation = maxEvaluation;
        this.proteinChain = proteinChain;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
        this.algorithmPath = algorithmPath;
        this.configuration = configuration;
        this.configurationFileName = configurationFileName;
        this.executions = executions;
        this.crossovers = crossovers;
        this.mutations = mutations;
        this.alpha = alpha;
        this.beta = beta;
        this.llhComparator = llhComparator;
        this.logChoiceFunctionBehavior = logChoiceFunctionBehavior;
        this.backtrackPercentage = backtrackPercentage;
    }

    @Override
    public void run() {

        File configurationDir = createDir(algorithmPath + File.separator + "C" + configuration);
        File objectivesDir = createDir(configurationDir.getPath() + File.separator);
        String outputDir = objectivesDir.getPath() + File.separator;

        try (PrintStream executionOut =
            new PrintStream(new FileOutputStream(configurationDir.getPath() + File.separator + "Execution.log"))) {
            SequentialSolutionSetEvaluator sequentialSolutionSetEvaluator = new SequentialSolutionSetEvaluator();

            PSPProblem problem = new PSPProblem(proteinChain, 2, population, executionOut);

            NSGAIIHyperHeuristic.Builder builder =
                new NSGAIIHyperHeuristic.Builder(problem, sequentialSolutionSetEvaluator);
            builder.setPopulationSize(population);
            builder.setMaxEvaluations(maxEvaluation);
            builder.setLLHComparator(llhComparator);
            builder.setBacktrackPercentage(backtrackPercentage);
            builder.setAminoAcidSequence(proteinChain);

            List<LowLevelHeuristic> lowLevelHeuristics = LowLevelHeuristic.Builder.generateLowLevelHeuristics(
                crossovers, crossoverProbability, mutations, mutationProbability, alpha, beta);
            builder.setLowLevelHeuristics(lowLevelHeuristics);

            ConfigurationExecutionLogger.logLowLevelHeuristics(lowLevelHeuristics, llhComparator,
                configurationDir.getPath() + File.separator + "LowLevelInfo.txt");

            Selection selection = new BinaryTournament2.Builder().build();
            builder.setSelection(selection);

            SolutionSet allRuns = new SolutionSet();
            long allExecutionTime = 0;
            executionOut.println("Algorithm configured with: ");
            executionOut.println("Pop: " + population);
            executionOut.println("CrP: " + crossoverProbability);
            executionOut.println("MtP: " + mutationProbability);
            executionOut.println("List of crossover: ");
            for (String crossover : crossovers) {
                executionOut.println(crossover);
            }
            executionOut.println("List of mutations: ");
            for (String mutation : mutations) {
                executionOut.println(mutation);
            }

            ConfigurationExecutionLogger.logConfigurationHH(ALGORITHM_NAME, population, null, crossovers, mutations,
                crossoverProbability, mutationProbability, maxEvaluation, proteinChain,
                outputDir + File.separator + configurationFileName);

            for (int i = 0; i < executions; i++) {
                String executionDirectory = outputDir + "EXECUTION_" + i;
                createDir(executionDirectory);

                if (!llhComparator.equals("Random") && logChoiceFunctionBehavior) {
                    builder.setChoiceFunctionLoggerFileName(executionDirectory + File.separator + "choiceBehavior.log");
                }
                NSGAIIHyperHeuristic algorithm = (NSGAIIHyperHeuristic) builder.build("NSGAII");
                algorithm.clearLowLevelHeuristics();

                executionOut.println("Execution: " + (i + 1));

                long initTime = System.currentTimeMillis();
                SolutionSet nonDominatedPopulation = algorithm.execute();
                long estimatedTime = System.currentTimeMillis() - initTime;

                allExecutionTime += estimatedTime;

                problem.removeDominateds(nonDominatedPopulation);
                problem.removeDuplicates(nonDominatedPopulation);

                SolutionSetOutput.printVariablesToFile(nonDominatedPopulation,
                    executionDirectory + File.separator + "VAR.txt");
                SolutionSetOutput.printObjectivesToFile(nonDominatedPopulation,
                    executionDirectory + File.separator + "FUN.txt");

                ConfigurationExecutionLogger.logLowLevelHeuristics(lowLevelHeuristics, llhComparator,
                    executionDirectory + File.separator + "LLH.txt");

                allRuns = allRuns.union(nonDominatedPopulation);
            }

            executionOut.println();
            executionOut.println("End of execution for problem " + problem.getClass().getName() + ".");
            executionOut.println("Total time (seconds): " + allExecutionTime / 1000);
            executionOut.println("Writing results.");
            ConfigurationExecutionLogger.logEndOfExecution(executions, allExecutionTime,
                outputDir + File.separator + configurationFileName);

            problem.removeDominateds(allRuns);
            problem.removeDuplicates(allRuns);

            SolutionSetOutput.printVariablesToFile(allRuns, outputDir + "VAR.txt");
            SolutionSetOutput.printObjectivesToFile(allRuns, outputDir + "FUN.txt");

        } catch (Exception e) {

            ConfigurationExecutionLogger.logMessage("ERROR: Occured while executing C" + this.configuration + ":",
                configurationDir.getPath() + File.separator + "error.log");
            ConfigurationExecutionLogger.logMessage(ExceptionUtils.getStackTrace(e),
                configurationDir.getPath() + File.separator + "Error.log");

            ConfigurationExecutionLogger.logMessage("ERROR: occured while executing C" + this.configuration + ":",
                algorithmPath + File.separator + "executions.log");
            ConfigurationExecutionLogger.logMessage(ExceptionUtils.getStackTrace(e),
                algorithmPath + File.separator + "AllExecutions.log");

        }
        NSGAIIHHTuningMultiobjectiveMain.executedTasks++;

    }

    private static File createDir(String dir) {

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

}
