package edu.ufpr.cbio.psp.experiments.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.qualityindicator.util.MetricsUtil;

import edu.ufpr.cbio.psp.qualityIndicators.HypervolumeHandler;
import edu.ufpr.cbio.psp.qualityIndicators.statistics.KruskalWallisTest;

public class TunningExperimentsMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        // Pasta dos resultados
        // String experimentsDir = "results-tuning-bkp";

        // String experimentsDir =
        // "/Users/vfontoura/Documents/Mestrado/top_ia/results-ibeahh/results-ibea-hh-cf";

        // String experimentsDir =
        // "/Users/vfontoura/Documents/Mestrado/top_ia/results-speahh/results-speahh-hhmutation";

        // String experimentsDir =
        // "/Users/vfontoura/workspace/work_master/PSP-Optimization/experiments-newchains";
        String experimentsDir = "experiments-newchains";
        // Pasta do problema
        String[] problems = new String[] { "H12PHPHP2H2P2H2P2HP2H2P2H2P2HP2H2P2H2P2HPHPH12 copy" };

        // Pasta dos algoritmos
        String[] algorithms = new String[] { "IBEA", "NSGAII" };

        boolean debugMode = false;

        MetricsUtil metricsUtil = new MetricsUtil();
        HypervolumeHandler hypervolumeHandler = new HypervolumeHandler();

        // For each problem
        for (int i = 0; i < problems.length; i++) {
            System.out.println("Problem: " + problems[i]);
            File problemDir = new File(experimentsDir + File.separator + problems[i]);

            HashMap<String, double[]> configurationHypervolume = new HashMap<>();
            Map<String, Double> configHypervolumeAvg = new HashMap<>();

            // Search for the fronts of specified algorithms
            Map<String, Map<String, List<SolutionSet>>> algorithmsConfigsSolutions =
                new HashMap<String, Map<String, List<SolutionSet>>>();
            for (int j = 0; j < algorithms.length; j++) {
                String algorithmDirSearch = algorithms[j];

                // Search for the algorithm (algorithmDirSearch) in the
                // directory of the problem
                Stream<Path> paths =
                    Files.list(problemDir.toPath()).filter(
                        algorithmDir -> algorithmDir.getFileName().toString().equals(algorithmDirSearch));
                Optional<Path> findFirst = paths.findFirst();

                if (findFirst.isPresent()) {
                    File algorithmDir = findFirst.get().toFile();

                    File[] configurations = algorithmDir.listFiles();
                    Map<String, List<SolutionSet>> configSolutionSet = new HashMap<String, List<SolutionSet>>();
                    for (File configuration : configurations) {
                        if (configuration.isDirectory()) {
                            File[] executions = configuration.listFiles();
                            List<SolutionSet> solutionsSet = new ArrayList<SolutionSet>();
                            for (File execution : executions) {
                                if (execution.isDirectory()) {
                                    // Read the solutions of each execution

                                    SolutionSet front =
                                        metricsUtil.readNonDominatedSolutionSet(execution.getPath() + File.separator
                                            + "FUN.txt");
                                    hypervolumeHandler.addParetoFront(front);
                                    // Add in this list to keep in the map the
                                    // algorithm
                                    // key and the list of solutions
                                    solutionsSet.add(front);

                                }
                            }
                            configSolutionSet.put(configuration.getName(), solutionsSet);
                        }
                    }

                    algorithmsConfigsSolutions.put(algorithmDirSearch, configSolutionSet);

                    Set<String> algorithmsKeys = algorithmsConfigsSolutions.keySet();
                    double maxAvgHypervolume = Double.MIN_NORMAL;
                    for (String algorithmKey : algorithmsKeys) {
                        System.out.println(algorithmKey);
                        Map<String, List<SolutionSet>> configSolutions = algorithmsConfigsSolutions.get(algorithmKey);
                        Set<String> configs = configSolutions.keySet();

                        for (String config : configs) {
                            List<SolutionSet> frontsByConfig = configSolutions.get(config);

                            System.out.println(config);
                            List<Double> hyperVolumes = frontsByConfig.stream().map(front -> {
                                double frontHypervolume = hypervolumeHandler.calculateHypervolume(front, 2);
                                // System.out.println(frontHypervolume);
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
                            //

                            configHypervolumeAvg.put(config, average);

                            System.out.println(" Avg: " + average);
                            System.out.println(" Std Dev: " + stdDev);
                            System.out.println(" Min: " + min);
                            System.out.println(" Max: " + max);

                            double[] arr = hyperVolumes.stream().mapToDouble(d -> d).toArray();
                            configurationHypervolume.put(config, arr);
                            if (debugMode) {
                                System.out.println("Hypervolume List");
                                for (int t = 0; t < arr.length; t++) {
                                    System.out.print(arr[i] + ",");
                                }
                            }

                        }
                    }

                    configHypervolumeAvg = sortByValue(configHypervolumeAvg);
                    Set<String> configs = configHypervolumeAvg.keySet();
                    for (String config : configs) {
                        System.out.println(config + ":" + configHypervolumeAvg.get(config));
                    }

                    // // Access the executions dir
                    // File[] executions = algorithmDir.listFiles();
                    // for (File execution : executions) {
                    // if (execution.isDirectory()) {
                    // // Read the solutions of each execution
                    //
                    // SolutionSet front =
                    // metricsUtil.readNonDominatedSolutionSet(execution.getPath()
                    // + File.separator
                    // + "FUN.txt");
                    // // Add in this list to keep in the map the algorithm
                    // // key and the list of solutions
                    // algorithmFronts.add(front);
                    // }
                    // }
                    // if (!algorithmFronts.isEmpty()) {
                    // // Add the algorithm name and the fronts found by it
                    // algorithmsFrontsMap.put(algorithmDirSearch,
                    // algorithmFronts);
                    // }
                } else {
                    System.out.println("Output dir does not exist for the specified algorithm: " + algorithmDirSearch);
                }
            }

            // Run the Kruskal and Wallis Test passing the map that contains the
            // algorthims and the list of hypervolumes calculated
            if (configurationHypervolume.size() > 0) {
                HashMap<String, HashMap<String, Boolean>> kruskalWallisTest =
                    KruskalWallisTest.test(configurationHypervolume);

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

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {

        Map<K, V> result = new LinkedHashMap<>();
        Stream<Entry<K, V>> st = map.entrySet().stream();

        st.sorted((o1, o2) -> {
            return o2.getValue().compareTo(o1.getValue());
        }).forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    public static double findDeviation(List<Double> nums, double mean) {

        double squareSum = 0;

        for (int i = 0; i < nums.size(); i++) {
            squareSum += Math.pow(nums.get(i) - mean, 2);
        }

        return Math.sqrt((squareSum) / (nums.size() - 1));
    }
}
