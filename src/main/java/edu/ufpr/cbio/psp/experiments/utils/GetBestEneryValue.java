/*
 * Copyright 2016, Charter Communications,  All rights reserved.
 */
package edu.ufpr.cbio.psp.experiments.utils;

import java.io.File;
import java.io.IOException;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.qualityindicator.util.MetricsUtil;

/**
 *
 *
 * @author vfontoura
 */
public class GetBestEneryValue {

	public static void main(String[] args) throws IOException {
		MetricsUtil metricsUtil = new MetricsUtil();
		String path = "D:\\work_master\\psp-2D\\sq8\\M_NSGAII";

		File resultDir = new File(path);

		String[] results = resultDir.list();

		double minEnergy = 0.0;
		int amountOfExecutionsWithBest = 0;
		for (int i = 0; i < results.length; i++) {
			String executionStr = path + File.separator + results[i];

			File executionFile = new File(executionStr);
			System.out.println(executionFile.getName());
			if (executionFile.getName().startsWith("EXECUTION_")) {

				for (File result : executionFile.listFiles()) {
					if (result.getName().equals("FUN.txt")) {
						SolutionSet solutionSet = metricsUtil.readNonDominatedSolutionSet(result.getAbsolutePath());

						for (Solution solution : solutionSet.getSolutionsList()) {
							double objective = solution.getObjective(0);

							if (objective < minEnergy) {
								minEnergy = objective;
								amountOfExecutionsWithBest = 0;
							} else if (objective == minEnergy) {
								amountOfExecutionsWithBest++;
							}

						}
						solutionSet.getSolutionsList().stream().forEach(s -> System.out.println(s.getObjective(0)));
					}
				}
			}
		}

		System.out.println("Min energy " + minEnergy);
		System.out.println("How many executions found this value " + amountOfExecutionsWithBest);

	}

}
