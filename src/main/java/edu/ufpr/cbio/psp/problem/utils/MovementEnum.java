/*
 * Copyright 2015, Charter Communications, All rights reserved.
 */
package edu.ufpr.cbio.psp.problem.utils;

import org.uma.jmetal.util.JMetalException;

/**
 *
 *
 * @author Vidal
 */
public enum MovementEnum {

    F(1), L(2), R(0);

    private final int move;
    private static final String UNSUPPORTED_MOVEMENT_MSG = "The % movement is unsupported";

    MovementEnum(int move) {
        this.move = move;

    }

    public int getMove() {

        return move;
    }

    public static MovementEnum getMoveFromInteger(int move) {

        switch (move) {
            case 1:
                return F;
            case 0:
                return R;
            case 2:
                return L;

        }
        throw new JMetalException(String.format(UNSUPPORTED_MOVEMENT_MSG, move));
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {

        return "MovementEnum." + this.name();
    }

}
