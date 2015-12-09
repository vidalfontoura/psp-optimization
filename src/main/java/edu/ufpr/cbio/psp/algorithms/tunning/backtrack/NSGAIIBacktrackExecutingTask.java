package edu.ufpr.cbio.psp.algorithms.tunning.backtrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.evaluator.MultithreadedSolutionSetEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.algorithms.backtrack.NSGAIIBacktrakInitializationTemplate;
import edu.ufpr.cbio.psp.algorithms.loggers.ConfigurationExecutionLogger;
import edu.ufpr.cbio.psp.problem.PSPProblem;

public class NSGAIIBacktrackExecutingTask implements Runnable {

    private static final String ALGORITHM_NAME = "NSGAIIBacktrack";
    private PSPProblem problem;
    private Crossover crossover;
    private double crossoverProbability;
    private double mutationProbability;
    private Mutation mutation;
    private int population;
    private int maxEvaluation;
    private String crossoverName;
    private String mutationName;
    private String proteinChain;
    private String algorithmPath;
    private int configuration;
    private String configurationFileName;
    private int executions;
    private int evaluatorThreads;
    private double backtrackPercentage;

    public NSGAIIBacktrackExecutingTask(PSPProblem problem, Crossover crossover, double crossoverProbability,
        String crossoverName, Mutation mutation, double mutationProbability, String mutationName, int population,
        int maxEvaluation, String proteinChain, String algorithmPath, int configuration, String configurationFileName,
        int executions, int evaluatorThreads, double backtrackPercentage) {

        this.problem = problem;
        this.crossover = crossover;
        this.mutation = mutation;
        this.population = population;
        this.maxEvaluation = maxEvaluation;
        this.proteinChain = proteinChain;
        this.crossoverName = crossoverName;
        this.mutationName = mutationName;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
        this.algorithmPath = algorithmPath;
        this.configuration = configuration;
        this.configurationFileName = configurationFileName;
        this.executions = executions;
        this.evaluatorThreads = evaluatorThreads;
        this.backtrackPercentage = backtrackPercentage;
    }

    @Override
    public void run() {

        MultithreadedSolutionSetEvaluator evaluator = new MultithreadedSolutionSetEvaluator(evaluatorThreads, problem);

        NSGAIIBacktrakInitializationTemplate.Builder builder =
            new NSGAIIBacktrakInitializationTemplate.Builder(problem, evaluator);

        builder.setMutation(mutation);
        builder.setCrossover(crossover);
        builder.setPopulationSize(population);
        builder.setMaxEvaluations(maxEvaluation);
        builder.setAminoAcidSequence(proteinChain);
        builder.setBacktrackPercentage(backtrackPercentage);

        File configurationDir = createDir(algorithmPath + File.separator + "C" + configuration);
        File objectivesDir = createDir(configurationDir.getPath() + File.separator);
        String outputDir = objectivesDir.getPath() + File.separator;

        try (PrintStream executionOut =
            new PrintStream(new FileOutputStream(configurationDir.getPath() + File.separator + "Execution.log"))) {

            SolutionSet allRuns = new SolutionSet();
            long allExecutionTime = 0;
            executionOut.println("Algorithm configured with: ");
            executionOut.println("Pop: " + population);
            executionOut.println("Crossover: " + crossoverName);
            executionOut.println("CrP: " + crossoverProbability);
            executionOut.println("Mutation: " + mutationName);
            executionOut.println("MtP: " + mutationProbability);
            executionOut.println("BacktrackPercentage: " + backtrackPercentage);
            executionOut.println("Starting executions...");

            ConfigurationExecutionLogger.logConfiguration(ALGORITHM_NAME, population, 0, crossoverName,
                crossoverProbability, mutationName, mutationProbability, maxEvaluation, proteinChain,
                outputDir + File.separator + configurationFileName, 0);

            for (int i = 0; i < executions; i++) {

                executionOut.println("Execution: " + (i + 1));
                Selection selection = new BinaryTournament2.Builder().build();
                builder.setSelection(selection);
                Algorithm algorithm = builder.build("NSGAIIBacktrackInitialization");

                long initTime = System.currentTimeMillis();
                SolutionSet nonDominatedPopulation = algorithm.execute();
                long estimatedTime = System.currentTimeMillis() - initTime;

                allExecutionTime += estimatedTime;

                String executionDirectory = outputDir + "EXECUTION_" + i;
                createDir(executionDirectory);

                problem.removeDominateds(nonDominatedPopulation);
                problem.removeDuplicates(nonDominatedPopulation);

                SolutionSetOutput.printVariablesToFile(nonDominatedPopulation,
                    executionDirectory + File.separator + "VAR.txt");
                SolutionSetOutput.printObjectivesToFile(nonDominatedPopulation,
                    executionDirectory + File.separator + "FUN.txt");

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
                configurationDir.getPath() + File.separator + "Error.log");
            ConfigurationExecutionLogger.logMessage(ExceptionUtils.getStackTrace(e),
                configurationDir.getPath() + File.separator + "Error.log");

            ConfigurationExecutionLogger.logMessage("ERROR: occured while executing C" + this.configuration + ":",
                algorithmPath + File.separator + "AllExecutions.log");
            ConfigurationExecutionLogger.logMessage(ExceptionUtils.getStackTrace(e),
                algorithmPath + File.separator + "AllExecutions.log");

        }
        NSGAIIBacktrackInitializationMultiobjectiveMain.executedTasks++;

    }

    private static File createDir(String dir) {

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

}
