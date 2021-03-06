package edu.ufpr.cbio.psp.problem.utils;

public enum EnumMovements {

    ROTATE_90_CLOCKWISE(1), ROTATE_180_CLOCKWISE(2), CORNER(3), CRANKSHAFT(4);

    private int id;

    private EnumMovements(int id) {

        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {

        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {

        this.id = id;
    }

}
