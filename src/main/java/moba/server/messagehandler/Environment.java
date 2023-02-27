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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import moba.server.com.Dispatcher;

import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.objects.EnvironmentData;
import moba.server.json.JSONException;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.messageType.EnvironmentMessage;
import moba.server.utilities.config.Config;
import moba.server.utilities.config.ConfigException;
import moba.server.utilities.exceptions.ErrorException;

public class Environment extends MessageHandlerA {
    protected Config  config = null;

    protected EnvironmentData environment = new EnvironmentData();

    public Environment(Dispatcher dispatcher, Config config) {
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    public int getGroupId() {
        return EnvironmentMessage.GROUP_ID;
    }

    @Override
    public void init() {
        Object o;
        o = config.getSection("environment.environment");
        if(o != null) {
            environment.fromJsonObject((Map<String, Object>)o);
        }
    }

    @Override
    public void handleMsg(Message msg)
    throws ErrorException {
        try {
            switch(EnvironmentMessage.fromId(msg.getMessageId())) {

                case GET_ENVIRONMENT:
                    dispatcher.dispatch(new Message(EnvironmentMessage.SET_ENVIRONMENT, environment, msg.getEndpoint()));
                    break;

                case SET_ENVIRONMENT:
                    environment.fromJsonObject((Map<String, Object>)msg.getData());
                    storeData();
                    dispatcher.dispatch(new Message(EnvironmentMessage.SET_ENVIRONMENT, environment));
                    break;

                case SET_AMBIENCE:
                case SET_AMBIENT_LIGHT:
                    msg.convertToBroadCastMessage();
                    dispatcher.dispatch(msg);
                    break;

                default:
                    throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknow msg <" + Long.toString(msg.getMessageId()) + ">.");
            }
        } catch(java.lang.ClassCastException | IOException | JSONException | ConfigException | NullPointerException | IllegalArgumentException e) {
            throw new ErrorException(ErrorId.FAULTY_MESSAGE, e.getMessage());
        }
    }

    protected long convertToLong(Object o) {
        if(o != null && o.getClass() == Integer.class) {
            return (long)Long.valueOf((Integer)o);
        }
        return (Long)o;
    }

    protected void storeData()
    throws ConfigException, IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("environment", environment);
        config.setSection("environment", map);
        config.writeFile();
    }
}
