/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.correlation;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.encoding.solutiontype.wrapper.XInt;

import jmetal.util.JMException;

/**
 *
 *
 * @author Vidal
 */
public class MathCorrelation {

    private int n_vars, n_objs;
    private double[][] data;
    private double[][] result;
    private int size;

    MathCorrelation(SolutionSet population) throws JMException {
        buildMatrixData(population);
    }

    protected void buildMatrixData(SolutionSet population) {

        size = population.size();
        Solution aux = population.get(0);
        n_vars = aux.numberOfVariables();
        n_objs = aux.getNumberOfObjectives();

        data = new double[size][n_vars + n_objs];

        for (int i = 0; i < size; ++i) {
            Solution s = population.get(i);

            XInt vars = new XInt(s);

            int j = 0;
            for (int t = 0; t < vars.size(); t++)
                data[i][j++] = vars.getValue(t);
            for (int k = 0; k < n_objs; ++k)
                data[i][j++] = s.getObjective(k);
        }
    }

    public double[][] covariance() {

        result = (new Covariance(data)).getCovarianceMatrix().getData();
        return result;
    }

    public double[][] pearsonsCorrelation() {

        result = (new PearsonsCorrelation(data)).getCorrelationMatrix().getData();
        return result;
    }

    public double[][] spearmansCorrelation() {

        result = (new SpearmansCorrelation()).computeCorrelationMatrix(data).getData();
        return result;
    }

    public void printToFileDouble(String file) {

        try {
            FileWriter writer = new FileWriter(file);
            PrintWriter print = new PrintWriter(writer);

            for (double[] dd : result) {
                for (double d : dd) {
                    print.printf("%f\t", d);
                }
                print.println();
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printToFileBin(double limiar, String file) {

        try {
            FileWriter writer = new FileWriter(file);
            PrintWriter print = new PrintWriter(writer);

            for (double[] dd : result) {
                for (double d : dd) {
                    if (Math.abs(d) > limiar) {
                        print.printf("1\t");
                    } else {
                        print.printf("0\t");
                    }
                }
                print.println();
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }

}
