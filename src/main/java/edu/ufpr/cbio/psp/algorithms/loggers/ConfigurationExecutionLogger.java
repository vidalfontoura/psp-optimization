package edu.ufpr.cbio.psp.algorithms.loggers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.List;

import edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic.LowLevelHeuristic;

public class ConfigurationExecutionLogger {

    private static DecimalFormat df = new DecimalFormat("0.00000000");

    public static void logConfiguration(String algorithm, int population, Integer auxPopulation, String crosssover,
                                        double crossoverProbability, String mutation, double mutationProbability,
                                        int maxEvaluations, String proteinChain, String fileName) throws IOException {

        File outputFile = new File(fileName);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile))) {
            fileWriter.append("Algorithm: " + algorithm);
            fileWriter.newLine();
            fileWriter.append("Crossover: " + crosssover);
            fileWriter.newLine();
            fileWriter.append("Cx Rate: " + crossoverProbability);
            fileWriter.newLine();
            fileWriter.append("Mutation: " + mutation);
            fileWriter.newLine();
            fileWriter.append("Mut Rate: " + mutationProbability);
            fileWriter.newLine();
            fileWriter.append("Population: " + population);
            fileWriter.newLine();
            fileWriter.append("Max Evaluations: " + maxEvaluations);
            fileWriter.newLine();
            if (auxPopulation != null) {
                fileWriter.append("Aux Pop: " + auxPopulation);
                fileWriter.newLine();
            }
            fileWriter.write("ProteinChain: " + proteinChain);
            fileWriter.newLine();

        }
    }

    public static void logConfigurationHH(String algorithm, int population, Integer auxPopulation, String[] crosovers,
                                          String[] mutations, double crossoverProbability, double mutationProbability,
                                          int maxEvaluations, String proteinChain, String fileName) throws IOException {

        File outputFile = new File(fileName);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile))) {
            fileWriter.append("Algorithm: " + algorithm);
            fileWriter.newLine();
            fileWriter.append("Cx Rate: " + crossoverProbability);
            fileWriter.newLine();
            fileWriter.append("Mut Rate: " + mutationProbability);
            fileWriter.newLine();
            fileWriter.append("Population: " + population);
            fileWriter.newLine();
            fileWriter.append("Max Evaluations: " + maxEvaluations);
            fileWriter.newLine();
            if (auxPopulation != null) {
                fileWriter.append("Aux Pop: " + auxPopulation);
                fileWriter.newLine();
            }
            fileWriter.append("List of crossovers: ");
            fileWriter.newLine();
            for (String crossover : crosovers) {
                fileWriter.append(crossover);
                fileWriter.newLine();
            }

            for (String mutation : mutations) {
                fileWriter.append(mutation);
                fileWriter.newLine();
            }
            fileWriter.write("ProteinChain: " + proteinChain);
            fileWriter.newLine();

        }
    }

    public static void logMessage(String message, String fileName) {

        try (PrintStream errorOut = new PrintStream(new FileOutputStream(fileName, true))) {
            errorOut.println(message);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void logAllConfiguration(int configId, String algoritm, int population, Integer auxPopulation,
                                           String crosssover, double crossoverProbability, String mutation,
                                           double mutationProbability, int maxEvaluations, String fileName)
        throws IOException {

        File outputFile = new File(fileName);

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile, true))) {

            fileWriter.write("C" + configId + ": " + population + "," + auxPopulation + "," + maxEvaluations + ","
                + crosssover + "," + crossoverProbability + "," + mutation + "," + mutationProbability);
            fileWriter.newLine();
        }
    }

    public static void logAllConfigurationHH(int configId, String algoritm, int population, Integer auxPopulation,
                                             String[] crosssover, double crossoverProbability, String[] mutation,
                                             double mutationProbability, int maxEvaluations, String fileName,
                                             double alpha, double beta) throws IOException {

        File outputFile = new File(fileName);

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile, true))) {

            fileWriter.write("C" + configId + ": " + population + "," + auxPopulation + "," + maxEvaluations + ","
                + alpha + "," + df.format(beta));
            fileWriter.newLine();
        }
    }

    public static void logEndOfExecution(int numberOfExecutions, long allExecutionTime, String fileName)
        throws IOException {

        File outputFile = new File(fileName);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile, true))) {
            fileWriter.write("Total time taken to execute " + numberOfExecutions + " times : " + allExecutionTime
                / 1000 + "(seconds)");
            fileWriter.newLine();
        }
    }

    public static void logLowLevelHeuristics(List<LowLevelHeuristic> lowLevelHeuristics, String llhComparator,
                                             String fileName) throws IOException {

        File outputFile = new File(fileName);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile))) {
            for (LowLevelHeuristic lowLevelHeuristic : lowLevelHeuristics) {
                fileWriter.write(lowLevelHeuristic.getName() + " " + lowLevelHeuristic.getNumberOfTimesApplied());
                fileWriter.newLine();
            }
        }

    }
}
