package edu.ufpr.cbio.psp.algorithms.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.spea2.SPEA2;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.operator.selection.Selection;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

import edu.ufpr.cbio.psp.algorithms.hyperheuristic.LowLevelHeuristic;
import edu.ufpr.cbio.psp.algorithms.hyperheuristic.SPEA2HyperHeuristic;
import edu.ufpr.cbio.psp.problem.PSPProblem;
import edu.ufpr.cbio.psp.problem.custom.operators.IntegerTwoPointsCrossover;
import edu.ufpr.cbio.psp.problem.custom.operators.UniformCrossover;

public class SPEA2HyperHeuristicPSPMain {

	public static void main(String[] args) throws Exception {

		File file = new File("results");
		if (!file.exists()) {
			file.mkdir();
		}

		String path = file.getPath()+File.separator+"PSP";
		String algorithms = "SPEA2HH";
		int executions = 30;

		PSPProblem problem; // The problem to solve
		Algorithm algorithm; // The algorithm to use

		String proteinChain =
				"PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";
		int numberOfObjectives = 2;
		problem = new PSPProblem(proteinChain, numberOfObjectives);

		SPEA2HyperHeuristic.Builder builder = new SPEA2HyperHeuristic.Builder(problem);

		int populationSize = 100;
		builder.setPopulationSize(populationSize);

		int maxEvaluations = 25000;
		builder.setMaxEvaluations(maxEvaluations);

		int archiveSize = 100;
		builder.setArchiveSize(archiveSize);

		double crossoverProbability = 0.9;
		List<Crossover> listCrossover = new ArrayList<Crossover>();
		listCrossover.add(new SinglePointCrossover.Builder().setProbability(crossoverProbability).build());
		listCrossover.add(new IntegerTwoPointsCrossover.Builder().crossoverProbability(crossoverProbability).build());
		//listCrossover.add(new UniformCrossover.Builder().crossoverProbability(crossoverProbability).build());

		double mutationProbability = 0.01;//1.0 / problem.getNumberOfVariables();
		
		List<Mutation> listMutation = new ArrayList<Mutation>();
		listMutation.add(new BitFlipMutation.Builder().setProbability(mutationProbability).build());
		listMutation.add(null);

		double alpha = 1.0;
		double beta = 0.000025;
		
		List<LowLevelHeuristic> lowLevelHeuristics = 
				LowLevelHeuristic.Builder.generateLowLevelHeuristics(listCrossover, listMutation, alpha, beta);
		
		builder.setLowLevelHeuristics(lowLevelHeuristics);

		Selection selection = new BinaryTournament2.Builder().build();
		builder.setSelection(selection);

		algorithm = builder.build();

		algorithms += "_" + alpha + "_" + beta;
		
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
