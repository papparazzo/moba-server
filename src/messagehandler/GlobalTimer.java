/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2019 Stefan Paproth <pappi-@gmx.de>
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

import com.SenderI;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import json.JSONException;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;
import utilities.config.Config;
import utilities.config.ConfigException;

public class GlobalTimer extends MessageHandlerA implements Runnable {
    protected SenderI         dispatcher = null;
    protected Config          config = null;
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected GlobalTimerData timerData = new GlobalTimerData();
    protected ColorThemeData  themeData = new ColorThemeData();

    protected ColorTheme       curTheme = null;
    protected volatile boolean isRunning = false;

    public GlobalTimer(SenderI dispatcher, Config config) {
        this.dispatcher = dispatcher;
        this.config = config;
        this.scheduler.scheduleWithFixedDelay(this, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void init() {
        Object o;
        o = config.getSection("globaltimer.globaltimer");
        if(o != null) {
            timerData.fromJsonObject((Map<String, Object>)o);
        }
        o = config.getSection("globaltimer.colortheme");
        if(o != null) {
            themeData.fromJsonObject((Map<String, Object>)o);
        }
        switch(themeData.getColorThemeCondition()) {
            case UNSET:
            case AUTO:
                // FIXME: ...
                break;

            case ON:
                curTheme = ColorTheme.BRIGHT;
                break;

            case OFF:
                curTheme = ColorTheme.DIM;
                break;

        }
    }

    @Override
    public void shutdown() {
        isRunning = false;
    }

    @Override
    public void handleMsg(Message msg) {
        try {
            switch(msg.getMsgType()) {
                case TIMER_GET_GLOBAL_TIMER:
                    dispatcher.dispatch(
                        new Message(MessageType.TIMER_SET_GLOBAL_TIMER, timerData, msg.getEndpoint())
                    );
                    break;

                case TIMER_SET_GLOBAL_TIMER:
                    timerData.fromJsonObject((Map<String, Object>)msg.getData());
                    storeData();
                    dispatcher.dispatch(new Message(MessageType.TIMER_SET_GLOBAL_TIMER, timerData)
                    );
                    break;

                case TIMER_GET_COLOR_THEME:
                    dispatcher.dispatch(
                        new Message(MessageType.TIMER_SET_COLOR_THEME, themeData, msg.getEndpoint())
                    );
                    dispatcher.dispatch(
                        new Message(MessageType.TIMER_COLOR_THEME_EVENT, curTheme, msg.getEndpoint())
                    );
                    break;

                case TIMER_SET_COLOR_THEME:
                    themeData.fromJsonObject((Map<String, Object>)msg.getData());
                    storeData();
                    dispatcher.dispatch(new Message(MessageType.TIMER_SET_COLOR_THEME, themeData));
                    break;

                default:
                    throw new UnsupportedOperationException("unknow msg <" + msg.getMsgType().toString() + ">.");
            }
        } catch(java.lang.ClassCastException | IOException | JSONException | ConfigException | NullPointerException e) {
            dispatcher.dispatch(new Message(MessageType.CLIENT_ERROR, new ErrorData(ErrorId.FAULTY_MESSAGE, e.getMessage()), msg.getEndpoint()));
        } catch(IllegalArgumentException e) {
            dispatcher.dispatch(new Message(MessageType.CLIENT_ERROR, new ErrorData(ErrorId.INVALID_DATA_SEND, e.getMessage()), msg.getEndpoint()));
        }
    }

    @Override
    public void run() {
        try {
            if(!isRunning) {
                return;
            }
            if(timerData.setTick()) {
                dispatcher.dispatch(new Message(MessageType.TIMER_GLOBAL_TIMER_EVENT, timerData));
            }

            if(themeData.getColorThemeCondition() != ThreeState.AUTO) {
                return;
            }

            boolean isBetween = timerData.isTimeBetween(themeData.getBrightTime(), themeData.getDimTime());

            if(isBetween && curTheme == ColorTheme.BRIGHT) {
                return;
            }

            if(!isBetween && curTheme == ColorTheme.DIM) {
                return;
            }

            if(curTheme == ColorTheme.BRIGHT) {
                curTheme = ColorTheme.DIM;
            } else {
                curTheme = ColorTheme.BRIGHT;
            }

            dispatcher.dispatch(new Message(MessageType.TIMER_COLOR_THEME_EVENT, curTheme));
        } catch(Exception e) {

        }
    }

    protected void storeData()
    throws ConfigException, IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("globaltimer", timerData);
        map.put("colortheme", themeData);
        config.setSection("globaltimer", map);
        config.writeFile();
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        boolean active = (state == HardwareState.AUTOMATIC);
        if(isRunning == active) {
            return;
        }
        isRunning = active;
    }
}
