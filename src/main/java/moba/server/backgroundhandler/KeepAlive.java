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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.backgroundhandler;

import moba.server.com.Dispatcher;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.ClientMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*
 * After a certain time (normally after 7200 seconds / 2h) tcp-connections run into a timeout
 * see https://serverfault.com/questions/216956/how-to-check-tcp-timeout-in-linux-macos
 * and https://stackoverflow.com/questions/13085676/tcp-socket-no-connection-timeout for further information
 * to prevent this, KeepAlive sends a "ping" after "intervall"-Seconds to each connected client
 * https://en.wikipedia.org/wiki/Keepalive#TCP_keepalive and
 */
final public class KeepAlive implements BackgroundHandlerInterface {
    private final Dispatcher dispatcher;
    private final long                     intervall;
    private final Logger                   logger;
    private Future<?>                      future;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public KeepAlive(Dispatcher dispatcher, long intervall, Logger logger) {
        this.dispatcher = dispatcher;
        this.intervall   = intervall;
        this.logger      = logger;
    }

    public void halt() {
        future.cancel(true);
        logger.info("keepalive-thread stopped.");
    }

    public void start() {
        Runnable runnable = ()->dispatcher.sendAll(new Message(ClientMessage.PING));
        future = executor.scheduleWithFixedDelay(runnable, intervall, intervall, TimeUnit.SECONDS);
        logger.info("keepalive-thread started");
    }
}
