package edu.ufpr.cbio.psp.algorithms.tuning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.nsgaII.NSGAII;
import org.uma.jmetal.metaheuristic.multiobjective.nsgaII.NSGAIITemplate;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.evaluator.SequentialSolutionSetEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.algorithms.loggers.ConfigurationExecutionLogger;
import edu.ufpr.cbio.psp.algorithms.tunning.hh.NSGAIIHHTuningMultiobjectiveMain;
import edu.ufpr.cbio.psp.problem.PSPProblem;

public class NSGAIIExecutingTask implements Runnable {

	private static final String ALGORITHM_NAME = "NSGAII";

	private double crossoverProbability;
	private double mutationProbability;
	private int population;
	private int maxEvaluation;
	private String proteinChain;
	private String algorithmPath;
	private int configuration;
	private String configurationFileName;
	private int executions;

	private Crossover crossover;
	private Mutation mutation;

	public NSGAIIExecutingTask(Crossover crossover, double crossoverProbability, Mutation mutation,
			double mutationProbability, int population, int maxEvaluation, String proteinChain, String algorithmPath,
			int configuration, String configurationFileName, int executions) {

		this.population = population;
		this.maxEvaluation = maxEvaluation;
		this.proteinChain = proteinChain;
		this.crossoverProbability = crossoverProbability;
		this.mutationProbability = mutationProbability;
		this.algorithmPath = algorithmPath;
		this.configuration = configuration;
		this.configurationFileName = configurationFileName;
		this.executions = executions;
		this.crossover = crossover;
		this.mutation = mutation;
	}

	@Override
	public void run() {

		File configurationDir = createDir(algorithmPath + File.separator + "C" + configuration);
		File objectivesDir = createDir(configurationDir.getPath() + File.separator);
		String outputDir = objectivesDir.getPath() + File.separator;

		try (PrintStream executionOut = new PrintStream(
				new FileOutputStream(configurationDir.getPath() + File.separator + "Execution.log"))) {
			SequentialSolutionSetEvaluator sequentialSolutionSetEvaluator = new SequentialSolutionSetEvaluator();

			PSPProblem problem = new PSPProblem(proteinChain, 2, population, executionOut);

			NSGAIITemplate.Builder builder = new NSGAIITemplate.Builder(problem, sequentialSolutionSetEvaluator);
			builder.setPopulationSize(population);
			builder.setMaxEvaluations(maxEvaluation);
			builder.setCrossover(crossover);
			builder.setMutation(mutation);

			Selection selection = new BinaryTournament2.Builder().build();
			builder.setSelection(selection);

			SolutionSet allRuns = new SolutionSet();
			long allExecutionTime = 0;
			executionOut.println("Algorithm configured with: ");
			executionOut.println("Pop: " + population);
			executionOut.println("CrP: " + crossoverProbability);
			executionOut.println("MtP: " + mutationProbability);
			executionOut.println();
			executionOut.println("Crossover: " + crossover.getClass());
			executionOut.println("Mutation: " + mutation.getClass());

			ConfigurationExecutionLogger.logConfiguration(ALGORITHM_NAME, population, null,
					crossover.getClass().toString(), crossoverProbability, mutation.getClass().toString(),
					mutationProbability, maxEvaluation, proteinChain,
					outputDir + File.separator + configurationFileName, 0);

			for (int i = 0; i < executions; i++) {
				String executionDirectory = outputDir + "EXECUTION_" + i;
				createDir(executionDirectory);

				NSGAII algorithm = (NSGAII) builder.build("NSGAII");

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
