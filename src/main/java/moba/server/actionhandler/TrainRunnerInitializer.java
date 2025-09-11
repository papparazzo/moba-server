/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.actionhandler;

import moba.server.datatypes.collections.BlockContactDataMap;
import moba.server.datatypes.collections.SwitchStateMap;
import moba.server.repositories.BlockListRepository;
import moba.server.repositories.TrackLayoutRepository;
import moba.server.repositories.SwitchStateRepository;
import moba.server.routing.parser.LayoutParser;
import moba.server.routing.router.SimpleRouter;
import moba.server.routing.typedefs.BlockNodeMap;
import moba.server.utilities.Database;
import moba.server.exceptions.ClientErrorException;

import java.sql.SQLException;

final public class TrainRunnerInitializer {

    private final Database database;

    public TrainRunnerInitializer(Database database) {
        this.database = database;
    }

    public TrainRunner getTrainRunner(long activeLayoutId)
        throws SQLException, ClientErrorException {

        //TrainlistRepository trainList = new TrainlistRepository(database);
        TrackLayoutRepository layout = new TrackLayoutRepository(database);

        BlockListRepository blockListRepository = new BlockListRepository(database);
        BlockContactDataMap blockContacts = blockListRepository.getBlockList(activeLayoutId);

        SwitchStateRepository switchStateListRepository = new SwitchStateRepository(database);
        SwitchStateMap switchStates = switchStateListRepository.getSwitchStateList(activeLayoutId);

        LayoutParser parser = new LayoutParser(
            layout.getLayout(activeLayoutId),
            blockContacts,
            switchStates
        );

        parser.parse();

        BlockNodeMap blocks = parser.getBlockMap();
        SimpleRouter router = new SimpleRouter(blocks);

        ActionListGenerator generator = new ActionListGenerator(blockContacts, switchStates);
        return new TrainRunner(router, generator);

    }
}
