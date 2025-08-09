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

    protected byte symbolFix;
    protected byte symbolDyn;

    public Symbol(byte symbol) {
        this.symbolFix = symbol;
        this.symbolDyn = symbol;

        if(isSymbol() && !isValidSymbol()) {
            throw new IllegalArgumentException("Invalid symbol given");
        }
    }

    public boolean isSymbol() {
        return symbolFix != 0;
    }

    byte getType() {
        return symbolFix;
    }

    public void rotateLeft() {
        rotateLeft((byte) 1);
    }

    public void rotateLeft(byte count) {
        count &= 7;
        symbolFix = (byte)((symbolFix >> count) | (symbolFix << (-count & 7)));
        symbolDyn = (byte)((symbolDyn >> count) | (symbolDyn << (-count & 7)));
    }

    public void rotateRight() {
        rotateLeft((byte) 1);
    }

    public void rotateRight(byte count) {
        count &= 7;
        symbolFix = (byte)((symbolFix << count) | (symbolFix >> (-count & 7)));
        symbolDyn = (byte)((symbolDyn << count) | (symbolDyn >> (-count & 7)));
    }

    public int getDistance(Symbol symbol) {
        for(int i = 0; i < 8; ++i) {
            if(symbolFix == symbol.symbolFix) {
                return i;
            }
            symbol.rotateRight((byte) 1);
        }
        throw new IllegalArgumentException("given symbol does not match");
    }

    boolean isStartSymbol() {
        if((symbolFix & Direction.LEFT.getDirection()) == 1) {
            return true;
        }
        if((symbolFix & Direction.TOP_LEFT.getDirection()) == 1) {
            return true;
        }
        if((symbolFix & Direction.TOP.getDirection()) == 1) {
            return true;
        }
        return (symbolFix & Direction.TOP_RIGHT.getDirection()) == 1;
    }

    public boolean isValidSymbol() {
        return isTrack() || isSwitch();
    }

    public byte getSymbolFix() {
        return symbolFix;
    }

    public byte getSymbolDyn() {
        return symbolDyn;
    }

    public void setSymbolDyn(byte symbol) {
        this.symbolDyn = symbol;
    }

    public boolean check(byte i, byte b) {
        while(i-- != 0) {
            if(symbolFix == b) {
                return true;
            }
            b = (byte)((b << 1) | (b >> 7));
        }
        return false;
    }

    public boolean check(Symbol symbol) {
        return check((byte)8, symbol.symbolFix);
    }

    public boolean isEnd() {
        return check((byte)8, (byte)SymbolType.END.getWeight());
    }
    
    public boolean isStraight() {
        return check((byte)4, (byte)SymbolType.STRAIGHT.getWeight());
    }
    
    public boolean isCrossOver() {
        return check((byte)2, (byte)SymbolType.CROSS_OVER.getWeight());
    }
    
    public boolean isBend() {
        return check((byte)8, (byte)SymbolType.BEND.getWeight());
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
        return isEnd();
    }

    public boolean isCrossOverSwitch() {
        return check((byte)4, (byte)SymbolType.CROSS_OVER_SWITCH.getWeight());
    }
    
    public boolean isLeftSwitch() {
        return check((byte)8, (byte)SymbolType.LEFT_SWITCH.getWeight());
    }
    
    public boolean isRightSwitch() {
        return check((byte)8, (byte)SymbolType.RIGHT_SWITCH.getWeight());
    }

    public boolean isSimpleSwitch() {
        if(isRightSwitch()) {
            return true;
        }
        if(isLeftSwitch()) {
            return true;
        }
        return false;
    }

    public boolean isThreeWaySwitch() {
        return check((byte)8, (byte)SymbolType.THREE_WAY_SWITCH.getWeight());
    }

    public boolean isSwitch() {
        if(isCrossOverSwitch()) {
            return true;
        }
        if(isSimpleSwitch()) {
            return true;
        }
        if(isThreeWaySwitch()) {
            return true;
        }
        return false;
    }

    public byte getJunctionsCount() {
        return countJunctions(symbolFix);
    }

    public byte getOpenJunctionsCount() {
        return countJunctions(symbolDyn);
    }

    public Direction getNextJunction() {
        return getNextJunction(Direction.TOP_LEFT);
    }

    public Direction getNextJunction(Direction start) {
        return nextJunction(symbolFix, start);
    }

    public int getNextOpenJunction() {
        return getNextOpenJunction(Direction.TOP_LEFT);
    }

    public Direction getNextOpenJunction(Direction start) {
        return nextJunction(symbolDyn, start);
    }

    public Direction getNextOpenJunction() {
        return getNextOpenJunction(Direction.TOP_LEFT);
    }

    void reset() {
        symbolDyn = symbolFix;
    }

    public boolean isJunctionSet(Direction dir) {
        return ((symbolDyn & (byte)(dir.getDirection())) != 0);
    }

    public boolean areJunctionsSet(byte junctions) {
        return (junctions == (symbolDyn & junctions));
    }

    public boolean isJunctionSet(int dir) {
        return ((symbolDyn & dir) == dir);
    }

    public boolean removeJunction(int dir) {
        if(!isJunctionSet(dir)) {
            return false;
        }
        byte x = (byte)(dir.getDirection());

        symbolDyn &= ~x;
        return true;
    }

    byte countJunctions(byte symbol) {
        byte counter = 0;
        byte b = (byte)(Direction.TOP.getDirection());
        for(byte i = 0; i < 8; ++i) {
            if((symbol & b) == b) {
                ++counter;
            }
            b <<= 1;
        }
        return counter;
    }

    Direction nextJunction(byte symbol, Direction start) {
        byte b = (byte)start.getDirection();
        for(byte i = 0; i < 8; ++i) {
            b = (byte)((b << 1) | (b >> 7));
            if((symbol & b) == b) {
                return Direction.fromId(b);
            }
        }
        return Direction.UNSET;
    }
}

