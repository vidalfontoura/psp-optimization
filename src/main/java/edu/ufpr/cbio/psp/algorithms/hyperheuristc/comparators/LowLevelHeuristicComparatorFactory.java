package edu.ufpr.cbio.psp.algorithms.hyperheuristc.comparators;

import java.util.Comparator;

import edu.ufpr.cbio.psp.algorithms.hyperheuristic.lowlevelheuristic.LowLevelHeuristic;

public class LowLevelHeuristicComparatorFactory {

    public static final String NAME_COMPARATOR = "Name";
    public static final String CHOICE_FUNCTION_COMPARATOR = LowLevelHeuristic.CHOICE_FUNCTION;

    // public static final String MULTI_ARMED_BANDIT_COMPARATOR =
    // LowLevelHeuristic.MULTI_ARMED_BANDIT;
    public static final String RANDOM_COMPARATOR = LowLevelHeuristic.RANDOM;

    public static Comparator<LowLevelHeuristic> createComparator(String name) {

        switch (name) {
            case CHOICE_FUNCTION_COMPARATOR:
                return new LowLevelHeuristicChoiceFunctionComparator();
            case RANDOM_COMPARATOR:
                return new LowLevelHeuristicRandomComparator();
                // case NAME_COMPARATOR:
                // return new LowLevelHeuristicNameComparator();
                // case MULTI_ARMED_BANDIT_COMPARATOR:
                // return new LowLevelHeuristicMultiArmedBanditComparator();
            default:
                return null;
        }
    }

}
