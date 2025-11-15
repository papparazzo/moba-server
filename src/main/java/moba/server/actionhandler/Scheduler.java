/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.actionhandler;

import moba.server.com.Dispatcher;
import moba.server.datatypes.objects.GlobalTimerData;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.TimerMessage;
import moba.server.timedaction.TimedActionInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * https://www.laenderdaten.info/Europa/Deutschland/sonnenuntergang.php
 *
 * ab 04:00 Uhr Sonnenaufgang
 * ab 05:00 Uhr Tag
 * ab 21:00 Uhr Sonnenuntergang
 * ab 22:00 Uht Nacht
 */
final public class Scheduler implements Runnable {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Dispatcher                 dispatcher;
    private GlobalTimerData                  timerData;

    private final List<TimedActionInterface> timedActions = new ArrayList<>();

    private volatile boolean                 isRunning = false;

    public Scheduler(Dispatcher dispatcher, GlobalTimerData timerData) {
        this.dispatcher = dispatcher;

        this.scheduler.scheduleWithFixedDelay(this, 1, 1, TimeUnit.SECONDS);
        this.timerData = Objects.requireNonNullElseGet(timerData, GlobalTimerData::new);
    }

    public void addTimedAction(TimedActionInterface timedAction) {
        timedActions.add(timedAction);
    }

    public void enable(boolean enable) {
        isRunning = enable;
    }

    public void shutdown() {
        isRunning = false;
        scheduler.shutdown();
    }

    public void setTimerData(GlobalTimerData timerData) {
        this.timerData = timerData;
    }

    public GlobalTimerData getTimerData() {
        return timerData;
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

            for(TimedActionInterface timedAction : timedActions) {
                timedAction.trigger(timerData.getModelTime(), timerData.getMultiplicator());
            }
        } catch(Throwable ignored) {

        }
    }
}
