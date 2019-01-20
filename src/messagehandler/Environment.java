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

package messagehandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.SenderI;
import datatypes.enumerations.ErrorId;
import datatypes.objects.AmbienceData;
import datatypes.objects.AmbientLightData;
import datatypes.objects.ErrorData;
import datatypes.objects.EnvironmentData;
import json.JSONException;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;
import utilities.config.Config;
import utilities.config.ConfigException;

public class Environment extends MessageHandlerA {
    protected SenderI dispatcher = null;
    protected Config  config = null;

    protected EnvironmentData  environment  = new EnvironmentData();
    protected AmbienceData     ambience     = new AmbienceData();
    protected AmbientLightData ambientLight = new AmbientLightData();

    public Environment(SenderI dispatcher, Config config) {
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    public void init() {
        Object o;
        o = config.getSection("environment.environment");
        if(o != null) {
            environment.fromJsonObject((Map<String, Object>)o);
        }
        o = config.getSection("environment.ambience");
        if(o != null) {
            ambience.fromJsonObject((Map<String, Object>)o);
        }
        o = config.getSection("environment.ambientlight");
        if(o != null) {
            setAmbientLight((Map<String, Object>)o);
        }
    }

    @Override
    public void handleMsg(Message msg) {
        try {
            switch(msg.getMsgType()) {

                case ENV_GET_ENVIRONMENT:
                    dispatcher.dispatch(
                        new Message(MessageType.ENV_SET_ENVIRONMENT, environment, msg.getEndpoint())
                    );
                    break;

                case ENV_SET_ENVIRONMENT:
                    environment.fromJsonObject((Map<String, Object>)msg.getData());
                    storeData();
                    dispatcher.dispatch(new Message(MessageType.ENV_SET_ENVIRONMENT, environment));
                    break;

                case ENV_GET_AMBIENCE:
                    dispatcher.dispatch(new Message(MessageType.ENV_SET_AMBIENCE, ambience, msg.getEndpoint()));
                    break;

                case ENV_SET_AMBIENCE:
                    ambience.fromJsonObject((Map<String, Object>)msg.getData());
                    storeData();
                    dispatcher.dispatch(new Message(MessageType.ENV_SET_AMBIENCE, ambience));
                    break;

                case ENV_GET_AMBIENT_LIGHT:
                    dispatcher.dispatch(
                        new Message(MessageType.ENV_SET_AMBIENT_LIGHT, ambientLight, msg.getEndpoint())
                    );
                    break;

                case ENV_SET_AMBIENT_LIGHT:
                    setAmbientLight((Map<String, Object>)msg.getData());
                    storeData();
                    dispatcher.dispatch(new Message(MessageType.ENV_SET_AMBIENT_LIGHT, ambientLight));
                    break;

                default:
                    throw new UnsupportedOperationException(
                        "unknow msg <" + msg.getMsgType().toString() + ">."
                    );
            }
        } catch(
            java.lang.ClassCastException | IOException | JSONException |
            ConfigException | NullPointerException | IllegalArgumentException e
        ) {
            dispatcher.dispatch(
                new Message(
                    MessageType.CLIENT_ERROR,
                    new ErrorData(ErrorId.FAULTY_MESSAGE, e.getMessage()),
                    msg.getEndpoint()
                )
            );
        }
    }

    protected void setAmbientLight(Map<String, Object> map) {
        ambientLight.setRed((int)(long)map.get("red"));
        ambientLight.setBlue((int)(long)map.get("blue"));
        ambientLight.setGreen((int)(long)map.get("green"));
        ambientLight.setWhite((int)(long)map.get("white"));
    }

    protected void storeData()
    throws ConfigException, IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("ambient",      ambience);
        map.put("environment",  environment);
        map.put("ambientlight", ambientLight);
        config.setSection("environment", map);
        config.writeFile();
    }
}
