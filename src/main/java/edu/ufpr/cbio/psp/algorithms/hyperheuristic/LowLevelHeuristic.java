package edu.ufpr.cbio.psp.algorithms.hyperheuristic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.operator.crossover.Crossover;
import org.uma.jmetal.operator.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.BitFlipMutation;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;

public class LowLevelHeuristic extends Operator{

	private static final long serialVersionUID = 1L;

	private String name;

	private Crossover crossover;
	private Mutation mutation;

	private double score;
	private int elapsedTime;

	private double alpha;
	private double beta;

	private LowLevelHeuristic(Builder builder) {
		this.name = builder.name;
		this.crossover = builder.crossover;
		this.mutation = builder.mutation;
		this.alpha = builder.alpha;
		this.beta = builder.beta;
		this.score = 0.0;
		this.elapsedTime = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Operator getCrossover() {
		return crossover;
	}

	public void setCrossover(Crossover crossover) {
		this.crossover = crossover;
	}

	public Operator getMutation() {
		return mutation;
	}

	public void setMutation(Mutation mutation) {
		this.mutation = mutation;
	}

	public double getScore() {
		return score;
	}

	public int getElapsedTime() {
		return elapsedTime;
	}

	public void executed() {
		updateElapsedTime(true);
	}

	public void notExecuted() {
		updateElapsedTime(false);
	}

	private void updateElapsedTime(boolean executed) {
		this.elapsedTime = (executed) ? 0 : this.elapsedTime + 1;
	}

	public void updateScore(Solution[] parents, Solution[] offsprings) {
		Comparator<Solution> comparator = new DominanceComparator();
		score = 0.0;
		for (Solution parent : parents) {
			for(Solution offspring : offsprings) {
				score += ( comparator.compare(parent, offspring) + 1.0 ) / 2.0;
			}
		}
		score /= ((double) parents.length * (double) offsprings.length);
	}

	public double getChoiceFunction() {
		return (alpha * score) + (beta * elapsedTime);
	}

	@Override
	public Object execute(Object parents) throws JMetalException {

		Solution[] offSpring = (Solution[]) crossover.execute(parents);

		if(mutation != null) {
			for(Solution offSpringSolution : offSpring) {
				mutation.execute(offSpringSolution);
			}
		}

		executed();

		return offSpring;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LowLevelHeuristic) {
			if (!this.crossover.equals(((LowLevelHeuristic) obj).crossover)) {
				return false;
			}
			if(mutation == null) {
				if (mutation != ((LowLevelHeuristic) obj).mutation) {
					return false;
				}
			} else if (!this.mutation.equals(((LowLevelHeuristic) obj).mutation)) {
				return false; 
			}
			return true;
		}
		return false;
	}

	public static class Builder {
		private String name;
		private Crossover crossover;
		private Mutation mutation;
		private double alpha;
		private double beta;
		
		public Builder() {
			this.name = "SinglePointCrossover_BitFlipMutation";
			this.crossover = new SinglePointCrossover.Builder().build();
			this.mutation = new BitFlipMutation.Builder().build();
			this.alpha = 1;
			this.beta = 1;
		}
		
		public Builder setName(String name) {
			this.name = name;
			
			return this;
		}
		
		public Builder setCrossover(Crossover crossover) {
			this.crossover = crossover;

			return this;
		}
		
		public Builder setMutation(Mutation mutation) {
			this.mutation = mutation;

			return this;
		}
		
		public Builder setAlpha(double alpha) {
			this.alpha = alpha;

			return this;
		}
		
		public Builder setBeta(double beta) {
			this.beta = beta;

			return this;
		}

		public LowLevelHeuristic build() {
			return new LowLevelHeuristic(this);
		}
		
		public static List<LowLevelHeuristic> generateLowLevelHeuristics(List<Crossover> listCrossover, List<Mutation> listMutation, double alpha, double beta) {

			List<LowLevelHeuristic> lowLevelHeuristics = new ArrayList<LowLevelHeuristic>();

			for (Crossover crossover : listCrossover) {
				for (Mutation mutation : listMutation) {
					LowLevelHeuristic.Builder builder = new LowLevelHeuristic.Builder();

					if(mutation != null)
						builder.setName(crossover.getClass().getSimpleName()+"_"+mutation.getClass().getSimpleName());
					else
						builder.setName(crossover.getClass().getSimpleName()+"_NullMutation");
					builder.setCrossover(crossover);
					builder.setMutation(mutation);
					builder.setAlpha(alpha);
					builder.setBeta(beta);
					lowLevelHeuristics.add(builder.build());
				}
			}

			return lowLevelHeuristics;
		}
	}
}
