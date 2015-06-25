package edu.ufpr.cbio.psp.algorithms.hyperheuristic.loggers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic.LowLevelHeuristic;

public class ChoiceFunctionLogger {

    private static DecimalFormat dfChoice = new DecimalFormat("0.00000");
    private static DecimalFormat dfScore = new DecimalFormat("0.00");

    private static String LLH_LINE = "%s - %s %s %s";
    private static String SELECTED_AND_EXECUTED_LLH = "LLH: %s CF: %s";

    public static void logHyperHeuristics(List<LowLevelHeuristic> lowLevelHeuristics, String fileName)
        throws IOException {

        File choiceFunctionFile = new File(fileName);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(choiceFunctionFile, true))) {
            for (LowLevelHeuristic heuristic : lowLevelHeuristics) {
                String name = heuristic.getName().split(" ")[0];
                double score = heuristic.getScore();
                int elapsedTime = heuristic.getElapsedTime();
                double choiceFunctionValue = heuristic.getChoiceFunction();
                fileWriter.write(String.format(LLH_LINE, name, dfScore.format(score), dfScore.format(elapsedTime),
                    dfChoice.format(choiceFunctionValue)));
                fileWriter.newLine();
            }

        }
    }

    public static void logSelectedHyperHeuristic(LowLevelHeuristic lowLevelHeuristic, String fileName)
        throws IOException {

        File choiceFunctionFile = new File(fileName);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(choiceFunctionFile, true))) {
            fileWriter.write(String.format(SELECTED_AND_EXECUTED_LLH, lowLevelHeuristic.getName().split(" ")[0],
                dfChoice.format(lowLevelHeuristic.getChoiceFunction())));
            fileWriter.newLine();

        }
    }

    public static void logSelectedHyperHeuristicScore(LowLevelHeuristic lowLevelHeuristic, String fileName)
        throws IOException {

        File choiceFunctionFile = new File(fileName);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(choiceFunctionFile, true))) {
            fileWriter.write(String.format("LLH: %s score: %s elapsedTime: %s",
                lowLevelHeuristic.getName().split(" ")[0], dfScore.format(lowLevelHeuristic.getScore()),
                (double) lowLevelHeuristic.getElapsedTime()));
            fileWriter.newLine();

        }
    }
}
