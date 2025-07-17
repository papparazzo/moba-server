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

import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.ActionType;
import moba.server.datatypes.objects.*;
import moba.server.messages.Message;
import moba.server.messages.messageType.InterfaceMessage;
import moba.server.repositories.Blocklist;
import moba.server.repositories.Trainlist;
import moba.server.utilities.Database;
import moba.server.utilities.exceptions.ClientErrorException;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Vector;

public class TrainRun {
    private final Dispatcher dispatcher;

    private final Router routing;

    private final Trainlist trainlist;

    private final Blocklist blocklist;

    private final int layoutId = 10;

    public TrainRun(Dispatcher dispatcher, Router routing, Database database) {
        this.dispatcher = dispatcher;
        this.routing = routing;

        blocklist = new Blocklist(database);
        trainlist = new Trainlist(database);
    }

    // Hier muss sich der Zug an dieser Position befinden
    public void feed(int fromBlock, int toBlock, int trainId)
    throws SQLException, ClientErrorException {

        var blocks = blocklist.getBlockList(layoutId);
        var trains = trainlist.getTrainList(layoutId);

        // ACHTUNG: Wie viele Schleifer?

        Vector<Integer> v = routing.getRoute(fromBlock, toBlock);

        var localId = trains.get(trainId).address();

        var last = v.lastElement();
        var first = v.firstElement();

        ActionListCollection actionLists = new ActionListCollection(localId);

        int previousBlock = 0;

        for(Integer i : v) {
            BlockContactData c = blocks.get(i);

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
            previousBlock = i;
        }

        actionLists.addActionList(
            new ActionList().
                addAction(ActionType.LOCO_FUNCTION_ON, "HEADLIGHTS").
                addAction(ActionType.LOCO_FUNCTION_ON, "OPERATION_SOUNDS").
                addAction(ActionType.DELAY, 2000).
                addAction(ActionType.LOCO_SPEED, 391)
        );

        dispatcher.sendGroup(new Message(InterfaceMessage.SET_ACTION_LIST, actionLists));
    }

    // Hier ist es egal, wo der Zug sich gerade befindet!
    public void feed(int toBlock, int trainId)
    throws SQLException, ClientErrorException {
        var trains = trainlist.getTrainList(layoutId);
        feed(trains.get(trainId).blockId(), toBlock, trainId);
    }
}
