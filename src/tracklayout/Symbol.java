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

import tracklayout.overlay.OverlayI;
import datatypes.base.Direction;

public class Symbol {
    protected int symbolFix = 0; // Symbol mit festen Verbindungen
    protected int symbolDyn = 0; // Symbol mit dynamischen Verbindungen

    protected Actuation actuation1 = null; // Weichenantrieb 1
    protected Actuation actuation2 = null; // Weichenantrieb 2

    protected OverlayI overlay = null;

    public Symbol() {

    }

    public Symbol(int symbol, OverlayI overlay) {
        this(symbol, null, null);
        this.overlay = overlay;
    }

    public Symbol(int symbol) {
        this(symbol, null, null);
    }

    public Symbol(int symbol, Actuation actuationI) {
        this(symbol, actuationI, null);
    }

    public Symbol(int symbol, Actuation actuationI, Actuation actuationII) {
        symbolFix = symbol;
        symbolDyn = symbol;

        if(isSymbol() && !isValidSymbol()) {
            throw new IllegalArgumentException("invalid symbol given");
        }

        if(actuationI != null && actuationII != null && !(isCrossOverSwitch() || isThreeWaySwitch())) {
            throw new IllegalArgumentException("symbol has no actuations set");
        }

        if(actuationI == null && actuationII == null && !isTrack()) {
            throw new IllegalArgumentException("symbol is swtich without actuations");
        }

        if(actuationI == null && actuationII != null) {
            throw new IllegalArgumentException("actuationI not set");
        }

        if(actuationI != null && actuationII == null && !isJunktionSwitch()) {
            throw new IllegalArgumentException("symbol is swtich without actuations");
        }

        actuation1 = actuationI;
        actuation2 = actuationII;
    }

    public OverlayI getOverlay() {
        return overlay;
    }

    public final boolean isSymbol() {
        if(symbolFix != Direction.UNSET) {
            return true;
        }
        return false;
    }

    public boolean isStartSymbol() {
        if((symbolFix & Direction.LEFT) == Direction.LEFT) {
            return false;
        }
        if((symbolFix & Direction.TOP_LEFT) == Direction.TOP_LEFT) {
            return false;
        }
        if((symbolFix & Direction.TOP) == Direction.TOP) {
            return false;
        }
        if((symbolFix & Direction.TOP_RIGHT) == Direction.TOP_RIGHT) {
            return false;
        }
        return true;
    }

    public boolean check(int i, int b) {
        while(i-- > 0) {
            if(symbolFix == b) {
                return true;
            }
            b = rotate(b);
        }
        return false;
    }

    public boolean isEnd() {
        return check(8, Direction.TOP);
    }

    public boolean isStraight() {
        return check(4, Direction.TOP | Direction.BOTTOM);
    }

    public boolean isCrossOver() {
        return check(2, Direction.TOP | Direction.BOTTOM | Direction.RIGHT | Direction.LEFT);
    }

    public boolean isBend() {
        return check(8, Direction.RIGHT | Direction.BOTTOM_LEFT);
    }

    public final boolean isTrack() {
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

    public final boolean isCrossOverSwitch() {
        return check(4, Direction.RIGHT | Direction.LEFT | Direction.TOP_RIGHT | Direction.BOTTOM_LEFT);
    }

    public boolean isLeftSwitch() {
        return check(8, Direction.RIGHT | Direction.LEFT | Direction.TOP_RIGHT);
    }

    public boolean isRightSwitch() {
        return check(8, Direction.RIGHT | Direction.LEFT | Direction.BOTTOM_RIGHT);
    }

    public final boolean isJunktionSwitch() {
        if(isRightSwitch()) {
            return true;
        }
        if(isLeftSwitch()) {
            return true;
        }
        return false;
    }

    public final boolean isThreeWaySwitch() {
        return check(8, Direction.TOP | Direction.BOTTOM | Direction.TOP_RIGHT | Direction.TOP_LEFT);
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

    public final boolean isValidSymbol() {
        if(isTrack()) {
            return true;
        }
        if(isSwitch()) {
            return true;
        }
        return false;
    }

    /*
    public int getJunktionsCount() {
        return countJunktions(symbolFix);
    }
    */
    public int getOpenJunktionsCount() {
        return countJunktions(symbolDyn);
    }

    /*
    public Direction getNextJunktion(Direction start) {
        return nextJunktion(symbolFix, start);
    }
    */
    public boolean hasOpenJunctionsLeft() {
        return symbolDyn > 0;
    }

    public Direction getNextOpenJunktion() {
        return getNextOpenJunktion(new Direction(Direction.TOP));
    }

    public Direction getNextOpenJunktion(Direction start) {
        return nextJunktion(symbolDyn, start);
    }

    public void reset() {
        symbolDyn = symbolFix;
    }

    public boolean isJunctionSet(Direction dir) {
        int i = dir.getDirection();
        return (symbolDyn & i) == i;
    }

    public boolean isOpenJunctionSet(Direction dir) {
        int i = dir.getDirection();
        return (symbolFix & i) == i;
    }

    void removeJunktion(Direction dir) {
        if((symbolDyn & dir.getDirection()) == Direction.UNSET) {
            throw new IndexOutOfBoundsException("junction not set");
        }
        symbolDyn &= ~dir.getDirection();
    }

    public int rotate(int symbol) {
        if((symbol & 0x80) == 0x80) { // if last bit (Most significant bit) is set rotate it to bit 0
            return (symbol << 1) | 0x1;
        }
        return symbol << 1;
    }

    public int countJunktions(int symbol) {
        int counter = 0;
        int b = Direction.TOP;
        for(int i = 0; i < 8; ++i) {
            if((symbol & b) == b) {
                ++counter;
            }
            b = rotate(b);
        }
        return counter;
    }

    public Direction nextJunktion(int symbol, Direction start) {
        int b = start.getDirection();
        for(int i = 0; i < 8; ++i) {
            if((symbol & b) == b) {
                return new Direction(b);
            }
            b = rotate(b);
        }
        // FIXME: Exception???
        return new Direction();
    }
}
