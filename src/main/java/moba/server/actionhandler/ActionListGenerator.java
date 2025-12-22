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

import moba.server.com.Dispatcher;
import moba.server.datatypes.collections.ActionDataList;
import moba.server.datatypes.collections.BlockContactDataMap;
import moba.server.datatypes.collections.SwitchStateMap;
import moba.server.datatypes.enumerations.ControllableFunction;
import moba.server.datatypes.objects.*;
import moba.server.datatypes.objects.actiondata.*;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.InterfaceMessage;
import moba.server.routing.typedefs.SwitchStateData;

import java.util.Vector;

final public class ActionListGenerator {

    private final BlockContactDataMap blockContacts;

    private final SwitchStateMap switchStates;

    private final Dispatcher dispatcher;

    public ActionListGenerator(BlockContactDataMap blockContacts, SwitchStateMap switchStates, Dispatcher dispatcher) {
        this.blockContacts = blockContacts;
        this.switchStates = switchStates;
        this.dispatcher = dispatcher;
    }

    public void sendSwitchActionList(int routeId, Vector<SwitchStateData> switchingList) {
        ActionDataByLocalIdCollection actionLists = new ActionDataByLocalIdCollection();

        for(SwitchStateData switchState : switchingList) {
            SwitchStandData s = switchStates.get(switchState.id());
            actionLists.addActionList(new ActionDataList().addAction(new Switching(s)));
        }

        actionLists.addActionList(new ActionDataList().addAction(new SendRouteSwitched(routeId)));
        this.dispatcher.sendGroup(new Message(InterfaceMessage.SET_ACTION_LIST, actionLists));
    }

    public void sendBlockActionList(Train train, Vector<Long> blockList) {
        // TODO: ACHTUNG: Wie viele Schleifer?
        ActionDataByLocalIdCollection actionLists = new ActionDataByLocalIdCollection(train.address());

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
            new ActionDataList().
                // FIXME: Signal auf freie Fahrt schalten!
                addAction(new Delay(2000)).
                addAction(new LocoFunction(ControllableFunction.HEADLIGHTS, true)).
                addAction(new LocoFunction(ControllableFunction.OPERATING_SOUNDS, true)).
                // … warten, bis Motor warmgelaufen ist :o)
                addAction(new Delay(2000)).
                addAction(new LocoSpeed(391))
        );
        this.dispatcher.sendGroup(new Message(InterfaceMessage.SET_ACTION_LIST, actionLists));
    }

    /**
     * first block:
     *     - block-contact: no actions!
     *     - brake-trigger: no actions!
     */
    private void setFirstListEntry(BlockContactData c, ActionDataByLocalIdCollection actionLists) {
        // FIXME: Hier gibt es im Moment nichts tun! Achtung: Schleifer vom letzten Wagen berücksichtigen!
        //actionLists.addTriggerList(new ActionDataByContactCollection(c.brakeTriggerContact()));
        //actionLists.addTriggerList(new ActionDataByContactCollection(c.blockContact()));
    }

    /**
     * nth block:
     *     - block-contact: release previous block if passed!
     *     - brake-trigger: no actions!
     */
    private void setMiddleListEntry(BlockContactData c, long previousBlock, ActionDataByLocalIdCollection actionLists) {
        actionLists.addTriggerList(new ActionDataByContactCollection(c.brakeTriggerContact()));
        actionLists.addTriggerList(
            new ActionDataByContactCollection(c.blockContact()).
                // FIXME: Signal auf rot!
                addActionList(new ActionDataList().addAction(new SendBlockReleased(previousBlock)))
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
    private void setLastListEntry(BlockContactData c, long previousBlock, ActionDataByLocalIdCollection actionLists) {
        actionLists.addTriggerList(
            new ActionDataByContactCollection(c.blockContact()).
                addActionList(
                    // FIXME: Signal auf rot!
                    (new ActionDataList().addAction(new SendBlockReleased(previousBlock))).
                        addAction(new Delay(1000)).
                        addAction(new LocoHalt())
                )
        );
        actionLists.addTriggerList(
            new ActionDataByContactCollection(c.brakeTriggerContact()).
                addActionList(new ActionDataList().addAction(new LocoSpeed(0)))
        );
    }
}
