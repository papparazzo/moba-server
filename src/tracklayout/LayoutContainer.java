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

import datatypes.base.Position;
import java.util.HashMap;
import java.util.Map;

public class LayoutContainer {
    protected final Map<Position, Symbol> map;

    protected int maxX = 0;
    protected int maxY = 0;

    public LayoutContainer() {
        map = new HashMap<>();
    }

    public Symbol put(int x, int y, Symbol value) {
        return put(new Position(x, y), value);
    }

    public Symbol put(Position pos, Symbol value) {
        if(maxX < pos.getX()) {
            maxX = pos.getX();
        }
        if(maxY < pos.getY()) {
            maxY = pos.getY();
        }
        return map.put(pos, value);
    }

    public Symbol get(Position pos) {
        if(pos.getX() > maxX || pos.getY() > maxY) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if(!map.containsKey(pos)) {
            return null;
        }
        return map.get(pos);
    }

    public Symbol get(int x, int y) {
        return get(new Position(x, y));
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(int x, int y) {
        return containsKey(new Position(x, y));
    }

    public boolean containsKey(Position pos) {
        return map.containsKey(pos);
    }

    public Position getNextFreePosition() {
        return getNextFreePosition(new Position());
    }

    public Position getNextFreePosition(Position pos) {
        for(int y = pos.getY(); y <= maxY; ++y) {
            for(int x = pos.getX(); x <= maxX; ++x) {
                Position tmp = new Position(x, y);
                if(!map.containsKey(tmp)) {
                    continue;
                }
                Symbol symbol = map.get(pos);
                if(symbol.isSymbol()) {
                    continue;
                }
                return tmp;
            }
        }
        throw new ArrayIndexOutOfBoundsException("No position found!");
    }

    public Position getNextBoundPosition() {
        return getNextBoundPosition(new Position());
    }

    public Position getNextBoundPosition(Position pos) {
        for(int y = pos.getY(); y <= maxY; ++y) {
            for(int x = pos.getX(); x <= maxX; ++x) {
                Position tmp = new Position(x, y);
                if(!map.containsKey(tmp)) {
                    continue;
                }
                Symbol symbol = map.get(pos);
                if(!symbol.isSymbol()) {
                    continue;
                }
                return tmp;
            }
        }
        throw new ArrayIndexOutOfBoundsException("No position found!");
    }
}
