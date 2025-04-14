/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2016 Stefan Paproth <pappi-@gmx.de>
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import moba.server.com.Dispatcher;

import moba.server.datatypes.objects.EnvironmentData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.messageType.EnvironmentMessage;
import moba.server.utilities.config.Config;
import moba.server.utilities.exceptions.ErrorException;

public class Environment extends MessageHandlerA {
    protected final Config config;

    protected final EnvironmentData environment = new EnvironmentData();

    @SuppressWarnings("unchecked")
    public Environment(Dispatcher dispatcher, Config config)
    throws ErrorException {
        this.dispatcher = dispatcher;
        this.config = config;
        this.environment.fromJsonObject((Map<String, Object>)config.getSection("environment.environment"));
    }

    @Override
    public int getGroupId() {
        return EnvironmentMessage.GROUP_ID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleMsg(Message msg)
    throws ErrorException, IOException {
        switch(EnvironmentMessage.fromId(msg.getMessageId())) {
            case GET_ENVIRONMENT ->
                dispatcher.send(new Message(EnvironmentMessage.SET_ENVIRONMENT, environment), msg.getEndpoint());

            case SET_ENVIRONMENT -> {
                environment.fromJsonObject((Map<String, Object>)msg.getData());
                storeData();
                dispatcher.broadcast(new Message(EnvironmentMessage.SET_ENVIRONMENT, environment));
            }

            case SET_AMBIENCE, SET_AMBIENT_LIGHT ->
                dispatcher.broadcast(msg);
        }
    }

    protected void storeData()
    throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("environment", environment);
        config.setSection("environment", map);
        config.writeFile();
    }
}
