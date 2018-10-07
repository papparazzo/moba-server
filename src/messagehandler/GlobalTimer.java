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

import com.Dispatcher;
import datatypes.enumerations.ColorTheme;
import datatypes.enumerations.ErrorId;
import datatypes.enumerations.HardwareState;
import datatypes.enumerations.ThreeState;
import datatypes.objects.ColorThemeData;
import datatypes.objects.ErrorData;
import datatypes.objects.GlobalTimerData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import json.JSONException;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;
import utilities.config.Config;
import utilities.config.ConfigException;

public class GlobalTimer extends MessageHandlerA implements Runnable {
    protected Dispatcher  dispatcher = null;
    protected Config      config = null;
    protected Thread      thread = null;
    protected GlobalTimerData timerData = null;
    protected ColorThemeData  themeData = null;

    protected ColorTheme       curTheme = null;
    protected volatile boolean isRunning = false;

    public GlobalTimer(Dispatcher dispatcher, Config config) {
        this.dispatcher = dispatcher;
        this.config = config;
        this.timerData = new GlobalTimerData();
        this.themeData = new ColorThemeData();
    }

    @Override
    public void init() {
        Object o;
        o = this.config.getSection("globaltimer.globaltimer");
        if(o != null) {
            this.timerData.fromJsonObject((Map<String, Object>)o);
        }
        o = this.config.getSection("globaltimer.colortheme");
        if(o != null) {
            this.themeData.fromJsonObject((Map<String, Object>)o);
        }
        switch(this.themeData.getColorThemeCondition()) {
            case UNSET:
            case AUTO:
                // FIXME: ...
                break;

            case ON:
                this.curTheme = ColorTheme.BRIGHT;
                break;

            case OFF:
                this.curTheme = ColorTheme.DIM;
                break;

        }
    }

    @Override
    public void shutdown() {
        this.isRunning = false;
    }

    @Override
    public void handleMsg(Message msg) {
        try {
            switch(msg.getMsgType()) {
                case GET_GLOBAL_TIMER:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_GLOBAL_TIMER,
                            this.timerData,
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_GLOBAL_TIMER:
                    this.timerData.fromJsonObject((Map<String, Object>)msg.getData());
                    this.storeData();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_GLOBAL_TIMER,
                            this.timerData
                        )
                    );
                    break;

                case GET_COLOR_THEME:
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_COLOR_THEME,
                            this.themeData,
                            msg.getEndpoint()
                        )
                    );
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.COLOR_THEME_EVENT,
                            this.curTheme,
                            msg.getEndpoint()
                        )
                    );
                    break;

                case SET_COLOR_THEME:
                    this.themeData.fromJsonObject((Map<String, Object>)msg.getData());
                    this.storeData();
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SET_COLOR_THEME,
                            this.themeData
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
        } catch(IllegalArgumentException e) {
            this.dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorData(
                        ErrorId.INVALID_DATA_SEND,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }

    @Override
    public void run() {
        try {
            while(!this.thread.isInterrupted()) {
                Thread.sleep(1000);
                if(!this.isRunning) {
                    continue;
                }

                if(this.timerData.setTick()) {
                    // FIXME: Is this really thread-save??
                    this.dispatcher.dispatch(
                        new Message(MessageType.GLOBAL_TIMER_EVENT, this.timerData)
                    );
                }

                if(this.themeData.getColorThemeCondition() != ThreeState.AUTO) {
                    continue;
                }

                boolean isBetween = this.timerData.isTimeBetween(
                    this.themeData.getBrightTime(),
                    this.themeData.getDimTime()
                );

                if(isBetween && this.curTheme == ColorTheme.BRIGHT) {
                    continue;
                }

                if(!isBetween && this.curTheme == ColorTheme.DIM) {
                    continue;
                }

                if(this.curTheme == ColorTheme.BRIGHT) {
                    this.curTheme = ColorTheme.DIM;
                } else {
                    this.curTheme = ColorTheme.BRIGHT;
                }

                // FIXME: Is this really thread-save??
                this.dispatcher.dispatch(
                    new Message(MessageType.COLOR_THEME_EVENT, this.curTheme)
                );
            }
        } catch(InterruptedException e) {

        }
    }

    protected void storeData()
    throws ConfigException, IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("globaltimer", this.timerData);
        map.put("colortheme", this.themeData);
        this.config.setSection("globaltimer", map);
        this.config.writeFile();
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        boolean active = (state == HardwareState.AUTOMATIC);
        if(this.isRunning == active) {
            return;
        }
        this.isRunning = active;

        if(this.thread != null && this.thread.isAlive()) {
            return;
        }

        if(!this.isRunning) {
            return;
        }

        this.thread = new Thread(this);
        this.thread.setName("globaltimer");
        this.thread.start();
    }
}


