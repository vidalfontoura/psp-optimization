package edu.ufpr.cbio.psp.experiments.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.qualityindicator.util.MetricsUtil;

import edu.ufpr.cbio.psp.qualityIndicators.HypervolumeHandler;
import edu.ufpr.cbio.psp.qualityIndicators.statistics.KruskalWallisTest;

public class ExperimentsMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        // Pasta dos resultados
        // String experimentsDir = "results";
        String experimentsDir = "results-compare-spea2-ibea-bests";
        // Pasta do problema
        // String[] problems = new String[] { "psp" };
        String[] problems = new String[] { "P6HPH2P5H3PH5PH2P4H2P2H2PH5PH10PH2PH7P11H7P2HPH3P6HPH2" };

        // Pasta dos algoritmos
        // String[] algorithms =
        // new String[] {
        // "SPEA2-new-config_SinglePointCrossover_BitFlipMutation",
        // "SPEA2HH-new-config-tests" };

        String[] algorithms = new String[] { "SPEA2", "IBEA" };
        boolean debugMode = false;

        MetricsUtil metricsUtil = new MetricsUtil();
        HypervolumeHandler hypervolumeHandler = new HypervolumeHandler();

        // For each problem
        for (int i = 0; i < problems.length; i++) {
            System.out.println("Problem: " + problems[i]);
            File problemDir = new File(experimentsDir + File.separator + problems[i]);

            Map<String, List<SolutionSet>> algorithmsFrontsMap = new HashMap<>();
            HashMap<String, double[]> algorithmsHypervolumes = new HashMap<>();

            // Search for the fronts of specified algorithms
            for (int j = 0; j < algorithms.length; j++) {
                String algorithmDirSearch = algorithms[j];

                // Search for the algorithm (algorithmDirSearch) in the
                // directory of the problem
                Stream<Path> paths =
                    Files.list(problemDir.toPath()).filter(
                        algorithmDir -> algorithmDir.getFileName().toString().equals(algorithmDirSearch));
                Optional<Path> findFirst = paths.findFirst();

                List<SolutionSet> algorithmFronts = new ArrayList<>();
                if (findFirst.isPresent()) {
                    File algorithmDir = findFirst.get().toFile();
                    // Access the executions dir
                    File[] executions = algorithmDir.listFiles();
                    for (File execution : executions) {
                        if (execution.isDirectory()) {
                            // Read the solutions of each execution
                            SolutionSet front =
                                metricsUtil.readNonDominatedSolutionSet(execution.getPath() + File.separator
                                    + "FUN.txt");
                            // Add in this list to keep in the map the algorithm
                            // key and the list of solutions
                            algorithmFronts.add(front);
                        }
                    }
                    if (!algorithmFronts.isEmpty()) {
                        // Add the algorithm name and the fronts found by it
                        algorithmsFrontsMap.put(algorithmDirSearch, algorithmFronts);
                    }
                } else {
                    System.out.println("Output dir does not exist for the specified algorithm: " + algorithmDirSearch);
                }
            }

            // Adding in the HypervolumeHandler the fronts that were found for
            // each algorithm
            Set<String> algorithmsFoundSet = algorithmsFrontsMap.keySet();
            List<String> algorithmsFound = algorithmsFoundSet.stream().sorted().collect(Collectors.toList());
            for (String algorithmFound : algorithmsFound) {
                List<SolutionSet> frontsByAlgorithm = algorithmsFrontsMap.get(algorithmFound);
                // For each front found by the specified algorithm (algorithm
                // found) add in the hypervolumeHandler
                frontsByAlgorithm.stream().forEach(front -> hypervolumeHandler.addParetoFront(front));
            }

            // Now that we have added all fronts found to the hypervolume
            // handler we will calculate the hypervolume for each front
            for (String algorithmFound : algorithmsFound) {
                List<SolutionSet> frontsByAlgorithm = algorithmsFrontsMap.get(algorithmFound);
                // Here we convert the solution set list to a list of double
                // that represents the hypervolume of each front, calculating
                // using the hypervolume handler
                List<Double> hyperVolumes = frontsByAlgorithm.stream().map(front -> {
                    double frontHypervolume = hypervolumeHandler.calculateHypervolume(front, 2);
                    return frontHypervolume;
                }).collect(Collectors.toList());

                // For each algorithm found we
                // Summarize the statistics here
                DoubleSummaryStatistics summaryStatistics =
                    hyperVolumes.stream().mapToDouble(a -> a).summaryStatistics();
                double average = summaryStatistics.getAverage();
                double max = summaryStatistics.getMax();
                double min = summaryStatistics.getMin();
                double stdDev = findDeviation(hyperVolumes, average);

                System.out.println(algorithmFound);
                System.out.println(" Avg: " + average);
                System.out.println(" Std Dev: " + stdDev);
                System.out.println(" Min: " + min);
                System.out.println(" Max: " + max);

                double[] arr = hyperVolumes.stream().mapToDouble(d -> d).toArray();
                algorithmsHypervolumes.put(algorithmFound, arr);
                if (debugMode) {
                    System.out.println("Hypervolume List");
                    for (int t = 0; t < arr.length; t++) {
                        System.out.print(arr[i] + ",");
                    }
                }

            }

            // Run the Kruskal and Wallis Test passing the map that contains the
            // algorthims and the list of hypervolumes calculated
            if (algorithmsHypervolumes.size() > 0) {
                HashMap<String, HashMap<String, Boolean>> kruskalWallisTest =
                    KruskalWallisTest.test(algorithmsHypervolumes);

                Set<String> testedAlgorithmsSet = kruskalWallisTest.keySet();
                List<String> testedAlgorithms = testedAlgorithmsSet.stream().sorted().collect(Collectors.toList());
                System.out.println("Kruskal and Wallis");
                for (String testedAlgorithm : testedAlgorithms) {
                    HashMap<String, Boolean> testMap = kruskalWallisTest.get(testedAlgorithm);
                    for (String vsAlgorithm : testMap.keySet()) {
                        Boolean isDiffer = testMap.get(vsAlgorithm);
                        System.out.println(testedAlgorithm + " vs " + vsAlgorithm + ":" + isDiffer);
                    }
                }

            }
            System.out.println();
        }

    }

    public static double findDeviation(List<Double> nums, double mean) {

        double squareSum = 0;

        for (int i = 0; i < nums.size(); i++) {
            squareSum += Math.pow(nums.get(i) - mean, 2);
        }

        return Math.sqrt((squareSum) / (nums.size() - 1));
    }
}
