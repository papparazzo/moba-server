/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.routing;

import moba.server.routing.nodes.Direction;

public class Symbol {

    protected int symbolFix;
    protected int symbolDyn;

    public Symbol(int symbol) {
        this.symbolFix = symbol;
        this.symbolDyn = symbol;

        if(isSymbol() && !isValidSymbol()) {
            throw new IllegalArgumentException("Invalid symbol given");
        }
    }

    public boolean hasOpenJunctionsLeft() {
        return symbolDyn != 0;
    }

    public boolean isSymbol() {
        return symbolFix != 0;
    }

    public boolean isValidSymbol() {
        if(isTrack()) {
            return true;
        }
        if(isSwitch()) {
            return true;
        }
        if(isCrossOver()) {
            return true;
        }
        return isEnd();
    }

    public boolean isTrack() {
        return isStraight() || isBend();
    }

    public boolean isStraight() {
        return check(4, SymbolType.STRAIGHT.getValue());
    }

    public boolean isCrossOver() {
        return check(2, SymbolType.CROSS_OVER.getValue());
    }

    public boolean isBend() {
        return check(8, SymbolType.BEND.getValue());
    }

    public boolean isEnd() {
        return check(8, SymbolType.END.getValue());
    }

    public boolean isSwitch() {
        if(isCrossOverSwitch()) {
            return true;
        }
        if(isSimpleSwitch()) {
            return true;
        }
        return isThreeWaySwitch();
    }

    public boolean isCrossOverSwitch() {
        return check(4, SymbolType.CROSS_OVER_SWITCH.getValue());
    }

    public boolean isSimpleSwitch() {
        return isRightSwitch() || isLeftSwitch();
    }

    public boolean isLeftSwitch() {
        return check(8, SymbolType.LEFT_SWITCH.getValue());
    }

    public boolean isRightSwitch() {
        return check(8, SymbolType.RIGHT_SWITCH.getValue());
    }

    public boolean isThreeWaySwitch() {
        return check(8, SymbolType.THREE_WAY_SWITCH.getValue());
    }

    public int getNextJunction() {
        return getNextJunction(Direction.TOP_LEFT);
    }

    public int getNextJunction(int start) {
        return nextJunction(symbolFix, start);
    }

    public int getNextOpenJunction(int start) {
        return nextJunction(symbolDyn, start);
    }

    public int getDistance(Symbol symbol) {
        int x = symbol.symbolFix;

        for(int i = 0; i < 8; ++i) {
            if(symbolFix == x) {
                return i;
            }
            x = rotateRight(x);
        }
        throw new IllegalArgumentException("given symbol does not match");
    }

    public boolean isJunctionSet(int dir) {
        return ((symbolDyn & dir) == dir);
    }

    public void removeJunction(int dir) {
        if(!isJunctionSet(dir)) {
            throw new IllegalArgumentException("given junction is not set");
        }

        symbolDyn &= ~dir;
    }

    private boolean check(int i, int t) {
        while(i-- != 0) {
            if(symbolFix == t) {
                return true;
            }
            t = rotateRight(t);
        }
        return false;
    }

    private int nextJunction(int symbol, int start) {
        for(byte i = 0; i < 8; ++i) {
            start = rotateRight(start);
            if((symbol & start) == start) {
                return start;
            }
        }
        return Direction.UNSET;
//        throw new IllegalArgumentException("no next junction found");
    }

    private int rotateRight(int symbol) {
        return ((symbol << 1) | (symbol >> 7)) & 255;
    }
}

