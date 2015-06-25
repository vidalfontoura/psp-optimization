package edu.ufpr.cbio.psp.algorithms.hyperheuristc.comparators;

import java.util.Comparator;

import edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic.LowLevelHeuristic;

public class LowLevelHeuristicChoiceFunctionComparator implements Comparator<LowLevelHeuristic> {

    @Override
    public int compare(LowLevelHeuristic o1, LowLevelHeuristic o2) {

        if (o1.getChoiceFunction() > o2.getChoiceFunction()) {
            return -1;
        } else if (o1.getChoiceFunction() < o2.getChoiceFunction()) {
            return 1;
        } else {
            return 0;
        }
    }

}
