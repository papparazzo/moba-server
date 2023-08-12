/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2023 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.datatypes.objects.helper;

import java.io.IOException;
import java.util.HashMap;
import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.messages.Message;
import moba.server.messages.messageType.LayoutMessage;
import moba.server.utilities.config.Config;
import moba.server.utilities.config.ConfigException;
import moba.server.utilities.exceptions.ErrorException;

public class ActiveLayout {

    protected Long activeLayout;
    protected Dispatcher dispatcher;
    protected Config config;

    public ActiveLayout(Dispatcher dispatcher, Config config) {
        this.config = config;
        this.dispatcher = dispatcher;
        activeLayout = (Long)config.getSection("trackLayout.activeTrackLayoutId");
    }

    public long getActiveLayout() {
        if(activeLayout == null) {
            //FIXME switch to nullable...
            return -1;
        }
        return activeLayout;
    }

    public long getActiveLayout(Object defaultId)
    throws ErrorException {
        if(defaultId != null) {
            return (long)defaultId;
        }
        if(activeLayout != null) {
            return activeLayout;
        }
        throw new ErrorException(ErrorId.NO_DEFAULT_GIVEN, "no default-track-layout given");
    }

    public void setActiveLayout(long activeLayout)
    throws ErrorException {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("activeTrackLayoutId", activeLayout);
            config.setSection("trackLayout", map);
            config.writeFile();
        } catch(ConfigException | IOException e) {
            //dispatcher.dispatch(new Message(ClientMessage.ERROR, e.getErrorData(), msg.getEndpoint()));
            throw new ErrorException(ErrorId.UNKNOWN_ERROR, e.getMessage());
        }
        dispatcher.dispatch(new Message(LayoutMessage.DEFAULT_LAYOUT_CHANGED, activeLayout));
        this.activeLayout = activeLayout;
    }
}
