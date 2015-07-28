package edu.ufpr.cbio.poc.moeaframework;

import java.util.Arrays;
import java.util.Iterator;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

public class MOEAMain {

    public static void main(String[] args) {

        String proteinChain = "HHHHHHHHHHHHPHPHPPHHPPHHPPHPPHHPPHHPPHPPHHPPHHPPHPHPHHHHHHHHHHHH";
        int variables = proteinChain.length() - 2;

        NondominatedPopulation run =
            new Executor().withProblemClass(MOEAPSPProblem.class, variables, 2, proteinChain).withAlgorithm("IBEA")
                .withMaxEvaluations(50000).withProperty("operator", "2x").withProperty("2x.rate", 0.9)
                /* .withProperty("sbx.distributionIndex", 15.0) */.withProperty("populationSize", 200).run();

        Iterator<Solution> iterator = run.iterator();
        while (iterator.hasNext()) {
            System.out.println(Arrays.toString(iterator.next().getObjectives()));
        }

    }

}
