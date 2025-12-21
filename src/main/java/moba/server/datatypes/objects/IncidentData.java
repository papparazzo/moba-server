/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2022 Stefan Paproth <pappi-@gmx.de>
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

import moba.server.com.Endpoint;
import moba.server.datatypes.base.TimeStamp;
import moba.server.datatypes.enumerations.IncidentLevel;
import moba.server.datatypes.enumerations.IncidentType;
import moba.server.utilities.CheckedEnum;
import moba.server.messages.Message;
import moba.server.exceptions.ClientErrorException;

import java.util.Map;
import java.util.Objects;

public class IncidentData {

    private final IncidentLevel  level;
    private final IncidentType type;
    private final String caption;
    private final String message;
    private final String source;
    private final TimeStamp timeStamp;
    private final Endpoint origin;

    @SuppressWarnings("unchecked")
    public IncidentData(Message msg)
    throws ClientErrorException {
        Map<String, String> map = (Map<String, String>)msg.getData();

        this.level = CheckedEnum.getFromString(IncidentLevel.class, map.get("level"));
        this.type = CheckedEnum.getFromString(IncidentType.class, map.get("type"));
        this.caption = map.get("caption");
        this.message = map.get("message");
        this.source = map.get("source");
        this.origin = msg.getEndpoint();
        this.timeStamp = new TimeStamp();
    }

    public IncidentData(IncidentLevel level, IncidentType type, String caption, String message, String source, Endpoint origin) {
        this.level = level;
        this.type = type;
        this.caption = caption;
        this.message = message;
        this.source = source;
        this.origin = origin;
        this.timeStamp = new TimeStamp();
    }

    public IncidentData(IncidentLevel level, IncidentType type, String caption, String message, String source) {
        this(level, type, caption, message, source, null);
    }

    public IncidentData(IncidentType type, Throwable throwable, Endpoint origin) {
        this(
            IncidentLevel.CRITICAL,
            type,
            getClassName(throwable),
            throwable.getMessage(),
            throwable.getStackTrace()[0].toString(),
            origin
        );
    }

    public IncidentData(Throwable throwable) {
        this(IncidentType.EXCEPTION, throwable);
    }

    public IncidentData(IncidentType type, Throwable throwable) {
        this(type, throwable, null);
    }

    public String toString() {
        return
            type.toString() + " " +
            caption + ": " +
            message + " @" + Objects.requireNonNullElse(origin, "[moba-server]");
    }

    public IncidentLevel getLevel() {
        return level;
    }

    public IncidentType getType() {
        return type;
    }

    public String getCaption() {
        return caption;
    }

    public String getMessage() {
        return message;
    }

    public Endpoint getOrigin() {
        return origin;
    }

    public TimeStamp getTimeStamp() {
        return timeStamp;
    }

    public String getSource() {
        return source;
    }

    private static String getClassName(Throwable throwable) {
        String str = throwable.getClass().getName();
        return str.substring(str.lastIndexOf(".") + 1);
    }
}
