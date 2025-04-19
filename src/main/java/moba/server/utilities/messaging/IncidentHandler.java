package moba.server.utilities.messaging;

import moba.server.com.Dispatcher;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.Message;
import moba.server.messages.messageType.MessagingMessage;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.logging.Level;
import java.util.logging.Logger;

final public class IncidentHandler {
    private final Logger logger;
    private final Dispatcher dispatcher;
    CircularFifoQueue<IncidentData> list;

    public IncidentHandler(Logger logger, Dispatcher dispatcher, CircularFifoQueue<IncidentData> list) {
        this.logger = logger;
        this.dispatcher = dispatcher;
        this.list = list;
    }

    public synchronized void add(IncidentData incident) {
        logger.log(convertLevel(incident.getLevel()), incident.toString());
        list.add(incident);
        dispatcher.broadcast(new Message(MessagingMessage.NOTIFY_INCIDENT, incident));
    }

    private Level convertLevel(IncidentData.Level level) {
        return switch(level) {
            case CRITICAL, ERROR -> Level.SEVERE;
            case WARNING -> Level.WARNING;
            case NOTICE -> Level.INFO;
        };
    }
}
