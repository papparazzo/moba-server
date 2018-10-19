/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2018 Stefan Paproth <pappi-@gmx.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package tracklayout;

public class Symbol {
    protected static int UNSET        = 0;

    protected static int TOP          = 1;
    protected static int TOP_RIGHT    = 2;
    protected static int RIGHT        = 4;
    protected static int BOTTOM_RIGHT = 8;

    protected static int BOTTOM       = 16;
    protected static int BOTTOM_LEFT  = 32;
    protected static int LEFT         = 64;
    protected static int TOP_LEFT     = 128;

    protected int symbol = 0;

    public boolean isSymbol() {
        return symbol != 0;
    }

    public byte rotate(int s) {
        if((s & 0x80) == s) {
            return (byte)((s << 1) | 0x01);
        }
        return (byte)(s << 0x01);
    }

    public boolean check(int i, int b) {
        while(i-- > 0) {
            if(symbol == b) {
                return true;
            }
            b = rotate(b);
        }
        return false;
    }

    public boolean isEnd() {
        return check(8, TOP);
    }

    public boolean isStraight() {
        return check(4, TOP | BOTTOM);
    }

    public boolean isCrossOver() {
        return check(2, TOP | BOTTOM | RIGHT | LEFT);
    }

    public boolean isBend() {
        return check(8, RIGHT | BOTTOM_LEFT);
    }

    public boolean isTrack() {
        if(isStraight()) {
            return true;
        }
        if(isCrossOver()) {
            return true;
        }
        if(isBend()) {
            return true;
        }
        if(isEnd()) {
            return true;
        }
        return false;
    }

    public boolean isCrossOverSwitch() {
        return check(4, RIGHT | LEFT | TOP_RIGHT | BOTTOM_LEFT);
    }

    public boolean isLeftSwitch() {
        return check(8, RIGHT | LEFT | TOP_RIGHT);
    }

    public boolean isRightSwitch() {
        return check(8, RIGHT | LEFT | BOTTOM_RIGHT);
    }

    public boolean isJunktionSwitch() {
        if(isRightSwitch()) {
            return true;
        }
        if(isLeftSwitch()) {
            return true;
        }
        return false;
    }

    public boolean isThreeWaySwitch() {
        return check(8, TOP | BOTTOM | TOP_RIGHT | TOP_LEFT);
    }

    public boolean isSwitch() {
        if(isCrossOverSwitch()) {
            return true;
        }
        if(isJunktionSwitch()) {
            return true;
        }
        if(isThreeWaySwitch()) {
            return true;
        }
        return false;
    }

    public boolean isValidSymbol() {
        if(isTrack()) {
            return true;
        }
        if(isSwitch()) {
            return true;
        }
        return false;
    }

    public int getJunktionsCount() {
        int c = 0;
        int b = TOP;
        for(int i = 0; i < 8; ++i) {
            if((symbol & b) > 0) {
                ++c;
            }
            b = rotate(b);
        }
        return c;
    }

    public int getNextJunktion(int start) {
        int b = start;
        while(b > 0) {
            if((symbol & b) > 0) {
                return b;
            }
            b <<= 1;
        }
        return UNSET;
    }



    /**
    public boolean hasOpenJunctionsLeft() {
        return t > 0;
    }

    void reset() {
        t = symbol;
    }

    public boolean isJunctionSet(int d) {
        return (symbol & d) > 0;
    }






    void removeJunktion(Direction curDir, int &symb) {
         Direction comDir = getComplementaryDirection(curDir);

         if(!(symb & comDir)) {
             throw std::exception();
         }
         symb |= ~comDir;
    }



    /*
    Direction getComplementaryDirection(Direction dir) {
         switch(dir) {
             case TOP:
                 return BOTTOM;

             case TOP_RIGHT:
                 return BOTTOM_LEFT;

             case RIGHT:
                 return LEFT;

             case BOTTOM_RIGHT:
                 return TOP_LEFT;

         case BOTTOM:
             return TOP;

         case BOTTOM_LEFT:
             return TOP_RIGHT;

         case LEFT:
             return RIGHT;

         case TOP_LEFT:
             return BOTTOM_RIGHT;
     }
}

void setNextPosition(Position &cur, Direction dir) {
     switch(dir) {
         case TOP_RIGHT:
             cur.x++;

         case TOP:
             cur.y--;
             return;

         case BOTTOM_RIGHT:
             cur.y++;

         case RIGHT:
             cur.x++;
             return;

         case BOTTOM_LEFT:
             cur.x--;

         case BOTTOM:
             cur.y++;
             return;

         case TOP_LEFT:
             cur.y--;

         case LEFT:
             cur.x--;
             return;
     }
}



     */

}
