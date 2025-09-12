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

package moba.server.repositories;

import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.objects.BlockContactData;
import moba.server.datatypes.objects.ContactData;
import moba.server.datatypes.collections.BlockContactDataMap;
import moba.server.utilities.Database;
import moba.server.exceptions.ClientErrorException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class BlockListRepository {
    protected final Database database;

    public BlockListRepository(Database database) {
        this.database = database;
    }

    public BlockContactDataMap getBlockList(long id)
    throws SQLException {
        Connection con = database.getConnection();

        String q =
            "SELECT `BlockSections`.`Id`, `BlockSections`.`TrainId`, " +
            "`TriggerContact`.`ModulAddress` AS `TriggerModulAddress`, " +
            "`TriggerContact`.`ContactNumber` AS `TriggerModulContactNumber`, " +
            "`BlockContact`.`ModulAddress` AS `BlockModulAddress`, " +
            "`BlockContact`.`ContactNumber` AS `BlockModulContactNumber` " +
            "FROM `BlockSections` " +
            "LEFT JOIN `FeedbackContacts` AS `TriggerContact` " +
            "ON `BrakeTriggerContactId` = `TriggerContact`.`Id` " +
            "LEFT JOIN `FeedbackContacts` AS `BlockContact` " +
            "ON `BlockContactId` = `BlockContact`.`Id` " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `BlockSections`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);

            BlockContactDataMap map = new BlockContactDataMap();

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Integer trainId = rs.getInt("TrainId");
                trainId = rs.wasNull() ? null : trainId;
                map.put(
                    rs.getLong("Id"),
                    new BlockContactData(
                        new ContactData(
                            rs.getInt("TriggerModulAddress"),
                            rs.getInt("TriggerModulContactNumber")
                        ),
                        new ContactData(
                            rs.getInt("BlockModulAddress"),
                            rs.getInt("BlockModulContactNumber")
                        ),
                        trainId
                    )
                );
            }
            return map;
        }
    }

    public void saveBlockList(long id, BlockContactDataMap container)
    throws SQLException, ClientErrorException {

        Connection con = database.getConnection();

        String stmt = "UPDATE `TrackLayouts` SET `ModificationDate` = NOW() WHERE `Id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            if(pstmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not save <" + id + ">");
            }
        }

        stmt =
            "DELETE `BlockSections`.* " +
            "FROM `BlockSections` " +
            "LEFT JOIN `TrackLayoutSymbols` ON `TrackLayoutSymbols`.`Id` = `BlockSections`.`Id` "    +
            "WHERE `TrackLayoutId` = ?";

        try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }

        for (Map.Entry<Long, BlockContactData> entry : container.entrySet()) {
            Long key = entry.getKey();
            BlockContactData value = entry.getValue();

            stmt =
                "INSERT INTO `BlockSections` " +
                "(`Id`, `BrakeTriggerContactId`, `BlockContactId`, `TrainId`) " +
                "VALUES (?, " +
                    "(SELECT `Id` FROM `FeedbackContacts` WHERE `ModulAddress` = ? AND `ContactNumber` = ?), " +
                    "(SELECT `Id` FROM `FeedbackContacts` WHERE `ModulAddress` = ? AND `ContactNumber` = ?), ?)";

            try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
                pstmt.setLong(1, key);
                pstmt.setLong(2, value.brakeTriggerContact().moduleAddr());
                pstmt.setLong(3, value.brakeTriggerContact().contactNb());
                pstmt.setLong(4, value.blockContact().moduleAddr());
                pstmt.setLong(5, value.blockContact().contactNb());
                if(value.trainId() == null) {
                    pstmt.setNull(6, java.sql.Types.INTEGER);
                } else {
                    pstmt.setLong(6, value.trainId());
                }
                pstmt.executeUpdate();
            }
        }
    }
}
