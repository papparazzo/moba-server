package moba.server.routing;

import moba.server.routing.nodes.Direction;

import java.util.Objects;

final public class Position {

    private long x;
    private long y;

    public Position(long x, long y) {
        if(x < 0 || y < 0) {
            throw new IllegalArgumentException("Position must be >= 0");
        }

        this.x = x;
        this.y = y;
    }

    public Position(Position pos) {
        this(pos.x, pos.y);
    }

    public Position() {
        this(0, 0);
    }

    public Position(Direction dir) {
        this(0, 0);
    }

    @Override
    public String toString() {
        return "Position{x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public Position getDistance(Position pos) {
        return new Position(pos.x - x, pos.y - y);
    }

    public Position grow(Position pos) {
        x = Math.max(pos.x, x);
        y = Math.max(pos.y, y);
        return new Position(x, y);
    }

    /**
     * setzt den Cursor (Position) in die Richtung, welche mit Direction
     * angegeben ist. Beispiel: Direction RIGHT → x einen weiter nach rechts
     */
    public void setNewPosition(Direction dir) {
        switch(dir) {
            case UNSET:
                return;

            case TOP_RIGHT:
                x++;  // fall-through

            case TOP:
                y--;
                break;

            case BOTTOM_RIGHT:
                y++; // fall-through

            case RIGHT:
                x++;
                return;

            case BOTTOM:
                y++;
                return;

            case BOTTOM_LEFT:
                y++;  // fall-through

            case LEFT:
                x--;
                break;

            case TOP_LEFT:
                y--;
                x--;
                break;
        }
    }
}

