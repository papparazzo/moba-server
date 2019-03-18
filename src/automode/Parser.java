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

package automode;

import automode.node.NodeI;
import datatypes.base.Direction;
import datatypes.base.Position;
import tracklayout.LayoutContainer;
import tracklayout.Symbol;
import tracklayout.overlay.Block;
import tracklayout.overlay.OverlayI;

public class Parser {

    protected LayoutContainer container;

    protected NodeI startNode = null;


    public NodeI parse(LayoutContainer container) throws ParserException {
        this.container = container;

        return startNode;
    }





    protected Position getRealStartPosition() throws ParserException {
        Position start = container.getNextBoundPosition();
        Position pos = start;

        Symbol currSymbol = container.get(pos);

        if(!currSymbol.isStartSymbol()) {
            throw new ParserException("first symbol is not a start symbol");
        }

        while(true) {
            if(currSymbol.isEnd()) {
                return pos;
            }

            if(!currSymbol.isTrack()) {
                return pos;
            }

            OverlayI overlay = currSymbol.getOverlay();
            if(overlay instanceof Block) {
                return pos;
            }

            Direction dir = currSymbol.getNextOpenJunktion();

            // Nächsten Koordinaten der Richtung
            pos.setNewPosition(dir);
            currSymbol = container.get(pos);
            if(currSymbol == null) {
                throw new ParserException("currSymbol == null");
            }
            if(pos.equals(start)) {
                throw new ParserException("back to origin");
            }
        }
    }
}
