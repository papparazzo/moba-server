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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.objects.GlobalTimerData;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import moba.server.com.Dispatcher;
import moba.server.datatypes.objects.PointOfTime;
import moba.server.datatypes.objects.TrackLayoutInfoData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.messageType.TimerMessage;
import moba.server.utilities.Database;
import moba.server.utilities.config.Config;
import moba.server.utilities.exceptions.ClientErrorException;

/*
 * https://www.laenderdaten.info/Europa/Deutschland/sonnenuntergang.php
 *
 * ab 04:00 Uhr Sonnenaufgang
 * ab 05:00 Uhr Tag
 * ab 21:00 Uhr Sonnenuntergang
 * ab 22:00 Uht Nacht
 */
public class Timer extends MessageHandlerA implements Runnable {
    protected Config          config;
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected GlobalTimerData timerData = null;
    protected Database        database;

    protected volatile boolean isRunning = false;

    public Timer(Dispatcher dispatcher, Config config, Database database) {
        this.dispatcher = dispatcher;
        this.config = config;
        this.database = database;
        this.scheduler.scheduleWithFixedDelay(this, 1, 1, TimeUnit.SECONDS);
        this.timerData = (GlobalTimerData)config.getSection("globalTimer.globalTimer");
        if(timerData == null) {
            this.timerData = new GlobalTimerData();
        }
    }

    @Override
    public int getGroupId() {
        return TimerMessage.GROUP_ID;
    }

    @Override
    public void shutdown()
    throws Exception {
        super.shutdown();
        isRunning = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleMsg(Message msg)
    throws ClientErrorException, IOException {
        switch(TimerMessage.fromId(msg.getMessageId())) {
            case GET_GLOBAL_TIMER ->
                dispatcher.sendSingle(new Message(TimerMessage.SET_GLOBAL_TIMER, timerData), msg.getEndpoint());

            case SET_GLOBAL_TIMER -> {
                timerData.fromJsonObject((Map<String, Object>)msg.getData());
                storeData();
                dispatcher.sendGroup(new Message(TimerMessage.SET_GLOBAL_TIMER, timerData));
            }
        }
    }

    @Override
    public void run() {
        try {
            if(!isRunning) {
                return;
            }

            if(timerData.setTick()) {
                dispatcher.sendGroup(new Message(TimerMessage.GLOBAL_TIMER_EVENT, timerData.getModelTime()));
            }

        } catch(Exception ignored) {

        }
    }

    protected void storeData()
    throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("globalTimer", timerData);
        config.setSection("globalTimer", map);
        config.writeFile();
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        isRunning = (state == HardwareState.AUTOMATIC);
    }
}
