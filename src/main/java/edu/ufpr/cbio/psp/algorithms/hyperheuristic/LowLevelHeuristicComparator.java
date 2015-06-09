package edu.ufpr.cbio.psp.algorithms.hyperheuristic;

import java.util.Comparator;

public class LowLevelHeuristicComparator implements Comparator<LowLevelHeuristic> {

	@Override
	public int compare(LowLevelHeuristic llh1, LowLevelHeuristic llh2) {
		return Double.compare(llh2.getChoiceFunction(), llh1.getChoiceFunction());
	}

}
