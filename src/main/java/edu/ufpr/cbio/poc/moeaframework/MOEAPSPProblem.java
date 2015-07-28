package edu.ufpr.cbio.poc.moeaframework;

import java.util.List;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import edu.ufpr.cbio.psp.problem.domain.Grid;
import edu.ufpr.cbio.psp.problem.domain.Residue;
import edu.ufpr.cbio.psp.problem.utils.Controller;
import edu.ufpr.cbio.psp.problem.utils.ResidueUtils;

public class MOEAPSPProblem extends AbstractProblem {

    private String proteinChain;

    public MOEAPSPProblem(int numberOfVariables, int numberOfObjectives, String proteinChain) {

        super(numberOfVariables, numberOfObjectives);
        this.proteinChain = proteinChain;
    }

    @Override
    public void evaluate(Solution solution) {

        int[] moves = EncodingUtils.getInt(solution);

        Controller controller = new Controller();
        List<Residue> residues = controller.parseInput(proteinChain, moves);
        Grid grid = controller.generateGrid(residues);

        int collisionsCount = ResidueUtils.getCollisionsCount(residues);
        int topologicalContacts = ResidueUtils.getTopologyContacts(residues, grid).size();
        double maxPointsDistance = ResidueUtils.getMaxPointsDistance(residues);
        if (collisionsCount > 0) {
            topologicalContacts = topologicalContacts - collisionsCount;
        }
        if (residues.size() != proteinChain.length()) {
            topologicalContacts = 0;
            maxPointsDistance = 100;
        }

        System.out.println(topologicalContacts);
        System.out.println(maxPointsDistance);
        if (numberOfObjectives == 2) {
            solution.setObjective(0, -topologicalContacts);
            solution.setObjective(1, maxPointsDistance);
        } else if (numberOfObjectives == 1) {
            solution.setObjective(0, -topologicalContacts);
        }

    }

    @Override
    public Solution newSolution() {

        Solution solution = new Solution(numberOfVariables, numberOfObjectives);
        for (int i = 0; i < numberOfVariables; i++) {
            solution.setVariable(i, EncodingUtils.newInt(0, 2));
        }
        return solution;
    }

}
