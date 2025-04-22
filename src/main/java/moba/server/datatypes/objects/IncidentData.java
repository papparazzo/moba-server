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
import moba.server.datatypes.enumerations.IncidentLevel;
import moba.server.datatypes.enumerations.IncidentType;
import moba.server.utilities.CheckedEnum;
import moba.server.messages.Message;
import moba.server.utilities.exceptions.ClientErrorException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class IncidentData {

    private final IncidentLevel  level;
    private final IncidentType type;
    private final String caption;
    private final String message;
    private final String source;
    private final String timeStamp;
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
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").format(new Date());
    }

    public IncidentData(IncidentLevel level, IncidentType type, String caption, String message, String source, Endpoint origin) {
        this.level = level;
        this.type = type;
        this.caption = caption;
        this.message = message;
        this.source = source;
        this.origin = origin;
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS ").format(new Date());
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
        return type.toString() + " " + caption + ": " + message + " @" + origin;
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getSource() {
        return source;
    }

    private static String getClassName(Throwable throwable) {
        String str = throwable.getClass().getName();
        return str.substring(str.lastIndexOf(".") + 1);
    }

/*

    private void inferCaller() {

        // Skip all frames until we have found the first logger frame.
        Optional<StackWalker.StackFrame> frame = new LogRecord().CallerFinder().get();
        frame.ifPresent(f -> {
            setSourceClassName(f.getClassName());
            setSourceMethodName(f.getMethodName());
        });

        // We haven't found a suitable frame, so just punt.  This is
        // OK as we are only committed to making a "best effort" here.
    }


        static final class CallerFinder implements Predicate<StackWalker.StackFrame> {
        private static final StackWalker WALKER;
        static {
            final PrivilegedAction<StackWalker> action =
                () -> StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            WALKER = AccessController.doPrivileged(action);
        }

        /**
         * Returns StackFrame of the caller's frame.
         * @return StackFrame of the caller's frame.
         * /
        Optional<StackWalker.StackFrame> get() {
            return WALKER.walk((s) -> s.filter(this).findFirst());
        }

        private boolean lookingForLogger = true;
        /**
         * Returns true if we have found the caller's frame, false if the frame
         * must be skipped.
         *
         * @param t The frame info.
         * @return true if we have found the caller's frame, false if the frame
         * must be skipped.
         * /
        @Override
        public boolean test(StackWalker.StackFrame t) {
            final String cname = t.getClassName();
            // We should skip all frames until we have found the logger,
            // because these frames could be frames introduced by e.g. custom
            // sub classes of Handler.
            if (lookingForLogger) {
                // the log record could be created for a platform logger
                lookingForLogger = !isLoggerImplFrame(cname);
                return false;
            }
            // Continue walking until we've found the relevant calling frame.
            // Skips logging/logger infrastructure.
            return !isFilteredFrame(t);
        }

        private boolean isLoggerImplFrame(String cname) {
            return (cname.equals("java.util.logging.Logger") ||
                cname.startsWith("sun.util.logging.PlatformLogger"));
        }
    }
*/

}
