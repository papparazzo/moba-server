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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import java.sql.SQLException;
import java.util.ArrayList;

import moba.server.datatypes.base.DateTime;
import moba.server.datatypes.collections.LayoutMap;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.objects.Position;
import moba.server.datatypes.objects.Symbol;
import moba.server.datatypes.objects.TrackLayoutSymbolData;
import moba.server.messages.AbstractMessageHandler;
import moba.server.repositories.TrackLayoutRepository;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.objects.TrackLayoutInfoData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import moba.server.com.Dispatcher;
import moba.server.utilities.layout.ActiveTrackLayout;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.LayoutMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.layout.TrackLayoutLock;
import moba.server.utilities.logger.Loggable;

public final class Layout extends AbstractMessageHandler implements Loggable {

    private final TrackLayoutRepository repository;

    private final TrackLayoutLock   lock;
    private final ActiveTrackLayout activeLayout;
    private boolean                 isRunning = false;

    public Layout(Dispatcher dispatcher, TrackLayoutRepository repository, ActiveTrackLayout activeLayout, TrackLayoutLock lock)
    throws SQLException {
        this.dispatcher   = dispatcher;
        this.repository   = repository;

        this.activeLayout = activeLayout;
        this.lock         = lock;
        this.lock.resetAll();
    }

    @Override
    public int getGroupId() {
        return LayoutMessage.GROUP_ID;
    }

    @Override
    public void shutdown()
    throws SQLException {
        lock.resetAll();
    }

    @Override
    public void freeResources(long appId)
    throws SQLException {
        lock.resetOwn(appId);
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        isRunning = (state == HardwareState.AUTOMATIC || state == HardwareState.AUTOMATIC_HALT);
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, SQLException, IOException {
        switch(LayoutMessage.fromId(msg.getMessageId())) {
            case GET_LAYOUTS_REQ          -> getLayouts(msg);
            case GET_LAYOUT_REQ           -> getLayout(msg, true);
            case GET_LAYOUT_READ_ONLY_REQ -> getLayout(msg, false);
            case DELETE_LAYOUT            -> deleteLayout(msg);
            case CREATE_LAYOUT            -> createLayout(msg);
            case UPDATE_LAYOUT            -> updateLayout(msg);
            case UNLOCK_LAYOUT            -> unlockLayout(msg);
            case LOCK_LAYOUT              -> lockLayout(msg);
            case SAVE_LAYOUT              -> saveLayout(msg);
        }
    }

    private void getLayouts(Message msg)
    throws SQLException {
        ArrayList<TrackLayoutInfoData> arraylist = repository.getLayouts(activeLayout.getActiveLayout());
        dispatcher.sendSingle(new Message(LayoutMessage.GET_LAYOUTS_RES, arraylist), msg.getEndpoint());
    }

    private void deleteLayout(Message msg)
    throws SQLException, ClientErrorException, IOException {
        long id = (long)msg.getData();
        lock.isLockedByApp(msg.getEndpoint().getAppId(), id);

        if(isRunning && id == activeLayout.getActiveLayout()) {
            throw new ClientErrorException(
                ClientError.OPERATION_NOT_ALLOWED,
                "cannot delete active layout <" + id + "> while running"
            );
        }
        repository.deleteLayout(id, msg.getEndpoint().getAppId());
        if(id == activeLayout.getActiveLayout()) {
            activeLayout.setActiveLayout(0);
        }
        dispatcher.sendGroup(new Message(LayoutMessage.DELETE_LAYOUT, id));
    }

    @SuppressWarnings("unchecked")
    private void createLayout(Message msg)
    throws SQLException, IOException {
        Map<String, Object> map = (Map<String, Object>)msg.getData();
        boolean isActive = (boolean)map.get("active");
        long    currAppId = msg.getEndpoint().getAppId();

        TrackLayoutInfoData tl = new TrackLayoutInfoData(
            (String)map.get("name"),
            (String)map.get("description"),
            currAppId,
            isActive
        );

        long id = repository.createLayout(tl, currAppId);

        if(isActive) {
            activeLayout.setActiveLayout(id);
        }
        tl.setId(id);
        dispatcher.sendGroup(new Message(LayoutMessage.CREATE_LAYOUT, tl));
    }

    @SuppressWarnings("unchecked")
    private void updateLayout(Message msg)
    throws SQLException, ClientErrorException, IOException {
        Map<String, Object> map = (Map<String, Object>)msg.getData();

        long id = (Long)map.get("id");
        lock.isLockedByApp(msg.getEndpoint().getAppId(), id);

        TrackLayoutInfoData tl;
        boolean active = (boolean)map.get("active");
        long appId = msg.getEndpoint().getAppId();
        tl = new TrackLayoutInfoData(
            id,
            (String)map.get("name"),
            (String)map.get("description"),
            appId,
            active,
            new DateTime(),
            repository.getCreationDate(id)
        );

        repository.updateLayout(tl, id, appId);
        if(active) {
            activeLayout.setActiveLayout(id);
        }
        dispatcher.sendGroup(new Message(LayoutMessage.UPDATE_LAYOUT, tl));
    }

    private void unlockLayout(Message msg)
    throws ClientErrorException, SQLException {
        long id = activeLayout.getActiveLayout((Long)msg.getData());
        lock.unlock(msg.getEndpoint().getAppId(), id);
        dispatcher.sendGroup(new Message(LayoutMessage.UNLOCK_LAYOUT, id));
    }

    private void lockLayout(Message msg)
    throws ClientErrorException, SQLException {
        long id = activeLayout.getActiveLayout((Long)msg.getData());
        lock.tryLock(msg.getEndpoint().getAppId(), id);
        dispatcher.sendGroup(new Message(LayoutMessage.LOCK_LAYOUT, id));
    }

    private void getLayout(Message msg, boolean tryLock)
    throws SQLException, ClientErrorException {
        long id = activeLayout.getActiveLayout((Long)msg.getData());

        if(tryLock) {
            lock.tryLock(msg.getEndpoint().getAppId(), id);
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("symbols", repository.getLayout(id));

        dispatcher.sendSingle(new Message(LayoutMessage.GET_LAYOUT_RES, map), msg.getEndpoint());
    }

    @SuppressWarnings("unchecked")
    private void saveLayout(Message msg)
    throws SQLException, ClientErrorException {

        Map<String, Object> map = (Map<String, Object>)msg.getData();
        long id = activeLayout.getActiveLayout((Long)map.get("id"));

        if(!lock.isLockedByApp(msg.getEndpoint().getAppId(), id)) {
            throw new ClientErrorException(ClientError.DATASET_NOT_LOCKED, "layout <" + id + "> not locked");
        }

        LayoutMap container = new LayoutMap();

        ArrayList<Object> arrayList = (ArrayList<Object>)map.get("symbols");

        for(Object item : arrayList) {
            Map<String, Object> symbol = (Map<String, Object>)item;

            container.put(
                new Position(
                    (long)symbol.get("xPos"),
                    (long)symbol.get("yPos")
                ),
                new TrackLayoutSymbolData(
                    (Long)symbol.get("id"),
                    new Symbol((int)symbol.get("symbol"))
                )
            );
        }

        repository.saveLayout(id, container);

        dispatcher.sendGroup(new Message(LayoutMessage.LAYOUT_CHANGED, id));
    }
}
