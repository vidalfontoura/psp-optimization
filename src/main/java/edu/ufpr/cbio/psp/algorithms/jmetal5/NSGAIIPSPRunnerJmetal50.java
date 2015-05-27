// package edu.ufpr.cbio.psp.algorithms.jmetal5;
//
// // package org.uma.jmetal.exec.tests;
// // //NSGAIIRunner.java
// // //
// // //Author:
// // // Antonio J. Nebro <antonio@lcc.uma.es>
// // //
// // //Copyright (c) 2014 Antonio J. Nebro
// // //
// // //This program is free software: you can redistribute it and/or modify
// // //it under the terms of the GNU Lesser General Public License as published
// by
// // //the Free Software Foundation, either version 3 of the License, or
// // //(at your option) any later version.
// // //
// // //This program is distributed in the hope that it will be useful,
// // //but WITHOUT ANY WARRANTY; without even the implied warranty of
// // //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// // //GNU Lesser General Public License for more details.
// // //
// // //You should have received a copy of the GNU Lesser General Public License
// // //along with this program. If not, see <http://www.gnu.org/licenses/>.
// //
// import java.io.File;
// import java.util.List;
//
// import org.uma.jmetal.algorithm.Algorithm;
// import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
// import org.uma.jmetal.operator.CrossoverOperator;
// import org.uma.jmetal.operator.MutationOperator;
// import org.uma.jmetal.operator.SelectionOperator;
// import org.uma.jmetal.operator.impl.crossover.IntegerSBXCrossover;
// import org.uma.jmetal.operator.impl.mutation.IntegerPolynomialMutation;
// import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
// import org.uma.jmetal.problem.IntegerProblem;
// import org.uma.jmetal.solution.IntegerSolution;
// import org.uma.jmetal.util.AlgorithmRunner;
// import org.uma.jmetal.util.JMetalException;
// import org.uma.jmetal.util.JMetalLogger;
// import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
// import org.uma.jmetal.util.fileoutput.SolutionSetOutput;
// import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
//
// /**
// * Class to configure and run the NSGA-II algorithm
// *
// * @author Antonio J. Nebro <antonio@lcc.uma.es>
// */
// public class NSGAIIPSPRunnerJmetal50 {
//
// /**
// * @param args Command line arguments.
// * @throws java.io.IOException
// * @throws SecurityException
// * @throws ClassNotFoundException Usage: two options -
// * org.uma.jmetal.runner.multiobjective.NSGAIIRunner -
// * org.uma.jmetal.runner.multiobjective.NSGAIIRunner problemName
// */
// public static void main(String[] args) throws JMetalException {
//
// IntegerProblem problem;
// Algorithm<List<IntegerSolution>> algorithm;
// CrossoverOperator<List<IntegerSolution>, List<IntegerSolution>> crossover;
// MutationOperator<IntegerSolution> mutation;
// SelectionOperator selection;
//
// File problemDir = new File("PSP");
// String nsgaIIVersion = "NSGAII_newest";
// if (!problemDir.exists()) {
// problemDir.mkdir();
// }
//
// File algorithmDir = new File(problemDir.getPath() + File.separator +
// nsgaIIVersion);
// if (!algorithmDir.exists()) {
// algorithmDir.mkdir();
// }
//
// String proteinChain =
// "PPPPPPHPHHPPPPPHHHPHHHHHPHHPPPPHHPPHHPHHHHHPHHHHHHHHHHPHHPHHHHHHHPPPPPPPPPPPHHHHHHHPPHPHHHPPPPPPHPHH";
// int numberOfObjectives = 2;
// problem = new PSPProblemJmetal50(proteinChain, numberOfObjectives);
//
// double crossoverProbability = 0.9;
// double crossoverDistributionIndex = 20.0;
// crossover = new IntegerSBXCrossover(crossoverProbability,
// crossoverDistributionIndex);
//
// double mutationProbability = 1.0 / problem.getNumberOfVariables();
// double mutationDistributionIndex = 20.0;
// mutation = new IntegerPolynomialMutation(mutationProbability,
// mutationDistributionIndex);
//
// selection = new BinaryTournamentSelection(new
// RankingAndCrowdingDistanceComparator());
//
// algorithm =
// new NSGAIIBuilder<IntegerSolution>(problem, crossover, mutation,
// NSGAIIBuilder.NSGAIIVariant.NSGAII)
// .setSelectionOperator(selection).setMaxIterations(250).setPopulationSize(100).build();
//
// new NSGAIIBuilder<IntegerSolution>(problem, crossover, mutation,
// NSGAIIBuilder.NSGAIIVariant.NSGAII).build();
//
// for (int i = 0; i < 30; i++) {
//
// AlgorithmRunner algorithmRunner = new
// AlgorithmRunner.Executor(algorithm).execute();
//
// List<IntegerSolution> population = algorithm.getResult();
// long computingTime = algorithmRunner.getComputingTime();
//
// File executionDir = new File(algorithmDir.getPath() + File.separator +
// "EXECUTION_" + i);
// if (!executionDir.exists()) {
// executionDir.mkdir();
// }
//
// new SolutionSetOutput.Printer(population)
// .setSeparator("\t")
// .setVarFileOutputContext(
// new DefaultFileOutputContext(executionDir.getPath() + File.separator +
// "VAR.tsv"))
// .setFunFileOutputContext(
// new DefaultFileOutputContext(executionDir.getPath() + File.separator +
// "FUN.tsv")).print();
//
// // JMetalLogger.logger.info("Total execution time: " + computingTime
// JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
// JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
// }
// }
// }
