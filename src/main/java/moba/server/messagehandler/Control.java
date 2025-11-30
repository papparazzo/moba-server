/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2020 Stefan Paproth <pappi-@gmx.de>
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
import java.util.Map;

import moba.server.com.Dispatcher;
import moba.server.datatypes.collections.BlockContactDataMap;
import moba.server.datatypes.collections.SwitchStateMap;
import moba.server.datatypes.collections.TrainList;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.objects.*;
import moba.server.messages.AbstractMessageHandler;
import moba.server.repositories.BlockListRepository;
import moba.server.repositories.SwitchStateRepository;
import moba.server.repositories.TrainlistRepository;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.ControlMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.layout.ActiveTrackLayout;
import moba.server.utilities.layout.TrackLayoutLock;
import moba.server.utilities.logger.Loggable;

final public class Control extends AbstractMessageHandler implements Loggable {
    private final BlockListRepository blocklistRepository;

    private final SwitchStateRepository switchStateRepository;

    private final ActiveTrackLayout activeLayout;

    private final TrackLayoutLock lock;

    private final TrainlistRepository trainlistRepository;

    public Control(
        Dispatcher dispatcher,
        BlockListRepository blocklistRepository,
        SwitchStateRepository switchStateRepository,
        TrainlistRepository trainlistRepository,
        ActiveTrackLayout activeLayout,
        TrackLayoutLock lock
    ) throws SQLException {
        this.switchStateRepository = switchStateRepository;
        this.blocklistRepository = blocklistRepository;
        this.dispatcher = dispatcher;
        this.trainlistRepository = trainlistRepository;

        this.activeLayout = activeLayout;
        this.lock         = lock;
        this.lock.resetAll();
    }

    @Override
    public int getGroupId() {
        return ControlMessage.GROUP_ID;
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
    public void handleMsg(Message msg)
    throws ClientErrorException, SQLException {
        switch(ControlMessage.fromId(msg.getMessageId())) {
            case GET_BLOCK_LIST_REQ        -> getBlockList(msg);
            case SAVE_BLOCK_LIST           -> saveBlockList(msg);
            case GET_SWITCH_STAND_LIST_REQ -> getSwitchStateList(msg);
            case GET_TRAIN_LIST_REQ        -> getTrainList(msg);

        }
    }

    private void getBlockList(Message msg)
    throws SQLException {
        long id = (long)msg.getData();

        BlockContactDataMap blockContactDataMap = blocklistRepository.getBlockList(id);
        dispatcher.sendSingle(new Message(ControlMessage.GET_BLOCK_LIST_RES, blockContactDataMap), msg.getEndpoint());
    }

    @SuppressWarnings("unchecked")
    private void saveBlockList(Message msg)
    throws SQLException, ClientErrorException {

        Map<String, Object> map = (Map<String, Object>)msg.getData();
        long id = (long)map.get("id");

        if(!lock.isLockedByApp(msg.getEndpoint().getAppId(), id)) {
            throw new ClientErrorException(ClientError.DATASET_NOT_LOCKED, "layout <" + id + "> not locked");
        }

        BlockContactDataMap container = new BlockContactDataMap();

        ArrayList<Object> arrayList = (ArrayList<Object>)map.get("symbols");

        for(Object item : arrayList) {
            Map<String, Object> symbol = (Map<String, Object>)item;

            container.put(
                (long)symbol.get("id"),
                new BlockContactData(
                    new PortAddressData(
                        (int)symbol.get("controllerAddr"),
                        (int)symbol.get("port")
                    ),
                    new PortAddressData(
                        (int)symbol.get("controllerAddr"),
                        (int)symbol.get("port")
                    ),
                    null
                )
            );
        }
        blocklistRepository.saveBlockList(id, container);

        //dispatcher.dispatch(new IncidentData(LayoutMessage.LAYOUT_CHANGED, map));
    }

    private void getSwitchStateList(Message msg)
    throws SQLException, ClientErrorException {
        long id = activeLayout.getActiveLayout((Long)msg.getData());
        SwitchStateMap switchStateList = switchStateRepository.getSwitchStateListForTracklayout(id);
        dispatcher.sendSingle(new Message(ControlMessage.GET_SWITCH_STAND_LIST_RES, switchStateList), msg.getEndpoint());
    }

    private void getTrainList(Message msg)
    throws SQLException, ClientErrorException {
        long id = activeLayout.getActiveLayout((Long)msg.getData());
        TrainList trainList = trainlistRepository.getTrainList(id);
        dispatcher.sendSingle(new Message(ControlMessage.GET_TRAIN_LIST_RES, trainList), msg.getEndpoint());
    }
}
