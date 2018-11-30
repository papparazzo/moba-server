
package datatypes.base;

public class Direction {
    public static final int UNSET        = 0;

    public static final int TOP          = 1;
    public static final int TOP_RIGHT    = 2;
    public static final int RIGHT        = 4;
    public static final int BOTTOM_RIGHT = 8;

    public static final int BOTTOM       = 16;
    public static final int BOTTOM_LEFT  = 32;
    public static final int LEFT         = 64;
    public static final int TOP_LEFT     = 128;

    protected int direction;

    public Direction(int dir) throws IllegalArgumentException {
        switch(dir) {
            case TOP:
            case TOP_RIGHT:
            case RIGHT:
            case BOTTOM:
            case BOTTOM_LEFT:
            case LEFT:
            case TOP_LEFT:
                direction = dir;
                return;
        }
        throw new IllegalArgumentException();
    }

    public Direction getNextLeftDirection(Direction dir) {
        if(dir.direction == TOP) {
            return new Direction(TOP_LEFT);
        }
        return new Direction(dir.direction / 2);
    }

    public Direction getNextRightDirection(Direction dir) {
        if(dir.direction == TOP_LEFT) {
            return new Direction(TOP);
        }
        return new Direction(dir.direction * 2);
    }

    public Direction getComplementaryDirection(Direction dir) {
        if(dir.direction == UNSET) {
            return new Direction(UNSET);
        }
        if(dir.direction < BOTTOM) {
            return new Direction(dir.direction * 16);
        }
        return new Direction(dir.direction / 16);
    }

    public enum DistanceType {
        INVALID,
        STRAIGHT,
        BEND
    };

    /**
    * Die Distanz zwischen zwei Verbindungspunkte muss mindestens 3 Bit betragen, damit
    * zwei 2 Verbindungspunkte (auch als Teil einer Weiche) ein gültiges Gleis bilden.
    * Zu einem Verbindungspunkt dir1 kommen nur 3 mögliche Verbindungspunkte dir2 in Frage:
    * 1. Der komplemntäre Verbindungspunkt (also ein gerades Gleis)
    * 2. Der komplemntäre Verbindungspunkt + 1 Bit (also gebogenes Gleis)
    * 3. Der komplemntäre Verbindungspunkt - 1 Bit (also gebogenes Gleis)
    *
    * @param dir1
    * @param dir2
    * @return DistanceType
    */
    public DistanceType getDistanceType(Direction dir1, Direction dir2) {
        if(dir1 == dir2) {
            return DistanceType.INVALID;
        }

        Direction dirc = getComplementaryDirection(dir1);

        if(dir2.direction == dirc.direction) {
            return DistanceType.STRAIGHT;
        }

        if(dir2.direction == (dirc.direction * 2)) {
            return DistanceType.BEND;
        }

        // Sonderfall: TOP == 1 -> 1 / 2 = 0 -> müsste hier jedoch 128 sein!!
        if(dirc.direction == TOP && dir2.direction == TOP_LEFT) {
            return DistanceType.BEND;
        }

        if(dir2.direction == (dirc.direction / 2)) {
            return DistanceType.BEND;
        }

        return DistanceType.INVALID;
    }
}
