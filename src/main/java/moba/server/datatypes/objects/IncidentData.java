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
        STATUS_CHANGE, // Statusänderung
        NOTICE,
        CLIENT_ERROR,
        SERVER_NOTICE         // Server-spezifische Nachrichten (z.B. Fehlermeldungen)
    }

    private final Level  level;
    private final Type   type;
    private final String caption;
    private final String message;
    private final String source;
    private final String timeStamp;
    private final Endpoint origin;

    @SuppressWarnings("unchecked")
    public IncidentData(Message msg)
    throws ClientErrorException {
        Map<String, String> map = (Map<String, String>)msg.getData();

        this.level = CheckedEnum.getFromString(Level.class, map.get("level"));
        this.type = CheckedEnum.getFromString(Type.class, map.get("type"));
        this.caption = map.get("caption");
        this.message = map.get("message");
        this.source = map.get("source");
        this.origin = msg.getEndpoint();
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS ").format(new Date());
    }

    public IncidentData(Level level, Type type, String caption, String message, String source, Endpoint origin) {
        this.level = level;
        this.type = type;
        this.caption = caption;
        this.message = message;
        this.source = source;
        this.origin = origin;
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS ").format(new Date());
    }

    public IncidentData(Level level, Type type, String caption, String message, String source) {
        this(level, type, caption, message, source, null);
    }

    public IncidentData(IncidentData.Type type, Throwable throwable, Endpoint origin) {
        this(
            Level.CRITICAL,
            type,
            getClassName(throwable) + "-Exception ",
            throwable.getMessage(),
            throwable.getStackTrace()[0].toString(),
            origin
        );
    }

    public IncidentData(Throwable throwable) {
        this(Type.EXCEPTION, throwable);
    }

    public IncidentData(IncidentData.Type type, Throwable throwable) {
        this(type, throwable, null);
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
}
