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

package moba.server.datatypes.base;

import moba.server.json.JSONToStringI;

public class Version implements Comparable, JSONToStringI {

    protected int major;
    protected int minor;
    protected int build;
    protected int patch;

    public Version() {
        this.major = -1;
    }

    public Version(String version)
    throws IllegalArgumentException {

        String str = version;

        if(str == null || str.isEmpty()) {
            throw new IllegalArgumentException("version-string is empty or not set");
        }

        String[] sa = str.split("-");

        str = sa[0];
        try {
            if(sa.length == 2) {
                patch = Integer.parseInt(sa[1]);
                if(patch > 9999) {
                    patch = 9999;
                }
            } else if(sa.length > 2) {
                throw new IllegalArgumentException("invalid Version-string");
            }

            sa = str.split("\\.");

            major = Integer.parseInt(sa[0]);

            if(sa.length > 1) {
                minor = Integer.parseInt(sa[1]);
            }

            if(sa.length > 2) {
                build = Integer.parseInt(sa[2]);
            }

            if(sa.length > 3) {
                patch = Integer.parseInt(sa[3]);
            }

        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("converting failed", e);
        }
    }

    public int compareMajor(Version v) {
        if(major < v.major) {
            return -1;
        } else if(major > v.major) {
            return 1;
        }
        return 0;
    }

    public int compareMinor(Version v) {
        if(minor < v.minor) {
            return -1;
        } else if(minor > v.minor) {
            return 1;
        }
        return 0;
    }

    @Override
    public int compareTo(Object o) {
        Version v = (Version)o;

        if(major < v.major) {
            return -1;
        }
        if(major > v.major) {
            return 1;
        }
        if(minor < v.minor) {
            return -1;
        }
        if(minor > v.minor) {
            return 1;
        }
        if(build < v.build) {
            return -1;
        }
        if(build > v.build) {
            return 1;
        }
        return Integer.compare(patch, v.patch);
    }

    @Override
    public String toString() {
        if(major == -1) {
            return "0.0.0-0000";
        }
        StringBuilder b = new StringBuilder();
        b.append(major);
        b.append('.');
        b.append(minor);
        b.append('.');
        b.append(build);
        b.append('-');

        if(patch < 10) {
            b.append("000");
        } else if(patch < 100) {
            b.append("00");
        } else if(patch < 1000) {
            b.append("0");
        }
        b.append(patch);
        return b.toString();
    }

    @Override
    public String toJsonString(boolean formatted, int indent) {
        if(major == -1) {
            return "\"0.0.0.0\"";
        }
        return String.valueOf('"' + major + '.' + minor + '.' + build + '.' + patch + '"');
    }
}
