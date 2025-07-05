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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.*;
import moba.server.utilities.CheckedEnum;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.InterfaceMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.utilities.exceptions.ClientErrorException;
import moba.server.utilities.messaging.IncidentHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

final public class Interface extends MessageHandlerA {

    private final MessageQueue msgQueueIn;
    private final IncidentHandler incidentHandler;

    public Interface(Dispatcher dispatcher, MessageQueue msgQueueIn, IncidentHandler incidentHandler) {
        this.dispatcher      = dispatcher;
        this.msgQueueIn      = msgQueueIn;
        this.incidentHandler = incidentHandler;
    }

    @Override
    public int getGroupId() {
        return InterfaceMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException {
        switch(InterfaceMessage.fromId(msg.getMessageId())) {
            case CONNECTIVITY_STATE_CHANGED:
                setConnectivity(CheckedEnum.getFromString(Connectivity.class, (String)msg.getData()), msg.getEndpoint());
                return;

            case SWITCH_ROUTE:
                addactionList();
                return;

            case ROUTE_SWITCHED:
            case ROUTE_RELEASED:
            case BLOCK_RELEASED:
            case SET_ACTION_LIST:
            case REPLACE_ACTION_LIST:
            case DELETE_ACTION_LIST:
                dispatcher.sendGroup(msg);
        }
    }

    private void setConnectivity(Connectivity connectivity, Endpoint ep) {
        switch(connectivity) {
            case CONNECTED:
                msgQueueIn.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
                addIncident(IncidentLevel.NOTICE, "Die Verbindung zur Hardware wurde hergestellt", ep);
                break;

            case ERROR:
                msgQueueIn.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.ERROR));
                addIncident(IncidentLevel.ERROR, "Die Verbindung zur Hardware wurde unterbrochen", ep);
                break;
        }
    }

    private void addIncident(IncidentLevel level, String message, Endpoint ep) {
        incidentHandler.add(new IncidentData(
            level,
            IncidentType.STATUS_CHANGED,
            "Hardwareverbindung",
            message,
            "Interface.addIncident()",
            ep
        ));
    }



    private void addactionList() {

        HashMap<String, Object> action = new HashMap<>();
        //action.put("action", type);
        action.put("data", 64);

        ArrayList<HashMap<String, Object>> actions = new ArrayList<>();
        actions.add(action);

        ArrayList<ArrayList<HashMap<String, Object>>> actionslist = new ArrayList<>();
        actionslist.add(actions);


        HashMap<String, Object> data = new HashMap<>();
        data.put("localId",   16410);
        data.put("trigger",   null);
        data.put("actionLists", actionslist);


        var msg = new Message(InterfaceMessage.SET_ACTION_LIST, data);


        dispatcher.sendGroup(msg);
    }


}
