package moba.server.utilities;

import moba.server.App;
import moba.server.datatypes.base.Version;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

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
class ManifestReaderTest {

    @Test
    void getAppName()
    throws IOException, ParseException {
        ManifestReader reader = new ManifestReader(App.class);
        assertEquals("moba-server", reader.getAppName());
    }

    @Test
    void getBuildDate()
    throws IOException, ParseException {
        ManifestReader reader = new ManifestReader(App.class);
        assertNotNull(reader.getBuildDate());
    }

    @Test
    void getVersion()
    throws IOException, ParseException {
        ManifestReader reader = new ManifestReader(App.class) ;
        assertNotNull(reader.getVersion());
    }
}