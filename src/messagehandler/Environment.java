/*
 *  appserver2
 *
 *  Copyright (C) 2014 Stefan Paproth <pappi-@gmx.de>
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

import utilities.config.Config;
import json.streamwriter.JSONStreamWriterStringBuilder;
import java.util.*;

import com.*;
import global.*;
import java.io.IOException;
import json.*;
import messages.*;
import utilities.*;
import utilities.config.ConfigException;

public class Environment extends MessageHandlerA implements JSONToStringI {
    protected Dispatcher  dispatcher = null;
    protected GlobalTimer globalTimer = null;
    protected Config  config = null;

    protected ThreeState thunderStorm = ThreeState.OFF;
    protected ThreeState wind = ThreeState.OFF;
    protected ThreeState rain = ThreeState.OFF;
    protected ThreeState aux01 = ThreeState.OFF;
    protected boolean curtainUp = false;
    protected boolean dayNightSimulation = false;
    protected boolean mainLightOn = false;

    public Environment(Dispatcher dispatcher, Config config) {
        this.globalTimer = new GlobalTimer(dispatcher);
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    public void init() {
        try {
            Object o;
            o = this.config.getSection("environment.environment");
            if(o != null) {
                this.setEnvironment((Map<String, Object>)o);
            }
            o = this.config.getSection("environment.globaltimer");
            if(o != null) {
                this.setGlobalTimer((Map<String, Object>)o);
            }
            o = this.config.getSection("environment.colortheme");
            if(o != null) {
                this.setColorTheme((Map<String, Object>)o);
            }
        } catch(GlobalTimerException e) {
            throw new ExceptionInInitializerError();
        }
    }

    @Override
    public void shutdown() {
        this.globalTimer.stopGlobalTimer();
    }

    @Override
    public void handleMsg(Message msg) {
        try {
            switch(msg.getMsgType()){
                case GET_GLOBAL_TIMER:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_GLOBAL_TIMER,
                            this.getGlobalTimerData(),
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_GLOBAL_TIMER:
                    this.setGlobalTimer((Map<String, Object>)msg.getData());
                    this.config.setSection("environment.globaltimer", this.getGlobalTimerData());
                    this.config.writeFile();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_GLOBAL_TIMER,
                            this.getGlobalTimerData()
                        )
                    );
                    break;

                case GET_ENVIRONMENT:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_ENVIRONMENT,
                            this,
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_ENVIRONMENT:
                    this.setEnvironment((Map<String, Object>)msg.getData());
                    this.config.setSection("environment.environment", this);
                    this.config.writeFile();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_ENVIRONMENT,
                            this
                        )
                    );
                    break;

                case GET_COLOR_THEME:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_COLOR_THEME,
                            this.getColorThemeData(),
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_COLOR_THEME:
                    this.setColorTheme((Map<String, Object>)msg.getData());
                    this.config.setSection("environment.colortheme", this.getColorThemeData());
                    this.config.writeFile();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_COLOR_THEME,
                            this.getColorThemeData()
                        )
                    );
                    break;

                case SET_AUTO_MODE:
                    this.setAutoMode(msg.getData());
                    break;

                default:
                    throw new UnsupportedOperationException(
                        "unknow msg <" + msg.getMsgType().toString() + ">."
                    );
            }
        } catch(java.lang.ClassCastException | GlobalTimerException | IOException | JSONException | ConfigException e) {
            this.dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorInfo(
                        ErrorInfo.ErrorId.FAULTY_MESSAGE,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }

    protected void setGlobalTimer(Map<String, Object> map)
    throws GlobalTimerException {
        this.globalTimer.setModelTime((String)map.get("curModelTime"));
        this.globalTimer.setIntervall((long)map.get("intervall"));
        this.globalTimer.setMultiplicator((long)map.get("multiplicator"));
    }

    protected void setEnvironment(Map<String, Object> map) {
        this.thunderStorm = (ThreeState)map.get("thunderStorm");
        this.wind = (ThreeState)map.get("wind");
        this.rain = (ThreeState)map.get("rain");
        this.aux01 = (ThreeState)map.get("aux01");
        this.curtainUp = (boolean)map.get("curtainUp");
        this.mainLightOn = (boolean)map.get("mainLightOn");
    }

    protected Object getGlobalTimerData() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("curModelTime",  this.globalTimer.getCurrentModelTime());
        map.put("intervall",     this.globalTimer.getIntervall());
        map.put("multiplicator", this.globalTimer.getMultiplicator());
        return map;
    }

    protected Object getColorThemeData() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("dimTime",    this.globalTimer.getDimTime());
        map.put("brightTime", this.globalTimer.getBrightTime());
        return map;
    }

    protected void setAutoMode(Object o) {
        if(o == null) {
            this.dayNightSimulation = !this.dayNightSimulation;
        } else {
            boolean active = (boolean)o;
            if(this.dayNightSimulation == active) {
                return;
            }
            this.dayNightSimulation = active;
        }

        if(this.dayNightSimulation) {
            this.globalTimer.startGlobalTimer();
        } else {
            this.globalTimer.stopGlobalTimer();
        }
        this.dispatcher.dispatch(
            new Message(
                MessageType.SET_AUTO_MODE,
                this.dayNightSimulation
            )
        );
    }

    protected void setColorTheme(Map<String, Object> map) {
        this.globalTimer.setColorThemeChangeTimes(
            (String)map.get("dimTime"),
            (String)map.get("brightTime")
        );
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("thunderStorm", this.thunderStorm);
        map.put("wind", this.wind);
        map.put("rain", this.rain);
        map.put("aux01", this.aux01);
        map.put("curtainUp", this.curtainUp);
        map.put("mainLightOn", this.mainLightOn);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
