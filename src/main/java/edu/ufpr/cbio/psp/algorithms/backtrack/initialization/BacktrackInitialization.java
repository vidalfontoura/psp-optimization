/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.algorithms.backtrack.initialization;

import com.google.common.collect.Lists;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.encoding.solutiontype.wrapper.XInt;

import edu.ufpr.cbio.psp.problem.PSPProblem;
import edu.ufpr.cbio.psp.problem.domain.Grid;
import edu.ufpr.cbio.psp.problem.domain.Residue;
import edu.ufpr.cbio.psp.problem.utils.Controller;
import edu.ufpr.cbio.psp.problem.utils.MovementEnum;
import edu.ufpr.cbio.psp.problem.utils.ResidueUtils;
import jmetal.util.PseudoRandom;

/**
 *
 *
 * @author vfontoura
 */
public class BacktrackInitialization {

    private int[][] grid;

    private List<MovementEnum> movements;

    private int count;

    private int sequenceLength;

    private List<MovementEnum> solutionMoves;

    private Problem problem;

    public BacktrackInitialization(Problem problem, String sequence) {

        this.sequenceLength = sequence.length();
        this.problem = problem;
    }

    public SolutionSet createPopulationAsIntegerSolution(int amountOfSolutions) throws ClassNotFoundException {

        SolutionSet solutionSet = new SolutionSet(amountOfSolutions);

        Solution newSolution;
        for (int i = 0; i < amountOfSolutions; i++) {
            newSolution = new Solution(problem);

            List<List<MovementEnum>> listMovementEnum = this.createPopulationAsListMovementEnum(amountOfSolutions);
            XInt vars = new XInt(newSolution);
            for (List<MovementEnum> movementEnum : listMovementEnum) {
                for (int j = 0; j < newSolution.getDecisionVariables().length; j++) {
                    vars.setValue(j, movementEnum.get(j).getMove());
                }
            }

            problem.evaluate(newSolution);
            problem.evaluateConstraints(newSolution);
            solutionSet.add(newSolution);

        }

        return solutionSet;
    }

    public List<List<MovementEnum>> createPopulationAsListMovementEnum(int amountOfSolutions) {

        List<List<MovementEnum>> population = Lists.newArrayListWithExpectedSize(amountOfSolutions);

        for (int i = 0; i < amountOfSolutions; i++) {

            this.movements = Lists.newArrayList(MovementEnum.values());
            this.count = 0;
            this.grid = createGrid(sequenceLength);
            this.solutionMoves = Lists.newArrayList();

            String lookingAxis = "X+";

            int startPoint = grid.length / 2;

            move(startPoint, startPoint, lookingAxis);

            population.add(solutionMoves);

        }
        return population;
    }

    private boolean move(int x, int y, String lookingAxis) {

        String localLookingAxis = lookingAxis;

        grid[x][y] = count;
        count++;
        if (count == sequenceLength) {
            return true;
        }

        int newX = -1;
        int newY = -1;

        List<java.awt.Point> badPlace = Lists.newArrayList();

        do {
            if (movements.size() == 0) {
                break;
            }
            int index = PseudoRandom.randInt(0, movements.size() - 1);
            MovementEnum movement = movements.remove(index);

            Object[] coordinates = fromMoveCodeToCoordinates(lookingAxis, movement, x, y);

            int xMove = (int) coordinates[0];
            int yMove = (int) coordinates[1];

            lookingAxis = (String) coordinates[2];

            newX = xMove;
            newY = yMove;

            if (newX < 0 || newY < 0 || newX >= grid.length || newY >= grid.length) {
                lookingAxis = localLookingAxis;
                continue;
            }
            if (!isEmpty(newX, newY)) {
                lookingAxis = localLookingAxis;
                continue;
            }

            if (badPlace.contains(new Point(newX, newY))) {
                lookingAxis = localLookingAxis;
                continue;
            }

            movements = Lists.newArrayList(MovementEnum.values());
            // It was possible to set point
            if (move(newX, newY, lookingAxis)) {

                solutionMoves.add(movement);
                badPlace = Lists.newArrayList();

                return true;
            }

            badPlace.add(new java.awt.Point(newX, newY));

        } while (!movements.isEmpty());

        lookingAxis = localLookingAxis;

        badPlace = Lists.newArrayList();
        movements = Lists.newArrayList(MovementEnum.values());

        grid[x][y] = -1;
        count--;

        return false;

    }

    public boolean isEmpty(int x, int y) {

        int i = grid[x][y];
        return i == -1 ? true : false;
    }

    public static Object[] fromMoveCodeToCoordinates(String lookingAxis, MovementEnum movement, int x, int y) {

        int DISTANCE = 1;

        String newLookingToAxis = lookingAxis;

        switch (movement) {
            case F: {
                newLookingToAxis = lookingAxis;
                switch (lookingAxis) {
                    case "Y+": {
                        y = y + DISTANCE;
                        break;
                    }
                    case "Y-": {
                        y = y - DISTANCE;
                        break;
                    }
                    case "X+": {
                        x = x + DISTANCE;
                        break;
                    }
                    case "X-": {
                        x = x - DISTANCE;
                        break;
                    }
                }
            }
                break;
            case L: {
                switch (lookingAxis) {
                    case "Y+":
                        newLookingToAxis = "X-";
                        x = x - DISTANCE;
                        break;
                    case "Y-":
                        newLookingToAxis = "X+";
                        x = x + DISTANCE;
                        break;
                    case "X+":
                        newLookingToAxis = "Y+";
                        y = y + DISTANCE;
                        break;
                    case "X-":
                        newLookingToAxis = "Y-";
                        y = y - DISTANCE;
                        break;
                }
            }
                break;
            case R: {
                switch (lookingAxis) {
                    case "Y+":
                        newLookingToAxis = "X+";
                        x = x + DISTANCE;
                        break;
                    case "Y-":
                        newLookingToAxis = "X-";
                        x = x - DISTANCE;
                        break;
                    case "X+":
                        newLookingToAxis = "Y-";
                        y = y - DISTANCE;
                        break;
                    case "X-":
                        newLookingToAxis = "Y+";
                        y = y + DISTANCE;
                        break;
                }
            }

        }
        lookingAxis = newLookingToAxis;

        Object[] array = new Object[] { x, y, lookingAxis };
        return array;

    }

    public int[][] createGrid(int size) {

        int[][] grid = new int[size][size];

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                grid[x][y] = -1;
            }
        }
        return grid;

    }

    public void printGrid() {

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                System.out.print("  " + grid[x][y] + "  ");
            }
            System.out.println();
        }

    }

    public static void main(String[] args) throws ClassNotFoundException {

        String aminoAcidSequence = "HHPPHHHPHHPHPHHHPPHHHPHHPHPH";

        PSPProblem pspProblem = new PSPProblem(aminoAcidSequence, 2);
        BacktrackInitialization backtrackInitialization = new BacktrackInitialization(pspProblem, aminoAcidSequence);

        SolutionSet population = backtrackInitialization.createPopulationAsIntegerSolution(1000);

        for (int i = 0; i < population.size(); i++) {

            XInt vars = new XInt(population.get(i));
            int[] moves = new int[pspProblem.getNumberOfVariables()];
            for (int j = 0; j < pspProblem.getNumberOfVariables(); j++) {
                moves[j] = (int) vars.getValue(j);
            }

            Controller controller = new Controller();
            List<Residue> residues = controller.parseInput(aminoAcidSequence, moves);
            Grid grid = controller.generateGrid(residues);

            int collisionsCount = ResidueUtils.getCollisionsCount(residues);
            int topologicalContacts = ResidueUtils.getTopologyContacts(residues, grid).size();

            System.out.println(
                collisionsCount + ": " + Arrays.toString(moves).replace("[", "").replace("]", "").replace(" ", ""));
        }

    }

}
