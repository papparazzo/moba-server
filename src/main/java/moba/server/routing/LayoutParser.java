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

import moba.server.datatypes.collections.BlockContactDataMap;
import moba.server.datatypes.collections.SwitchStateMap;
import moba.server.datatypes.enumerations.SwitchStand;
import moba.server.datatypes.objects.Position;
import moba.server.datatypes.objects.TrackLayoutSymbolData;
import moba.server.routing.nodes.*;
import moba.server.routing.typedefs.*;

public class LayoutParser {
    // IN
    private final LayoutContainer layout;

    private final BlockContactDataMap blockContacts;

    private final SwitchStateMap switchStates;

    // OUT
    private final SwitchNodeMap switcheNodeMap = new SwitchNodeMap();

    private final BlockNodeMap blockNodeMap = new BlockNodeMap();

    // INTERN
    private final NodeJunctionsMap nodes = new NodeJunctionsMap();

    public LayoutParser(
        LayoutContainer layout,
        BlockContactDataMap blockContacts,
        SwitchStateMap switchStates
    ) {
        this.layout = layout;
        this.blockContacts = blockContacts;
        this.switchStates = switchStates;
        if(blockContacts.isEmpty()) {
            throw new LayoutParserException("no blocks found");
        }
    }

    public SwitchNodeMap getSwitchMap() {
        return switcheNodeMap;
    }

    public BlockNodeMap getBlockMap() {
        return blockNodeMap;
    }

    public void parse() {

        // Position des ersten Blockkontaktes
        long id = blockContacts.entrySet().iterator().next().getKey();

        Position startPos  = getPositionFromId(id);
        Symbol   curSymbol = layout.get(startPos).symbol();

        int dir1 = curSymbol.getNextJunction();
        fetchBlockNodes(new LocationVector(startPos, dir1));

        int dir2 = curSymbol.getNextJunction(dir1);
        fetchBlockNodes(new LocationVector(startPos, dir2));
    }

    private Position getPositionFromId(long id) {
        for(var entry : layout.entrySet()) {
            if(entry.getValue().id() == id) {
                return entry.getKey();
            }
        }
        throw new LayoutParserException("no position found for id " + id);
    }

    private void fetchBlockNodes(LocationVector startPos) {

        LocationVector endPos = getNextNodePosition(new LocationVector(startPos));

        if(endPos == null) {
            return;
        }

        NodeJunction start = getNodeJunction(startPos.getPosition());
        NodeJunction end   = getNodeJunction(endPos.getPosition());

        start.setCounterpartNode(startPos.getDirection(), end.node());
        end.setCounterpartNode(Direction.getComplementaryDirection(endPos.getDirection()), start.node());

        Symbol startSymbol = layout.get(startPos.getPosition()).symbol();
        Symbol endSymbol = layout.get(endPos.getPosition()).symbol();

        startSymbol.removeJunction(startPos.getDirection());
        endSymbol.removeJunction(Direction.getComplementaryDirection(endPos.getDirection()));

        int dir = Direction.TOP;

        while((dir = endSymbol.getNextOpenJunction(dir)) != Direction.UNSET) {
            fetchBlockNodes(new LocationVector(endPos.getPosition(), dir));
        }
    }

    private LocationVector getNextNodePosition(LocationVector pos) {
        while(true) {
            // Schritt weiter zum nächsten Symbol
            pos.step();

            // Symbol von der aktuellen Position im Gleisplan
            TrackLayoutSymbolData curSymbolData = layout.get(pos.getPosition());
            Symbol curSymbol = curSymbolData.symbol();


            // Prüfen, ob das Symbol eine Weiche oder ein Block ist
            if(blockContacts.containsKey(curSymbolData.id()) || switchStates.containsKey(curSymbolData.id())) {
                if(curSymbol.hasOpenJunctionsLeft()) {
                   return pos;
                }
                return null;
            }

            // Ist das Symbol ein Prellbock?
            if(curSymbol.isEnd()) {
                return null;
            }

            if(curSymbol.isBend()) {
                // Wenn das Symbol eine Kurve darstellt, ist eine neue Richtung zu bestimmen.
                var curDir = pos.getDirection();
                curDir = Direction.getComplementaryDirection(curDir);
                curDir = curSymbol.getNextOpenJunction(curDir);
                pos.setDirection(curDir);
            }
        }
    }

    private NodeJunction getNodeJunction(Position curPos) {

        if(nodes.containsKey(curPos)) {
            return nodes.get(curPos);
        }

        TrackLayoutSymbolData curSymbolData = layout.get(curPos);
        Symbol                curSymbol   = curSymbolData.symbol();
        long                  id          = curSymbolData.id();
        SwitchStand           switchState = switchStates.get(id);

        // Ein Knoten existiert hier noch nicht, neu erzeugen …
        NodeInterface newNode;
        Symbol newSymbol;

        // … aktueller Knoten ist eine Weiche
        if(curSymbol.isLeftSwitch()) {
            newSymbol = new Symbol(SymbolType.LEFT_SWITCH.getValue());
            newNode = new SwitchNode(id, switchState);
            switcheNodeMap.put(id, newNode);
        } else if(curSymbol.isRightSwitch()) {
            newSymbol = new Symbol(SymbolType.RIGHT_SWITCH.getValue());
            newNode = new SwitchNode(id, switchState);
            switcheNodeMap.put(id, newNode);
        } else if(curSymbol.isTrack()){
            newSymbol = new Symbol(SymbolType.STRAIGHT.getValue());
            newNode = new BlockNode(id);
            blockNodeMap.put(id, (BlockNode)newNode);
        } else {
            throw new NodeException("Invalid symbol found!");
        }

        int offset = curSymbol.getDistance(newSymbol);

        NodeJunction junction = new NodeJunction(newNode, offset);
        nodes.put(new Position(curPos), junction);
        return junction;
    }
}
