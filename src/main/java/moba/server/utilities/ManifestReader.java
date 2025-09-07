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

package moba.server.utilities;

import moba.server.datatypes.base.Version;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

final public class ManifestReader {

    private final String appName;
    private final Date buildDate;
    private final Version version;

    public ManifestReader(Class<?> clazz)
    throws IOException, ParseException {
        String className = clazz.getSimpleName() + ".class";
        String classPath = Objects.requireNonNull(clazz.getResource(className)).toString();
        if(!classPath.startsWith("jar")) {
            appName = "moba-server";
            buildDate = new Date();
            version   = new Version();
            return;
        }

        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(URI.create(manifestPath).toURL().openStream());
        Attributes attrs = manifest.getMainAttributes();

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        buildDate = formatter.parse(attrs.getValue("Build-Timestamp"));
        appName = attrs.getValue("Implementation-Title");
        version = new Version(attrs.getValue("Implementation-Version"));
    }

    public String getAppName() {
        return appName;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public Version getVersion() {
        return version;
    }
}
