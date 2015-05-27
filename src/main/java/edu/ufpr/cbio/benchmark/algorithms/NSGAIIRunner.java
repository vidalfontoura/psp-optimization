// NSGAIIRunner.java
//
// Author:
// Antonio J. Nebro <antonio@lcc.uma.es>
//
// Copyright (c) 2014 Antonio J. Nebro
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package edu.ufpr.cbio.benchmark.algorithms;

import java.io.File;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.metaheuristic.multiobjective.nsgaII.NSGAIITemplate;
import org.uma.jmetal.operator.crossover.SBXCrossover;
import org.uma.jmetal.operator.mutation.PolynomialMutation;
import org.uma.jmetal.operator.selection.BinaryTournament2;
import org.uma.jmetal.problem.ProblemFactory;
import org.uma.jmetal.problem.multiobjective.OKA2;
import org.uma.jmetal.qualityindicator.QualityIndicatorGetter;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.evaluator.SequentialSolutionSetEvaluator;
import org.uma.jmetal.util.evaluator.SolutionSetEvaluator;
import org.uma.jmetal.util.fileoutput.DefaultFileOutputContext;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;

/**
 * Class to configure and run the NSGA-II algorithm (including Steady State and
 * parallel versions)
 */
public class NSGAIIRunner {

    /**
     * @param args Command line arguments.
     * @throws org.uma.jmetal.util.JMetalException
     * @throws java.io.IOException
     * @throws SecurityException
     * @throws java.lang.ClassNotFoundException Usage: three options -
     *         NSGAIIRunner - NSGAIIRunner problemName - NSGAIIRunner
     *         problemName paretoFrontFile
     */
    public static void main(String[] args) throws Exception {

        String algorithmDirectoryName = "NSGAII_branch";

        Problem problem;
        Algorithm algorithm;
        Operator crossover;
        Operator mutation;
        Operator selection;

        QualityIndicatorGetter indicators;

        indicators = null;
        if (args.length == 1) {
            Object[] params = { "Real" };
            problem = (new ProblemFactory()).getProblem(args[0], params);
        } else if (args.length == 2) {
            Object[] params = { "Real" };
            problem = (new ProblemFactory()).getProblem(args[0], params);
            indicators = new QualityIndicatorGetter(problem, args[1]);
        } else {
            // problem = new Kursawe("Real", 3);
            // problem = new Water("Real");
            // problem = new ZDT3("ArrayReal", 30);
            // problem = new ConstrEx("Real");
            // problem = new DTLZ1("Real");
            problem = new OKA2("Real");

        }

        String problemName = problem.getName();
        SolutionSetEvaluator evaluator = new SequentialSolutionSetEvaluator();

        crossover = new SBXCrossover.Builder().setDistributionIndex(20.0).setProbability(0.9).build();

        mutation =
            new PolynomialMutation.Builder().setDistributionIndex(20.0)
                .setProbability(1.0 / problem.getNumberOfVariables()).build();

        selection = new BinaryTournament2.Builder().build();

        algorithm =
            new NSGAIITemplate.Builder(problem, evaluator).setCrossover(crossover).setMutation(mutation)
                .setSelection(selection).setMaxEvaluations(25000).setPopulationSize(100).build("NSGAII");

        File problemDir = new File(problemName);
        if (!problemDir.exists()) {
            problemDir.mkdir();
        }

        File algorithmDir = new File(problemDir.getPath() + File.separator + algorithmDirectoryName);
        if (!algorithmDir.exists()) {
            algorithmDir.mkdir();
        }

        for (int i = 0; i < 30; i++) {
            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

            SolutionSet population = algorithmRunner.getSolutionSet();
            long computingTime = algorithmRunner.getComputingTime();

            File executionDir = new File(algorithmDir.getPath() + File.separator + "EXECUTION_" + i);
            if (!executionDir.exists()) {
                executionDir.mkdir();
            }

            new SolutionSetOutput.Printer(population)
                .setSeparator("\t")
                .setVarFileOutputContext(
                    new DefaultFileOutputContext(executionDir.getPath() + File.separator + "VAR.txt"))
                .setFunFileOutputContext(
                    new DefaultFileOutputContext(executionDir.getPath() + File.separator + "FUN.txt")).print();

            JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
            JMetalLogger.logger.info("Objectives values have been written to file FUN.txt");
            JMetalLogger.logger.info("Variables values have been written to file VAR.txt");

        }
        //
        // AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(
        // algorithm).execute();
        //
        // SolutionSet population = algorithmRunner.getSolutionSet();
        // long computingTime = algorithmRunner.getComputingTime();
        //
        // new SolutionSetOutput.Printer(population)
        // .setSeparator("\t")
        // .setVarFileOutputContext(
        // new DefaultFileOutputContext("VAR.tsv"))
        // .setFunFileOutputContext(
        // new DefaultFileOutputContext("FUN.tsv")).print();
        //
        // JMetalLogger.logger.info("Total execution time: " + computingTime
        // + "ms");
        // JMetalLogger.logger
        // .info("Objectives values have been written to file FUN.tsv");
        // JMetalLogger.logger
        // .info("Variables values have been written to file VAR.tsv");
        //
        // if (indicators != null) {
        // JMetalLogger.logger.info("Quality indicators");
        // JMetalLogger.logger.info("Hypervolume: "
        // + indicators.getHypervolume(population));
        // JMetalLogger.logger.info("GD         : "
        // + indicators.getGD(population));
        // JMetalLogger.logger.info("IGD        : "
        // + indicators.getIGD(population));
        // JMetalLogger.logger.info("Spread     : "
        // + indicators.getSpread(population));
        // JMetalLogger.logger.info("Epsilon    : "
        // + indicators.getEpsilon(population));
        // }
    }
}
