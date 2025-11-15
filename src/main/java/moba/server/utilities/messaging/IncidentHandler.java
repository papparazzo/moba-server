/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2022 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.utilities.messaging;

import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.IncidentLevel;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.MessagingMessage;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.logging.Level;
import java.util.logging.Logger;

final public class IncidentHandler {
    private final Logger logger;
    private final Dispatcher dispatcher;
    CircularFifoQueue<IncidentData> list;

    public IncidentHandler(Logger logger, Dispatcher dispatcher, CircularFifoQueue<IncidentData> list) {
        this.logger = logger;
        this.dispatcher = dispatcher;
        this.list = list;
    }

    public synchronized void add(IncidentData incident) {
        logger.log(convertLevel(incident.getLevel()), incident.toString());
        list.add(incident);
        dispatcher.sendGroup(new Message(MessagingMessage.NOTIFY_INCIDENT, incident));
    }

    private Level convertLevel(IncidentLevel level) {
        return switch(level) {
            case CRITICAL, ERROR -> Level.SEVERE;
            case WARNING -> Level.WARNING;
            case NOTICE -> Level.INFO;
        };
    }
}
