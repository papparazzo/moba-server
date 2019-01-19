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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package messages;

public enum MessageType {
    //---------------------------------------------------
    // Base (Intern)
    //---------------------------------------------------
    SERVER_SHUTDOWN(MessageGroup.BASE, MessageClass.INTERN),
    SERVER_RESET(MessageGroup.BASE, MessageClass.INTERN),
    FREE_RESOURCES(MessageGroup.BASE, MessageClass.INTERN),
    SET_HARDWARE_STATE(MessageGroup.BASE, MessageClass.INTERN),

    //---------------------------------------------------
    // Client
    //---------------------------------------------------
    VOID(MessageGroup.CLIENT, MessageClass.SINGLE),
    ECHO_REQ(MessageGroup.CLIENT, MessageClass.SINGLE),
    ECHO_RES(MessageGroup.CLIENT, MessageClass.SINGLE),
    ERROR(MessageGroup.CLIENT, MessageClass.SINGLE),
    START(MessageGroup.CLIENT, MessageClass.SINGLE),
    CONNECTED(MessageGroup.CLIENT, MessageClass.SINGLE),
    CLOSE(MessageGroup.CLIENT, MessageClass.SINGLE),
    SHUTDOWN(MessageGroup.CLIENT, MessageClass.SINGLE),
    RESET(MessageGroup.CLIENT, MessageClass.SINGLE),
    SELF_TESTING(MessageGroup.CLIENT, MessageClass.SINGLE),

    //---------------------------------------------------
    // Server
    //---------------------------------------------------
    MAX_CLIENT_COUNT(MessageGroup.SERVER, MessageClass.GROUP),
    NEW_CLIENT_STARTED(MessageGroup.SERVER, MessageClass.GROUP),
    CLIENT_CLOSED(MessageGroup.SERVER, MessageClass.GROUP),
    RESET_CLIENT(MessageGroup.SERVER, MessageClass.SINGLE),
    INFO_REQ(MessageGroup.SERVER, MessageClass.SINGLE),
    INFO_RES(MessageGroup.SERVER, MessageClass.SINGLE),
    CON_CLIENTS_REQ(MessageGroup.SERVER, MessageClass.SINGLE),
    CON_CLIENTS_RES(MessageGroup.SERVER, MessageClass.SINGLE),
    SELF_TESTING_CLIENT(MessageGroup.SERVER, MessageClass.SINGLE),

    //---------------------------------------------------
    // GlobalTimer
    //---------------------------------------------------
    GLOBAL_TIMER_EVENT(MessageGroup.TIMER, MessageClass.GROUP, MessagePriority.REAL_TIME),
    GET_GLOBAL_TIMER(MessageGroup.TIMER, MessageClass.SINGLE),
    SET_GLOBAL_TIMER(MessageGroup.TIMER, MessageClass.GROUP),
    GET_COLOR_THEME(MessageGroup.TIMER, MessageClass.SINGLE),
    SET_COLOR_THEME(MessageGroup.TIMER, MessageClass.GROUP),
    COLOR_THEME_EVENT(MessageGroup.TIMER, MessageClass.GROUP),

    //---------------------------------------------------
    // Environment
    //---------------------------------------------------
    GET_ENVIRONMENT(MessageGroup.ENV, MessageClass.SINGLE),
    SET_ENVIRONMENT(MessageGroup.ENV, MessageClass.GROUP),
    GET_AMBIENCE(MessageGroup.ENV, MessageClass.SINGLE),
    SET_AMBIENCE(MessageGroup.ENV, MessageClass.GROUP),
    GET_AMBIENT_LIGHT(MessageGroup.ENV, MessageClass.SINGLE),
    SET_AMBIENT_LIGHT(MessageGroup.ENV, MessageClass.GROUP),

    //---------------------------------------------------
    // Interface
    //---------------------------------------------------
    SET_CONNECTIVITY(MessageGroup.INTERFACE, MessageClass.SINGLE),

    //---------------------------------------------------
    // System
    //---------------------------------------------------
    SET_AUTOMATIC_MODE(MessageGroup.SYSTEM, MessageClass.SINGLE, MessagePriority.REAL_TIME),
    SET_EMERGENCY_STOP(MessageGroup.SYSTEM, MessageClass.SINGLE, MessagePriority.REAL_TIME),
    SET_STANDBY_MODE(MessageGroup.SYSTEM, MessageClass.SINGLE),
    GET_HARDWARE_STATE(MessageGroup.SYSTEM),
    HARDWARE_STATE_CHANGED(MessageGroup.SYSTEM, MessageClass.GROUP),
    HARDWARE_SHUTDOWN(MessageGroup.SYSTEM, MessageClass.SINGLE),
    HARDWARE_RESET(MessageGroup.SYSTEM, MessageClass.SINGLE),

    //---------------------------------------------------
    // Layouts
    //---------------------------------------------------
    GET_LAYOUTS_REQ(MessageGroup.LAYOUTS, MessageClass.SINGLE),
    GET_LAYOUTS_RES(MessageGroup.LAYOUTS, MessageClass.SINGLE),
    DEL_LAYOUT(MessageGroup.LAYOUTS, MessageClass.SINGLE),
    LAYOUT_DELETED(MessageGroup.LAYOUTS, MessageClass.GROUP),
    CREATE_LAYOUT_REQ(MessageGroup.LAYOUTS, MessageClass.SINGLE),
    CREATE_LAYOUT_RES(MessageGroup.LAYOUTS, MessageClass.SINGLE),
    LAYOUT_CREATED(MessageGroup.LAYOUTS, MessageClass.GROUP),
    UPDATE_LAYOUT(MessageGroup.LAYOUTS, MessageClass.SINGLE),
    LAYOUT_UPDATED(MessageGroup.LAYOUTS, MessageClass.GROUP),
    UNLOCK_LAYOUT(MessageGroup.LAYOUTS, MessageClass.SINGLE),
    LAYOUT_UNLOCKED(MessageGroup.LAYOUTS, MessageClass.GROUP),

    //---------------------------------------------------
    // Layout
    //---------------------------------------------------
    GET_LAYOUT_REQ(MessageGroup.LAYOUT, MessageClass.SINGLE),
    GET_LAYOUT_RES(MessageGroup.LAYOUT, MessageClass.SINGLE),
    SAVE_LAYOUT(MessageGroup.LAYOUT, MessageClass.SINGLE),

    //---------------------------------------------------
    // GUI
    //---------------------------------------------------
    SYSTEM_NOTICE(MessageGroup.GUI, MessageClass.GROUP);

    public enum MessageGroup {
        BASE,
        CLIENT,
        SERVER,
        TIMER,
        ENV,
        INTERFACE,
        SYSTEM,
        LAYOUTS,
        LAYOUT,
        GUI
    }

    public enum MessageClass {
        INTERN,
        SINGLE,
        GROUP
    }

    public enum MessagePriority {
        REAL_TIME(0),
        HIGHEST(250),
        VERY_HIGHT(500),
        HIGHT(1000),
        NORMAL(2500),
        LOW(5000),
        VERY_LOW(10000),
        LOWEST(25000);

        private final long offset;

        MessagePriority(long offset) {
            this.offset = offset;
        }

        public long getOffset() {
            return offset;
        }
    }

    private final MessageGroup    grp;
    private final MessagePriority pty;
    private final MessageClass    cls;

    MessageType(MessageGroup grp) {
        this(grp, MessageClass.INTERN, MessagePriority.NORMAL);
    }

    MessageType(MessageGroup grp, MessageClass cls) {
        this(grp, cls, MessagePriority.NORMAL);
    }

    MessageType(MessageGroup grp, MessageClass cls, MessagePriority pty) {
        this.grp = grp;
        this.pty = pty;
        this.cls = cls;
    }

    public MessageGroup getMessageGroup() {
        return grp;
    }

    public MessagePriority getMessagePriority() {
        return pty;
    }

    public MessageClass getMessageClass() {
        return cls;
    }
}