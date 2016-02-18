/*
 * Copyright 2016, Charter Communications,  All rights reserved.
 */
package edu.ufpr.cbio.psp.experiments.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.uma.jmetal.qualityindicator.util.MetricsUtil;

/**
 *
 *
 * @author vfontoura
 */
public class GetNonDominatedFrontier {

	public static void main(String[] args) throws IOException {
		MetricsUtil metricsUtil = new MetricsUtil();
		String path = "D:\\work_master\\psp-2D\\sq8\\nsgaii-result";

		File resultDir = new File(path);

		String[] results = resultDir.list();

		for (int i = 0; i < results.length; i++) {
			String executionStr = path + File.separator + results[i];

			File executionFile = new File(executionStr);
			executionFile = new File(executionFile.listFiles()[0].getPath());
			executionFile = new File(executionFile.listFiles()[0].getPath());

			for (int j = 0; j < executionFile.list().length; j++) {
				File file = executionFile.listFiles()[j];

				if (file.getName().equals("C0")) {

					File[] listFiles = file.listFiles();
					for (File fileEnd : listFiles) {
						String newDir = path + File.separator + "EXECUTION_" + i;
						File newFile = new File(newDir);
						newFile.mkdir();

						String dest = path + File.separator + "EXECUTION_" + i;
						Path destination = Paths.get(dest + File.separator + fileEnd.getName());

						Files.copy(Paths.get(fileEnd.getAbsolutePath()), destination);

					}

				}
			}

		}

	}

}
