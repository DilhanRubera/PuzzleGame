package edu.curtin.models;

public class Coordinate {

    private int row;
    private int col;

    public Coordinate(int row, int col){
        this.row=row;
        this.col= col;

    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Check if it's the same reference
        if (obj == null || getClass() != obj.getClass()) return false; // Check if it's the same class

        Coordinate other = (Coordinate) obj; // Cast to Coordinate
        return this.row == other.row && this.col == other.col; // Compare row and col values
    }

    // Override hashCode to ensure proper behavior when used as a key in hash-based collections
    @Override
    public int hashCode() {
        return 31 * row + col; // A simple hash combining row and col
    }
}
