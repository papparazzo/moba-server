/*
 *  common
 *
 *  Copyright (C) 2013 Stefan Paproth <pappi-@gmx.de>
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
package com;

import java.io.*;
import java.util.logging.*;
import json.*;
import messages.*;
import messages.Message;

public class Transmitter implements SenderI {

    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected JSONMessageEncoder encoder = null;

    public Transmitter(JSONMessageEncoder encoder)
    throws IOException {
        this.encoder = encoder;
    }

    @Override
    public boolean dispatch(Message msg) {
        try {
            if(msg == null) {
                Transmitter.logger.log(Level.SEVERE, "msg is null!");
                return false;
            }

            Transmitter.logger.log(
                Level.INFO,
                "try to send message <{0}>",
                new Object[]{msg.getMsgType().toString()}
            );

            if(
                msg.getMsgType().getMessageClass() ==
                MessageType.MessageClass.INTERN
            ) {
                Transmitter.logger.log(
                    Level.WARNING,
                    "msg-class is intern!"
                );
                return false;
            }
            this.encoder.encodeMsg(msg);
            return true;
        } catch(IOException | JSONException e) {
            Transmitter.logger.log(
                Level.WARNING,
                "<{0}>",
                new Object[]{e.toString()}
            );
        }
        return false;
    }
}
