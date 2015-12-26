package messagehandler;

import java.util.logging.*;

import com.*;
import java.util.concurrent.PriorityBlockingQueue;
import messages.*;
import utilities.*;

public class Systems extends MessageHandlerA {
    public enum HardwareStatus {
		ERROR,
		STANDBY,
		POWER_OFF,
		READY
    }

    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected HardwareStatus status = HardwareStatus.ERROR;
    protected SenderI dispatcher = null;
    protected boolean emergencyStop = false;
    protected PriorityBlockingQueue<Message> in = null;

    public Systems(SenderI dispatcher, PriorityBlockingQueue<Message> in) {
        this.dispatcher = dispatcher;
        this.in = in;
    }

    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case EMERGENCY_STOP:
                this.emergencyStop();
                break;

            case EMERGENCY_STOP_CLEARING:
                this.emergencyStopClearing();
                break;

            case GET_HARDWARE_STATE:
                this.dispatcher.dispatch(
                    new Message(
                        MessageType.SET_HARDWARE_STATE,
                        this.status.toString(),
                        msg.getEndpoint()
                    )
                );
                break;

            case SET_HARDWARE_STATE:
                if(this.setHardwareState(
                    HardwareStatus.valueOf((String)msg.getData())
                )) {
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.HARDWARE_STATE_CHANGED,
                            this.status.toString()
                        )
                    );
                }
                break;

            case HARDWARE_SHUTDOWN:
                this.in.add(new Message(MessageType.SERVER_SHUTDOWN));
                break;

            case HARDWARE_RESET:
                this.in.add(new Message(MessageType.SERVER_RESET));
                break;

            case HARDWARE_SWITCH_STANDBY:
                this.setHardwareSwitchStandby();
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    public HardwareStatus getHardwareState() {
        return this.status;
    }

    public boolean getEmergencyStopStatus() {
        return this.emergencyStop;
    }

    protected boolean setHardwareState(HardwareStatus state) {
        switch(state) {
            case READY:
                if(this.emergencyStop) {
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.SYSTEM_NOTICE,
                            new Notice(
                                Notice.NoticeType.ERROR,
                                "Nothalt gedrückt",
                                "Es wurde ein Nothalt ausgelöst"
                            )
                        )
                    );
                    return false;
                }
            case ERROR:
            case STANDBY:
            case POWER_OFF:
                if(this.status == state) {
                    return false;
                }
                this.status = state;
                return true;

            default:
                throw new UnsupportedOperationException(
                    "unknow state <" + state.toString() + ">."
                );
        }
    }

    protected void setHardwareSwitchStandby() {
        switch(this.status) {
            case ERROR:
                return;

            case READY:
            case POWER_OFF:
                this.status = HardwareStatus.STANDBY;
                break;

            case STANDBY:
                if(this.emergencyStop) {
                    this.status = HardwareStatus.POWER_OFF;
                    break;
                }
                this.status = HardwareStatus.READY;
                break;
        }
        this.dispatcher.dispatch(
            new Message(
                MessageType.HARDWARE_STATE_CHANGED,
                this.status.toString()
            )
        );
    }

    public void emergencyStop() {
        if(this.emergencyStop) {
            return;
        }
        this.dispatcher.dispatch(
            new Message(
                MessageType.SYSTEM_NOTICE,
                new Notice(
                    Notice.NoticeType.WARNING,
                    "Nothalt gedrückt",
                    "Es wurde ein Nothalt ausgelöst"
                )
            )
        );
        this.emergencyStop = true;
        if(this.status == HardwareStatus.READY) {
            this.status = HardwareStatus.POWER_OFF;
            this.dispatcher.dispatch(
                new Message(
                    MessageType.HARDWARE_STATE_CHANGED,
                    this.status.toString()
                )
            );
        }
    }

    public void emergencyStopClearing() {
        if(!this.emergencyStop) {
            return;
        }
        this.dispatcher.dispatch(
            new Message(
                MessageType.SYSTEM_NOTICE,
                new Notice(
                    Notice.NoticeType.INFO,
                    "Nothalt freigabe",
                    "Es wurde ein Nothalt freigegeben"
                )
            )
        );
        this.emergencyStop = false;
    }
}