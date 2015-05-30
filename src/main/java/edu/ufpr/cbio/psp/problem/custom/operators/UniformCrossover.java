package edu.ufpr.cbio.psp.problem.custom.operators;

import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.util.JMetalException;

public class UniformCrossover extends Crossover{

	private double crossoverProbability ;
	
	public UniformCrossover(double crossoverProbability) {
		super();
		this.crossoverProbability = crossoverProbability;
	}

	@Override
	public Object execute(Object object) throws JMetalException {
		// TODO Auto-generated method stub
		return null;
	}

}
