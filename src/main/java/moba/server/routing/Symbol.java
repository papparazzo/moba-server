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

       // if(isSymbol() && !isValidSymbol()) {
       //     throw new IllegalArgumentException("Invalid symbol given");
       // }
    }

    public boolean isSymbol() {
        return symbolFix != 0;
    }


    /*



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

    public byte getDistance(Symbol symbol) {
        for(byte i = 0; i < 8; ++i) {
            if(symbolFix == symbol.symbolFix) {
                return i;
            }
            symbol.rotateRight((byte) 1);
        }
        throw new IllegalArgumentException("given symbol does not match");
    }

    boolean isStartSymbol() {
        if((symbolFix & Direction.LEFT.getWeight()) == 1) {
            return true;
        }
        if((symbolFix & Direction.TOP_LEFT.getWeight()) == 1) {
            return true;
        }
        if((symbolFix & Direction.TOP.getWeight()) == 1) {
            return true;
        }
        if((symbolFix & Direction.TOP_RIGHT.getWeight()) == 1) {
            return true;
        }
        return false;
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
        b = (b << 1) | (b >> 7);
    }
    return false;
}




public boolean check(Symbol symbol) {
    return check(8, symbol.symbolFix);
}

public boolean isEnd() {
    return check(8, SymbolType::END);
}

public boolean isStraight() {
    return check(4, SymbolType::STRAIGHT);
}

public boolean isCrossOver() {
    return check(2, SymbolType::CROSS_OVER);
}

public boolean isBend() {
    return check(8, SymbolType::BEND);
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
    return check(4, SymbolType::CROSS_OVER_SWITCH);
}

public boolean isLeftSwitch() {
    return check(8, SymbolType::LEFT_SWITCH);
}

public boolean isRightSwitch() {
    return check(8, SymbolType::RIGHT_SWITCH);
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
    return check(8, SymbolType::THREE_WAY_SWITCH);
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

byte getJunctionsCount() {
    return countJunctions(symbolFix);
}

byte getOpenJunctionsCount() {
    return countJunctions(symbolDyn);
}

Direction getNextJunction() {
    return getNextJunction(Direction.TOP_LEFT);
}

Direction getNextJunction(Direction start) {
    return nextJunction(symbolFix, start);
}

public boolean hasOpenJunctionsLeft() {
    return static_cast<bool>(symbolDyn);
}

Direction getNextOpenJunction(Direction start) {
    return nextJunction(symbolDyn, start);
}

void reset() {
    symbolDyn = symbolFix;
}

public boolean isJunctionSet(Direction dir) {
    return symbolDyn & static_cast<byte>(dir);
}

public boolean areJunctionsSet(byte junctions) {
    return (junctions == (symbolDyn & junctions));
}

public boolean isOpenJunctionSet(Direction dir) {
    return symbolFix & static_cast<byte>(dir);
}

public boolean areOpenJunctionsSet(byte junctions) {
    return (junctions == (symbolFix & junctions));
}

public boolean removeJunction(Direction dir) {
     if(!(symbolDyn & static_cast<byte>(dir))) {
         return false;
     }
     symbolDyn &= ~static_cast<byte>(dir);
     return true;
}

byte countJunctions(byte symbol) {
    byte counter = 0;
    var b = static_cast<byte>(Direction::TOP);
    for(byte i = 0; i < 8; ++i) {
        if(symbol & b) {
            ++counter;
        }
        b <<= 1;
    }
    return counter;
}

Direction nextJunction(byte symbol, Direction start) {
    var b = static_cast<byte>(start);
    for(byte i = 0; i < 8; ++i) {
        b = (b << 1) | (b >> 7);
        if(symbol & b) {
            return static_cast<Direction>(b);
        }
    }
    return Direction::UNSET;
}
//}
//        throw std::invalid_argument("invalid symbol given");
//    }*/
}

