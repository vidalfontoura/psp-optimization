/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.algorithms.tuning.backtrack;

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
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.algorithms.backtrack.IBEABacktrackInitialization;
import edu.ufpr.cbio.psp.algorithms.loggers.ConfigurationExecutionLogger;
import edu.ufpr.cbio.psp.problem.PSPProblem;

/**
 *
 *
 * @author Vidal
 */
public class IBEABacktrackExecutingTask implements Runnable {

    private static final String ALGORITHM_NAME = "IBEABacktrack";
    private PSPProblem problem;
    private Crossover crossover;
    private double crossoverProbability;
    private double mutationProbability;
    private Mutation mutation;
    private int population;
    private int auxPopulation;
    private int maxEvaluation;
    private String crossoverName;
    private String mutationName;
    private String proteinChain;
    private String algorithmPath;
    private int configuration;
    private String configurationFileName;
    private int executions;
    private double backtrackPercentage;

    public IBEABacktrackExecutingTask(PSPProblem problem, Crossover crossover, double crossoverProbability,
        String crossoverName, Mutation mutation, double mutationProbability, String mutationName, int population,
        int auxPopulation, int maxEvaluation, String proteinChain, String algorithmPath, int configuration,
        String configurationFileName, int executions, double backtrackPercentage) {

        this.problem = problem;
        this.crossover = crossover;
        this.mutation = mutation;
        this.population = population;
        this.auxPopulation = auxPopulation;
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
        this.backtrackPercentage = backtrackPercentage;
    }

    @Override
    public void run() {

        IBEABacktrackInitialization.Builder builder = new IBEABacktrackInitialization.Builder(problem);

        builder.setMutation(mutation);
        builder.setCrossover(crossover);
        builder.setArchiveSize(auxPopulation);
        builder.setPopulationSize(population);
        builder.setMaxEvaluations(maxEvaluation);
        builder.setAminoAcidSequence(proteinChain);
        builder.setPercentageBacktrackPopulation(backtrackPercentage);

        File configurationDir = createDir(algorithmPath + File.separator + "C" + configuration);
        File objectivesDir = createDir(configurationDir.getPath() + File.separator);
        String outputDir = objectivesDir.getPath() + File.separator;

        try (PrintStream executionOut =
            new PrintStream(new FileOutputStream(configurationDir.getPath() + File.separator + "Execution.log"))) {

            SolutionSet allRuns = new SolutionSet();
            long allExecutionTime = 0;
            executionOut.println("Algorithm configured with: ");
            executionOut.println("Pop: " + population);
            executionOut.println("Aux Pop: " + auxPopulation);
            executionOut.println("Crossover: " + crossoverName);
            executionOut.println("CrP: " + crossoverProbability);
            executionOut.println("Mutation: " + mutationName);
            executionOut.println("MtP: " + mutationProbability);
            executionOut.println("Backtrack Percentage: " + backtrackPercentage);
            executionOut.println("Starting executions...");

            ConfigurationExecutionLogger.logConfiguration(ALGORITHM_NAME, population, auxPopulation, crossoverName,
                crossoverProbability, mutationName, mutationProbability, maxEvaluation, proteinChain,
                outputDir + File.separator + configurationFileName, backtrackPercentage);

            for (int i = 0; i < executions; i++) {

                executionOut.println("Execution: " + (i + 1));
                Selection selection = new BinaryTournament2.Builder().build();
                builder.setSelection(selection);
                Algorithm algorithm = builder.build();

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
        IBEABacktrackTuningMultiobjectiveMain.executedTasks++;

    }

    private static File createDir(String dir) {

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

}
