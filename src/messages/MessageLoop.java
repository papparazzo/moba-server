package messages;

import com.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;

import messages.MessageType.*;

public class MessageLoop {
    protected static final Logger logger =
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected Map<MessageGroup, MessageHandlerA> handlers = new HashMap<>();
    protected Dispatcher dispatcher = null;

    public MessageLoop(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void addHandler(MessageGroup msgGroup, MessageHandlerA msgHandler) {
        msgHandler.init();
        this.handlers.put(msgGroup, msgHandler);
    }

    public Set<MessageGroup> getRegisteredHandlers(){
        return this.handlers.keySet();
    }

    public boolean loop(PriorityBlockingQueue<Message> in)
    throws InterruptedException {
        while(true) {
            Message msg = in.take();
            MessageLoop.logger.log(
                Level.INFO,
                "handle msg <{0}> from <{1}>",
                new Object[]{msg.getMsgType(), msg.getEndpoint()}
            );

            if(msg.getMsgType().getMessageGroup() == MessageGroup.BASE) {
                switch(msg.getMsgType()) {
                    case SERVER_RESET:
                        in.clear();
                        this.resetHandler();
                        return true;

                    case SERVER_SHUTDOWN:
                        this.shutdownHandler();
                        return false;

                    case FREE_RESOURCES:
                        this.freeResources((long)msg.getData());
                        continue;

                }
            }

            if(this.handlers.containsKey(msg.getMsgType().getMessageGroup())) {
                this.handlers.get(
                    msg.getMsgType().getMessageGroup()
                ).handleMsg(msg);
                continue;
            }

            MessageLoop.logger.log(
                Level.SEVERE,
                "handler for msg-group <{0}> was not registered!",
                new Object[]{msg.getMsgType().getMessageGroup()}
            );
        }
    }

    protected void freeResources(long id) {
        Iterator<MessageGroup> iter = this.handlers.keySet().iterator();

        while(iter.hasNext()) {
            this.handlers.get(iter.next()).freeResources(id);
        }
    }

    protected void resetHandler() {
        for(Endpoint ep : this.dispatcher.getEndpoints()) {
            this.dispatcher.dispatch(
                new Message(MessageType.CLIENT_RESET, null, ep)
            );
        }
        Iterator<MessageGroup> iter = this.handlers.keySet().iterator();
        while(iter.hasNext()) {
            this.handlers.get(iter.next()).reset();
        }
    }

    protected void shutdownHandler() {
        for(Endpoint ep : this.dispatcher.getEndpoints()) {
            this.dispatcher.dispatch(
                    new Message(MessageType.CLIENT_SHUTDOWN, null, ep)
            );
        }
        Iterator<MessageGroup> iter = this.handlers.keySet().iterator();
        while(iter.hasNext()) {
            this.handlers.get(iter.next()).shutdown();
        }
    }
}
