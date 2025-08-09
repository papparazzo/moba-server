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

import java.util.HashMap;

public class LayoutParser {

    // IN
    private LayoutContainer layout;

    private BlockContactDataMap blockContacts;

    private SwitchStandMap switchStates;

    private TrainList trainList;

    // OUT
    private final SwitchNodeMap switcheNodeMap = new SwitchNodeMap();

    private final BlockNodeMap blockNodeMap = new BlockNodeMap();

    public SwitchNodeMap getSwitchMap() {
        return switcheNodeMap;
    }

    public BlockNodeMap getBlockMap() {
        return blockNodeMap;
    }

    // INTERN
    private final NodeJunctionsMap nodes = new NodeJunctionsMap();

    public void parse(
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

        var firstBlockContact = blockContacts.entrySet().iterator().next();

        Position pos = firstBlockContact.getKey();

        var curSymbol = layout.get(pos);
        var dir1 = curSymbol.symbol().getNextJunction();
        var dir2 = curSymbol.symbol().getNextJunction(dir1);

        BlockContactData firstBlockContactData = firstBlockContact.getValue();

        BlockNode block = createBlock(curSymbol.id(), firstBlockContactData);

        blockNodeMap.put(firstBlockContactData.blockContact(), block);


        NodeJunctions junctions = new NodeJunctions(new HashMap<>(), block);
        nodes.put(pos, junctions);

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

    public void fetchBlockNodes(int curDir, Position curPos) {
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
            LayoutSymbol curSymbol = layout.get(curPos);

            var compDir = Direction.getComplementaryDirection(curDir);

            curSymbol.symbol().removeJunction(compDir);

            // Ist das Symbo ein Prellbock?
            if(curSymbol.symbol().isEnd()) {
                return;
            }

            var block = blockContacts.get(curPos);
            var switchState = switchStates.get(curPos);

            // Prüfen, ob das Symbol keine Weiche und kein Block ist
            if(block == null && switchState == null) {
                // Wenn das Symbol gerades Gleis oder Kreuzung dann einfach weiter in Richtung
                if(curSymbol.symbol().isBend()) {
                    curDir = curSymbol.symbol().getNextOpenJunction(curDir);
                }
                continue;
            }

            // Ab hier entweder Weiche oder Block! Startknoten ermitteln...
            NodeJunctions startNode = nodes.get(startPos);

            // Existiert an aktueller Stelle schon ein Knoten?
            NodeJunctions curNode = nodes.get(curPos);
            if(curNode != null) {
                //... wenn ja, diesen mit dem Startknoten verbinden und Funktion verlassen
                startNode.junctions().get(startDir).setJunction(curNode.node());
                curNode.junctions().get(compDir).setJunction(startNode.node());
                curSymbol.symbol().removeJunction(curDir);
                return;
            }

            Node newNode;
            Symbol sym;

            if(block != null) {
                // ...aktueller Knoten ist ein Block
                var bNode = createBlock(curSymbol.id(), block);
                sym = curSymbol.symbol();
                sym.reset();
                blockNodeMap.put(block.blockContact(), bNode);
                newNode = bNode;
            } else {
                // ...aktueller Knoten ist eine Weiche
                if(curSymbol.symbol().isLeftSwitch()) {
                    sym = new Symbol((byte)SymbolType.LEFT_SWITCH.getWeight());
                    newNode = new SimpleSwitchNode(curSymbol.id(), switchState.switchStand());
                } else if(curSymbol.symbol().isRightSwitch()) {
                    sym = new Symbol((byte)SymbolType.RIGHT_SWITCH.getWeight());
                    newNode = new SimpleSwitchNode(curSymbol.id(), switchState.switchStand());
                } else if(curSymbol.symbol().isCrossOverSwitch()) {
                    sym = new Symbol((byte)SymbolType.CROSS_OVER_SWITCH.getWeight());
                    newNode = new CrossOverSwitchNode(curSymbol.id(), switchState.switchStand());
                } else if(curSymbol.symbol().isThreeWaySwitch()) {
                    sym = new Symbol((byte)SymbolType.THREE_WAY_SWITCH.getWeight());
                    newNode = new ThreeWaySwitchNode(curSymbol.id(), switchState.switchStand());
                } else {
                    throw new NodeException("");
                }
                switcheNodeMap.put(switchState.id(), newNode);
            }

            curNode = curNode.withNode(newNode);
            int offset = curSymbol.symbol().getDistance(sym);

            startNode.junctions().get(startDir).setJunction(curNode.node());

            int dir;

            while((dir = sym.getNextOpenJunction()) != Direction.UNSET) {
                curNode.junctions().put(Direction.shift(dir, offset), new NodeCallback(dir, newNode));
                sym.removeJunction(dir);
            }

            sym.reset();

            while((dir = sym.getNextOpenJunction()) != Direction.UNSET) {
                sym.removeJunction(dir);
                if(Direction.shift(dir, offset) == compDir) {
                    curNode.junctions().get(compDir).setJunction(startNode.node());
                    continue;
                }

                // Prüfen, ob Junction noch existiert. Bei einer Kehrschleife kann
                // diese durch Rekursion bereits gelöscht worden sein
                if(curSymbol.symbol().removeJunction(Direction.shift(dir, offset))) {
                    fetchBlockNodes(Direction.shift(dir, offset), curPos);
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
