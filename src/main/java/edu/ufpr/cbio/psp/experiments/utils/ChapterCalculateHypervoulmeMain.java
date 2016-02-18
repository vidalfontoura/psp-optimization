package edu.ufpr.cbio.psp.experiments.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.qualityindicator.util.MetricsUtil;

import edu.ufpr.cbio.psp.qualityIndicators.HypervolumeHandler;
import edu.ufpr.cbio.psp.qualityIndicators.statistics.KruskalWallisTest;

public class ChapterCalculateHypervoulmeMain {

	public static void main(String[] args) throws IOException, InterruptedException {

		MetricsUtil metricsUtil = new MetricsUtil();
		HypervolumeHandler hypervolumeHandler = new HypervolumeHandler();
		String results = "D:\\work_master\\psp-2D\\sq7";

		File resultDir = new File(results);

		Map<String, double[]> hypervoulmePerAlgorithm = new HashMap<>();
		Map<String, List<SolutionSet>> algorithmsSolutioSets = new HashMap<String, List<SolutionSet>>();

		File[] algorithms = resultDir.listFiles();

		for (File algorithm : algorithms) {

			List<SolutionSet> solutionsSet = new ArrayList<SolutionSet>();
			File[] executions = algorithm.listFiles();
			for (File execution : executions) {
				if (execution.getName().startsWith("EXECUTION_")) {

					SolutionSet front = metricsUtil
							.readNonDominatedSolutionSet(execution.getPath() + File.separator + "FUN.txt");

					hypervolumeHandler.addParetoFront(front);
					solutionsSet.add(front);
				}
			}
			algorithmsSolutioSets.put(algorithm.getName(), solutionsSet);
		}

		Set<String> algorithmsKeys = algorithmsSolutioSets.keySet();
		for (String algorithmName : algorithmsKeys) {
			System.out.println("Algorithm: " + algorithmName);
			List<SolutionSet> frontsByAlgorithm = algorithmsSolutioSets.get(algorithmName);

			List<Double> hyperVolumes = frontsByAlgorithm.stream().map(front -> {
				double frontHypervolume = hypervolumeHandler.calculateHypervolume(front, 2);
				// System.out.println(frontHypervolume);
				return frontHypervolume;
			}).collect(Collectors.toList());

			DoubleSummaryStatistics summaryStatistics = hyperVolumes.stream().mapToDouble(a -> a).summaryStatistics();
			double average = summaryStatistics.getAverage();
			double max = summaryStatistics.getMax();
			double min = summaryStatistics.getMin();
			double stdDev = findDeviation(hyperVolumes, average);

			System.out.println(" Avg: " + average);
			System.out.println(" Std Dev: " + stdDev);
			System.out.println(" Min: " + min);
			System.out.println(" Max: " + max);

			double[] arr = hyperVolumes.stream().mapToDouble(d -> d).toArray();

			hypervoulmePerAlgorithm.put(algorithmName, arr);

		}

		// Run the Kruskal and Wallis Test passing the map that contains the
		// algorthims and the list of hypervolumes calculated
		if (hypervoulmePerAlgorithm.size() > 0) {
			HashMap<String, HashMap<String, Boolean>> kruskalWallisTest = KruskalWallisTest
					.test(hypervoulmePerAlgorithm);

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
