package moba.server.datatypes.objects;

import moba.server.com.Endpoint;
import moba.server.utilities.CheckedEnum;
import moba.server.messages.Message;
import moba.server.utilities.exceptions.ClientErrorException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class IncidentData {
    public enum Level {
        CRITICAL,  // schwerwiegende Ausnahme, moba-server wird neu gestartet
        ERROR,     // Fehlerhafte Nachrichten vom Client: Ungültige Nachrichten-Id, Falscher Nachrichtenaufbau
        WARNING,   // Nothalt ausgelöst, Probleme im Automatikbetrieb
        NOTICE
    }

    public enum Type {
        EXCEPTION,
        STATUS_CHANGE,     // Statusänderung
        NOTICE,
        CLIENT,
        SERVER
    }

    private final Level  level;
    private final Type   type;
    private final String caption;
    private final String message;
    private final String origin;
    private final String timeStamp;

    public IncidentData(Level level, Type type, String caption, String message, String origin) {
        this.level = level;
        this.type = type;
        this.caption = caption;
        this.message = message;
        this.origin = origin;
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS ").format(new Date());
    }

    public IncidentData(Level level, Type type, String caption, String message, Endpoint origin) {
        this(level, type, caption, message, origin.toString());
    }

    @SuppressWarnings("unchecked")
    public IncidentData(Message msg)
    throws ClientErrorException {
        Map<String, String> map = (Map<String, String>)msg.getData();

        this.level = CheckedEnum.getFromString(Level.class, map.get("level"));
        this.type = CheckedEnum.getFromString(Type.class, map.get("type"));
        this.caption = map.get("caption");
        this.message = map.get("message");
        this.origin = msg.getEndpoint().toString();
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS ").format(new Date());
    }

    public IncidentData(Throwable throwable) {
        this(Type.EXCEPTION, throwable);
    }

    public IncidentData(IncidentData.Type type, Throwable throwable) {
        this(type, "", throwable);
    }

    public IncidentData(IncidentData.Type type, String caption, Throwable throwable) {
        this(
            Level.CRITICAL,
            type,
            throwable.getClass().getName() + "-Exception " + caption,
            throwable.toString(),
            throwable.getStackTrace()[0].toString()
        );
    }

    public IncidentData(IncidentData.Type type, String caption, Throwable throwable, Endpoint ep) {
        this(type, (caption + " [" + ep.toString() + "]").trim(), throwable);
    }

    public String toString() {
        return type.toString() + " " + caption + ": " + message + " @" + origin;
    }

    public Level getLevel() {
        return level;
    }

    public Type getType() {
        return type;
    }

    public String getCaption() {
        return caption;
    }

    public String getMessage() {
        return message;
    }

    public String getOrigin() {
        return origin;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
