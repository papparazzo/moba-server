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

package moba.server.apiconnector;

public final class TrainApiConnector {
    private final OAuth2HttpClient client;
    private final String endPoint;

    public TrainApiConnector(OAuth2HttpClient client, String baseUrl) {
        this.client = client;
        this.endPoint = baseUrl;
    }

    public Object getTrains()
    throws ApiConnectorException {
        return client.get(endPoint + "trains");
    }

    public Object getTrain(int id)
    throws ApiConnectorException {
        return client.get(endPoint + "train/" + id);
    }
}
