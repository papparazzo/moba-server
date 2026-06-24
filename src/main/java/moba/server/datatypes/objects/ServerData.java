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

package moba.server.datatypes.objects;

import moba.server.application.ServerApplication;
import moba.server.com.Dispatcher;
import moba.server.datatypes.base.Date;
import moba.server.datatypes.base.DateTime;
import moba.server.datatypes.base.Version;

public record ServerData(
    String appName,
    Version version,
    Date buildDate,
    DateTime startTime,
    int maxClients,
    int connectedClients,
    String osArch,
    String osName,
    String osVersion,
    String fwType,
    String fwVersion
) {
    public static ServerData from(ServerApplication app, Dispatcher dispatcher) {
        return new ServerData(
            app.getAppName(),
            app.getVersion(),
            new Date(app.getBuildDate()),
            new DateTime(app.getStartTime()),
            app.getMaxClients(),
            dispatcher.getEndPointsCount(),
            java.lang.System.getProperty("os.arch", ""),
            java.lang.System.getProperty("os.name", ""),
            java.lang.System.getProperty("os.version", ""),
            java.lang.System.getProperty("java.vm.vendor", ""),
            java.lang.System.getProperty("java.version", "")
        );
    }
}
