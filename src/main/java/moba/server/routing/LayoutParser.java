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

import moba.server.datatypes.objects.BlockContactData;
import moba.server.datatypes.objects.Position;
import moba.server.routing.nodes.*;
import moba.server.routing.typedefs.*;

final public class LayoutParser {

    // IN
    private final LayoutContainer layout;

    private final BlockContactDataMap blockContacts;

    private final SwitchStandMap switchStates;

    private final TrainList trainList;

    // OUT
    private final SwitchNodeMap switcheNodeMap = new SwitchNodeMap();

    private final BlockNodeMap blockNodeMap = new BlockNodeMap();

    // INTERN
    private final NodeJunctionsMap nodes = new NodeJunctionsMap();

    public LayoutParser(
        LayoutContainer layout,
        BlockContactDataMap blockContacts,
        SwitchStandMap switchStates,
        TrainList trainList
    ) {
        this.layout = layout;
        this.blockContacts = blockContacts;
        this.switchStates = switchStates;
        this.trainList = trainList;
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
        var firstBlockContact = blockContacts.entrySet().iterator().next();

        Position pos = firstBlockContact.getKey();

        var curSymbol = layout.get(pos);
        var dir1 = curSymbol.symbol().getNextJunction();
        var dir2 = curSymbol.symbol().getNextJunction(dir1);

        BlockContactData firstBlockContactData = firstBlockContact.getValue();

        BlockNode block = createBlock(curSymbol.id(), firstBlockContactData);

        blockNodeMap.put(firstBlockContactData.blockContact(), block);


        NodeJunctions junctions = new NodeJunctions(new HashMap<>(), block);
        nodes.put(new Position(pos), junctions);

        junctions.junctions().put(dir1, new NodeCallback(dir1, block));
        junctions.junctions().put(dir2, new NodeCallback(dir2, block));

        curSymbol.symbol().removeJunction(dir1);
        fetchBlockNodes(dir1, pos);

        if(!curSymbol.symbol().isJunctionSet(dir2)) {
            return;
        }

        curSymbol.symbol().removeJunction(dir2);
        fetchBlockNodes(dir2, pos);
    }

    private void fetchBlockNodes(Position curPos, int curDir) {
        // "curPos" ist immer eine Weiche oder ein Block!
        var startDir = curDir;
        var startPos = new Position(curPos);

        System.out.println("=======================");
        System.out.println("fetchBlockNodes: ");
        System.out.println("Pos:       " + curPos);
        System.out.println("Dir:       " + curDir);
        System.out.println("=======================");

        while(true) {
            // Schritt weiter zum nächsten Symbol
            curPos.setNewPosition(curDir);

            // Symbol von der aktuellen Position im Gleisplan
            System.out.println("Pos:       " + curPos);
            LayoutSymbol tmp   = layout.get(curPos);
            Symbol curSymbol   = tmp.symbol();
            int    curSymbolId = tmp.id();

            var compDir = Direction.getComplementaryDirection(curDir);

            curSymbol.removeJunction(compDir);

            // Ist das Symbol ein Prellbock?
            if(curSymbol.isEnd()) {
                return;
            }

            var block = blockContacts.get(curPos);
            var switchState = switchStates.get(curPos);

            // Prüfen, ob das Symbol keine Weiche und kein Block ist
            if(block == null && switchState == null) {
                if(curSymbol.isBend()) {
                    // Wenn das Symbol eine Kurve darstellt, ist eine neue Richtung zu bestimmen.
                    curDir = curSymbol.getNextOpenJunction(curDir);
                }
                curSymbol.removeJunction(curDir);
                continue;
            }

            // Ab hier entweder Weiche oder Block! Startknoten ermitteln...
            NodeJunctions startNode = nodes.get(startPos);

            // Existiert an aktueller Stelle schon ein Knoten?
            NodeJunctions curNode = nodes.get(curPos);
            if(curNode != null) {
                //… wenn ja, diesen mit dem Startknoten verbinden und Funktion verlassen
                startNode.node().setJunctionNode(Direction.shift(startDir, startNode.offset()), curNode.node());
                startNode.node().setJunctionNode(Direction.shift(compDir, startNode.offset()), curNode.node());
                curSymbol.removeJunction(curDir);
                return;
            }

            // Ein Knoten existiert hier noch nicht, neu erzeugen …
            Node newNode;
            Symbol newSymbol;

            if(block != null) {
                // … aktueller Knoten ist ein Block
                var newBlockNode = new BlockNode(curSymbolId);
                newBlockNode.setTrain(trainList.get(block.trainId()));

                newSymbol = new Symbol(curSymbol);
                blockNodeMap.put(block.blockContact(), newBlockNode);
                newNode = newBlockNode;
            } else {
                // … aktueller Knoten ist eine Weiche
                if(curSymbol.isLeftSwitch()) {
                    newSymbol = new Symbol(SymbolType.LEFT_SWITCH.getValue());
                    newNode = new SimpleSwitchNode(curSymbolId, switchState.switchStand());
                } else if(curSymbol.isRightSwitch()) {
                    newSymbol = new Symbol(SymbolType.RIGHT_SWITCH.getValue());
                    newNode = new SimpleSwitchNode(curSymbolId, switchState.switchStand());
                } else if(curSymbol.isCrossOverSwitch()) {
                    newSymbol = new Symbol(SymbolType.CROSS_OVER_SWITCH.getValue());
                    newNode = new CrossOverSwitchNode(curSymbolId, switchState.switchStand());
                } else if(curSymbol.isThreeWaySwitch()) {
                    newSymbol = new Symbol(SymbolType.THREE_WAY_SWITCH.getValue());
                    newNode = new ThreeWaySwitchNode(curSymbolId, switchState.switchStand());
                } else {
                    throw new NodeException("Invalid symbol found!");
                }
                switcheNodeMap.put(switchState.id(), newNode);
            }

            int offset = curSymbol.getDistance(newSymbol);

            NodeJunctions junctions = new NodeJunctions(newNode, offset);
            nodes.put(new Position(curPos), junctions);

            int dir;

            while((dir = newSymbol.getNextOpenJunction()) != Direction.UNSET) {
                newSymbol.removeJunction(dir);
                int shiftedDir = Direction.shift(dir, offset);

                if(shiftedDir == compDir) {
                    junctions.node().setJunctionNode(dir, startNode.node());
                    continue;
                }

                // Prüfen, ob Junction noch existiert. Bei einer Kehrschleife kann
                // diese durch Rekursion bereits gelöscht worden sein
                if(curSymbol.isJunctionSet(shiftedDir)) {
                    curSymbol.removeJunction(shiftedDir);
                    fetchBlockNodes(curPos, shiftedDir);
                }
            }
            return;
        }
    }

    BlockNode createBlock(int id, BlockContactData contact) {
        var iter = trainList.get(contact.trainId());
        var block = new BlockNode(id);

        if(iter != null) {
            block.setTrain(iter);
        }

        return block;
    }
}
