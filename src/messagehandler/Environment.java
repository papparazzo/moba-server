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

import utilities.config.*;
import json.streamwriter.*;
import java.util.*;

import com.*;
import global.*;
import java.io.*;
import json.*;
import messages.*;
import utilities.*;

public class Environment extends MessageHandlerA implements JSONToStringI {
    protected Dispatcher  dispatcher = null;
    protected GlobalTimer globalTimer = null;
    protected Config  config = null;

    protected Switch thunderStorm = Switch.OFF;
    protected Switch environmentSound = Switch.OFF;
    protected Switch wind = Switch.OFF;
    protected Switch rain = Switch.OFF;
    protected Switch aux01 = Switch.OFF;
    protected Switch aux02 = Switch.OFF;
    protected Switch aux03 = Switch.OFF;

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
            o = this.config.getSection("environment.ambience");
            if(o != null) {
                this.setAmbience((Map<String, Object>)o);
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
                    this.storeData();
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
                    this.storeData();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_ENVIRONMENT,
                            this
                        )
                    );
                    break;

                case GET_AMBIENCE:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_AMBIENCE,
                            this.getAmbience(),
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_AMBIENCE:
                    this.setAmbience((Map<String, Object>)msg.getData());
                    this.storeData();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_AMBIENCE,
                            this.getAmbience()
                        )
                    );
                    break;

                case GET_AUTO_MODE:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_AUTO_MODE,
                            this.dayNightSimulation,
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_AUTO_MODE:
                    this.setAutoMode(msg.getData());
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
                    this.storeData();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_COLOR_THEME,
                            this.getColorThemeData()
                        )
                    );
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

    protected void storeData()
    throws ConfigException, IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("globaltimer", this.getGlobalTimerData());
        map.put("colortheme", this.getColorThemeData());
        map.put("ambient", this.getAmbience());
        map.put("environment", this);
        this.config.setSection("environment", map);
        this.config.writeFile();
    }

    protected void setGlobalTimer(Map<String, Object> map)
    throws GlobalTimerException {
        this.globalTimer.setModelTime((String)map.get("curModelTime"));
        this.globalTimer.setIntervall((long)map.get("intervall"));
        this.globalTimer.setMultiplicator((long)map.get("multiplicator"));
    }

    protected void setColorTheme(Map<String, Object> map) {
        this.globalTimer.setColorThemeChangeTimes(
            (String)map.get("dimTime"),
            (String)map.get("brightTime")
        );
    }

    protected void setAmbience(Map<String, Object> map) {
        this.curtainUp = ThreeState.getValue((Switch)map.get("curtainUp"), this.curtainUp);
        this.mainLightOn = ThreeState.getValue((Switch)map.get("mainLightOn"), this.mainLightOn);
    }

    protected void setEnvironment(Map<String, Object> map) {
        this.thunderStorm = Switch.getValue(map.get("thunderStorm"), this.thunderStorm);
        this.environmentSound = Switch.getValue(map.get("environmentSound"), this.environmentSound);
        this.wind =  Switch.getValue(map.get("wind"), this.wind);
        this.rain = Switch.getValue(map.get("rain"), this.rain);
        this.aux01 = Switch.getValue(map.get("aux01"), this.aux01);
        this.aux02 = Switch.getValue(map.get("aux02"), this.aux02);
        this.aux03 = Switch.getValue(map.get("aux03"), this.aux03);
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
        map.put("dimTime",    this.globalTimer.getDimTimeString());
        map.put("brightTime", this.globalTimer.getBrightTimeString());
        return map;
    }

    protected Object getAmbience() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("curtainUp", ThreeState.getValue(this.curtainUp));
        map.put("mainLightOn", ThreeState.getValue(this.mainLightOn));
        return map;
    }

    protected void setAutoMode(Object o) {
        boolean active = (boolean)o;
        if(this.dayNightSimulation == active) {
            return;
        }
        this.dayNightSimulation = active;

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

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("thunderStorm", this.thunderStorm);
        map.put("wind", this.wind);
        map.put("rain", this.rain);
        map.put("environmentSound", this.environmentSound);
        map.put("aux01", this.aux01);
        map.put("aux02", this.aux02);
        map.put("aux03", this.aux03);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
