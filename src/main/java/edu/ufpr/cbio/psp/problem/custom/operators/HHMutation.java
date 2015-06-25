package edu.ufpr.cbio.psp.problem.custom.operators;

import java.util.logging.Level;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.encoding.solutiontype.IntSolutionType;
import org.uma.jmetal.encoding.variable.Int;
import org.uma.jmetal.operator.mutation.Mutation;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.random.PseudoRandom;

public class HHMutation extends Mutation {

    private static final long serialVersionUID = -3349165791496573889L;

    private double mutationProbability = 0;

    /** Constructor */
    private HHMutation(Builder builder) {

        addValidSolutionType(IntSolutionType.class);
        mutationProbability = builder.mutationProbability;
    }

    /* Getter */
    public double getMutationProbability() {

        return mutationProbability;
    }

    /** Builder class */
    public static class Builder {

        private double mutationProbability = 0.0;

        public Builder() {

        }

        public Builder(double probability) {

            mutationProbability = probability;
        }

        public Builder setProbability(double probability) {

            mutationProbability = probability;

            return this;
        }

        public HHMutation build() {

            return new HHMutation(this);
        }
    }

    /** Execute() method */
    public Object execute(Object object) throws JMetalException {

        if (null == object) {
            throw new JMetalException("Null parameter");
        } else if (!(object instanceof Solution)) {
            throw new JMetalException("Invalid parameter class");
        }

        Solution solution = (Solution) object;

        if (!solutionTypeIsValid(solution)) {
            throw new JMetalException("HHMutation.execute: the solution type "
                + "is not of the right type. The type should be'Int', but " + solution.getType() + " is obtained");
        }

        doMutation(mutationProbability, solution);
        return solution;
    }

    /**
     * Perform the mutation operation
     *
     * @param probability Mutation setProbability
     * @param solution The solutiontype to mutate
     * @throws org.uma.jmetal.util.JMetalException
     */
    public void doMutation(double probability, Solution solution) throws JMetalException {

        try {

            if (PseudoRandom.randDouble() < probability) {
                int mutationIndex = PseudoRandom.randInt(0, solution.getDecisionVariables().length - 1);
                int value =
                    PseudoRandom.randInt(((Int) solution.getDecisionVariables()[0]).getLowerBound(),
                        ((Int) solution.getDecisionVariables()[0]).getUpperBound());
                ((Int) solution.getDecisionVariables()[mutationIndex]).setValue(value);
            }
        } catch (ClassCastException e1) {
            JMetalLogger.logger.log(Level.SEVERE, "HHMutation.doMutation: ClassCastException error" + e1.getMessage(),
                e1);
            Class<String> cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMetalException("Exception in " + name + ".doMutation()");
        }
    }

}
