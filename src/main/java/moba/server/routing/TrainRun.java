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

import moba.server.datatypes.enumerations.ActionType;
import moba.server.datatypes.objects.*;
import moba.server.datatypes.collections.BlockContactDataMap;
import moba.server.routing.router.SimpleRouter;
import moba.server.routing.typedefs.SwitchStateData;

import java.util.Objects;
import java.util.Vector;

public class TrainRun {

    private final SimpleRouter routing;


    private final BlockContactDataMap blockContacts;

    public TrainRun(BlockContactDataMap blockContacts, SimpleRouter routing) {
        this.blockContacts = blockContacts;
        this.routing = routing;
    }

    public ActionListCollection getActionList(TrainData train, int toBlock) {

        int fromBlock = train.blockId();

        // TODO: ACHTUNG: Wie viele Schleifer?

        Vector<SwitchStateData> v = routing.getRoute(fromBlock, toBlock);


        var last = v.lastElement();
        var first = v.firstElement();

        ActionListCollection actionLists = new ActionListCollection(train.address());

        long previousBlock = 0;

        for(SwitchStateData i : v) {
            BlockContactData c = blockContacts.get(i.id());

            if(c == null) {
                // FIXME: Wenn c == null dann Weiche und kein Block!!
                continue;
            }

            if(Objects.equals(first, i)) {
                // first block:
                //     - block-contact: no actions!
                //     - brake-trigger: no actions!

                // FIXME: Hier gibt es im Moment nichts tun! Achtung: Schleifer vom letzen Wagen berücksichtigen!
                //actionLists.addTriggerList(new ActionTriggerList(c.brakeTriggerContact()));
                //actionLists.addTriggerList(new ActionTriggerList(c.blockContact()));
            } else if(!Objects.equals(i, last)) {
                // nth block:
                //     - block-contact: release previous block if passed!
                //     - brake-trigger: no actions!

                actionLists.addTriggerList(new ActionTriggerList(c.brakeTriggerContact()));
                actionLists.addTriggerList(
                    new ActionTriggerList(c.blockContact()).
                        addActionList(new ActionList(ActionType.SEND_BLOCK_RELEASED, previousBlock))
                );
            } else {
                // last block:
                //     - block-contact:
                //           * delay 1-second
                //           * halt lok
                //           * release previous block if passed!
                //     - brake-trigger: stop lok

                actionLists.addTriggerList(
                    new ActionTriggerList(c.blockContact()).
                        addActionList(
                            (new ActionList(ActionType.SEND_BLOCK_RELEASED, previousBlock)).
                                addAction(ActionType.DELAY, 1000).
                                addAction(ActionType.LOCO_HALT)//.
                                //addActionOnCondition(ActionType.CHECK_NEXT, last != toBlock)
                        )
                );
                actionLists.addTriggerList(
                    new ActionTriggerList(c.brakeTriggerContact()).
                        addActionList(new ActionList(ActionType.LOCO_SPEED, 0))
                );
            }
            previousBlock = i.id();
        }

        actionLists.addActionList(
            new ActionList().
                addAction(ActionType.LOCO_FUNCTION_ON, "HEADLIGHTS").
                addAction(ActionType.LOCO_FUNCTION_ON, "OPERATION_SOUNDS").
                // … warten, bis Motor warmgelaufen ist :o)
                addAction(ActionType.DELAY, 2000).
                addAction(ActionType.LOCO_SPEED, 391)
        );
        return actionLists;
    }
}
