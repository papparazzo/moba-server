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

import com.Dispatcher;
import datatypes.base.Percent;
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
    protected Dispatcher  dispatcher = null;
    protected Config      config = null;

    protected EnvironmentData  environment  = new EnvironmentData();
    protected AmbienceData     ambience     = new AmbienceData();
    protected AmbientLightData ambientLight = new AmbientLightData();

    public Environment(Dispatcher dispatcher, Config config) {
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    public void init() {
        Object o;
        o = this.config.getSection("environment.environment");
        if(o != null) {
            this.environment.fromJsonObject((Map<String, Object>)o);
        }
        o = this.config.getSection("environment.ambience");
        if(o != null) {
            this.ambience.fromJsonObject((Map<String, Object>)o);
        }
        o = this.config.getSection("environment.ambientlight");
        if(o != null) {
            this.setAmbientLight((Map<String, Object>)o);
        }
    }

    @Override
    public void handleMsg(Message msg) {
        try {
            switch(msg.getMsgType()) {

                case GET_ENVIRONMENT:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_ENVIRONMENT,
                            this.environment,
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_ENVIRONMENT:
                    this.environment.fromJsonObject((Map<String, Object>)msg.getData());
                    this.storeData();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_ENVIRONMENT,
                            this.environment
                        )
                    );
                    break;

                case GET_AMBIENCE:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_AMBIENCE,
                            this.ambience,
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_AMBIENCE:
                    this.ambience.fromJsonObject((Map<String, Object>)msg.getData());
                    this.storeData();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_AMBIENCE,
                            this.ambience
                        )
                    );
                    break;

                case GET_AMBIENT_LIGHT:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_AMBIENT_LIGHT,
                            this.ambientLight,
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_AMBIENT_LIGHT:
                    this.setAmbientLight((Map<String, Object>)msg.getData());
                    this.storeData();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_AMBIENT_LIGHT,
                            this.ambientLight
                        )
                    );
                    break;

                default:
                    throw new UnsupportedOperationException(
                        "unknow msg <" + msg.getMsgType().toString() + ">."
                    );
            }
        } catch(
            java.lang.ClassCastException | IOException | JSONException |
            ConfigException | NullPointerException e
        ) {
            this.dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorData(
                        ErrorId.FAULTY_MESSAGE,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }

    protected void setAmbientLight(Map<String, Object> map) {
        this.ambientLight.setRed(new Percent((int)(long)map.get("red")));
        this.ambientLight.setBlue(new Percent((int)(long)map.get("blue")));
        this.ambientLight.setGreen(new Percent((int)(long)map.get("green")));
        this.ambientLight.setWhite(new Percent((int)(long)map.get("white")));
    }

    protected void storeData()
    throws ConfigException, IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("ambient",      this.ambience);
        map.put("environment",  this.environment);
        map.put("ambientlight", this.ambientLight);
        this.config.setSection("environment", map);
        this.config.writeFile();
    }
}
