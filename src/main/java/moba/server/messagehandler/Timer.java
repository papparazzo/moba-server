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

import moba.server.actionhandler.Scheduler;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.objects.GlobalTimerData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import moba.server.com.Dispatcher;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.messageType.TimerMessage;
import moba.server.utilities.Database;
import moba.server.utilities.config.Config;
import moba.server.exceptions.ClientErrorException;

final public class Timer extends AbstractMessageHandler {
    private final Scheduler scheduler;
    private final Config    config;

    public Timer(Dispatcher dispatcher, Config config, Database database) {
        this.dispatcher = dispatcher;
        this.config = config;

        GlobalTimerData timerData = (GlobalTimerData)config.getSection("globalTimer.globalTimer");
        this.scheduler = new Scheduler(dispatcher, timerData, database);
    }

    @Override
    public int getGroupId() {
        return TimerMessage.GROUP_ID;
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleMsg(Message msg)
    throws ClientErrorException, IOException {
        switch(TimerMessage.fromId(msg.getMessageId())) {
            case GET_GLOBAL_TIMER ->
                dispatcher.sendSingle(
                    new Message(TimerMessage.SET_GLOBAL_TIMER, scheduler.getTimerData()), msg.getEndpoint()
                );

            case SET_GLOBAL_TIMER -> {
                GlobalTimerData timerData = GlobalTimerData.fromJsonObject((Map<String, Object>)msg.getData());
                storeData(timerData);
                scheduler.setTimerData(timerData);
                dispatcher.sendGroup(new Message(TimerMessage.SET_GLOBAL_TIMER, timerData));
            }
        }
    }

    private void storeData(GlobalTimerData timerData)
    throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("globalTimer", timerData);
        config.setSection("globalTimer", map);
        config.writeFile();
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        scheduler.enable((state == HardwareState.AUTOMATIC));
    }
}
