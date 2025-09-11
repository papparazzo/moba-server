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

package moba.server.actionhandler;

import moba.server.datatypes.collections.BlockContactDataMap;
import moba.server.datatypes.collections.SwitchStateMap;
import moba.server.datatypes.enumerations.ActionType;
import moba.server.datatypes.enumerations.SwitchStand;
import moba.server.datatypes.objects.*;

import java.util.Vector;

final public class ActionListGenerator {

    private final BlockContactDataMap blockContacts;

    private final SwitchStateMap switchStates;

    public ActionListGenerator(BlockContactDataMap blockContacts, SwitchStateMap switchStates) {
        this.blockContacts = blockContacts;
        this.switchStates = switchStates;
    }

    public ActionListCollection getSwitchActionList(int routeId, Vector<Long> switchingList) {
        ActionListCollection actionLists = new ActionListCollection();

        for(Long switchId : switchingList) {
            SwitchStandData s = switchStates.get(switchId);

            if(s.stand() == SwitchStand.BEND) {
                actionLists.addActionList(new ActionList(ActionType.SWITCHING_RED, s.address()));
            } else {
                actionLists.addActionList(new ActionList(ActionType.SWITCHING_GREEN, s.address()));
            }
        }

        actionLists.addActionList(new ActionList(ActionType.SEND_ROUTE_SWITCHED, routeId));
        return actionLists;
    }

    public ActionListCollection getBlockActionList(Train train, Vector<Long> blockList) {
        // TODO: ACHTUNG: Wie viele Schleifer?
        ActionListCollection actionLists = new ActionListCollection(train.address());

        long previousBlock = 0;

        long last = blockList.lastElement();
        long first = blockList.firstElement();

        for(Long blockNodeId : blockList) {
            BlockContactData blockContactData = blockContacts.get(blockNodeId);

            if(blockNodeId == first) {
                setFirstListEntry(blockContactData, actionLists);
            } else if(blockNodeId != last) {
                setMiddleListEntry(blockContactData, previousBlock, actionLists);
            } else {
                setLastListEntry(blockContactData, previousBlock, actionLists);
            }
            previousBlock = blockNodeId;
        }

        actionLists.addActionList(
            new ActionList().
                addAction(ActionType.LOCO_FUNCTION_ON, "HEADLIGHTS").
                addAction(ActionType.LOCO_FUNCTION_ON, "OPERATION_SOUNDS").
                // … warten, bis Motor warmgelaufen ist :o)
                // FIXME: Signal auf freie Fahrt schalten!
                addAction(ActionType.DELAY, 2000).
                addAction(ActionType.LOCO_SPEED, 391)
        );
        return actionLists;
    }

    /**
     * first block:
     *     - block-contact: no actions!
     *     - brake-trigger: no actions!
     */
    private void setFirstListEntry(BlockContactData c, ActionListCollection actionLists) {
        // FIXME: Hier gibt es im Moment nichts tun! Achtung: Schleifer vom letzen Wagen berücksichtigen!
        //actionLists.addTriggerList(new ActionTriggerList(c.brakeTriggerContact()));
        //actionLists.addTriggerList(new ActionTriggerList(c.blockContact()));
    }

    /**
     * nth block:
     *     - block-contact: release previous block if passed!
     *     - brake-trigger: no actions!
     */
    private void setMiddleListEntry(BlockContactData c, long previousBlock, ActionListCollection actionLists) {
        actionLists.addTriggerList(new ActionTriggerList(c.brakeTriggerContact()));
        actionLists.addTriggerList(
            new ActionTriggerList(c.blockContact()).
                addActionList(new ActionList(ActionType.SEND_BLOCK_RELEASED, previousBlock))
        );
    }

    /**
     * last block:
     *     - block-contact:
     *           * delay 1-second
     *           * halt lok
     *           * release previous block if passed!
     *     - brake-trigger: stop lok
     */
    private void setLastListEntry(BlockContactData c, long previousBlock, ActionListCollection actionLists) {
        actionLists.addTriggerList(
            new ActionTriggerList(c.blockContact()).
                addActionList(
                    (new ActionList(ActionType.SEND_BLOCK_RELEASED, previousBlock)).
                        addAction(ActionType.DELAY, 1000).
                        addAction(ActionType.LOCO_HALT)
                )
        );
        actionLists.addTriggerList(
            new ActionTriggerList(c.brakeTriggerContact()).
                addActionList(new ActionList(ActionType.LOCO_SPEED, 0))
        );
    }
}
