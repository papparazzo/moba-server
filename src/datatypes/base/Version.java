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

package datatypes.base;

import json.JSONToStringI;

public class Version implements Comparable, JSONToStringI {

    protected int major;
    protected int minor;
    protected int build;
    protected int patch;

    public Version() {
        this.major = -1;
    }

    public Version(int major, int minor, int build, int patch) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.patch = patch;
    }

    public Version(String version)
    throws IllegalArgumentException {

        String str = version;

        if(str == null || str.length() == 0) {
            throw new IllegalArgumentException(
                "version-string is empty or not set"
            );
        }

        String sa[] = str.split("-");

        str = sa[0];
        try {
            if(sa.length == 2) {
                this.patch = Integer.parseInt(sa[1]);
                if(this.patch > 9999) {
                    this.patch = 9999;
                }
            } else if(sa.length > 2) {
                throw new IllegalArgumentException("invalid Version-string");
            }

            sa = str.split("\\.");

            this.major = Integer.parseInt(sa[0]);

            if(sa.length > 1) {
                this.minor = Integer.parseInt(sa[1]);
            }

            if(sa.length > 2) {
                this.build = Integer.parseInt(sa[2]);
            }

            if(sa.length > 3) {
                this.patch = Integer.valueOf(sa[3]);
            }

        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("converting failed");
        }
    }

    public int compareMajor(Version v) {
        if(this.major < v.major) {
            return -1;
        } else if(this.major > v.major) {
            return 1;
        }
        return 0;
    }

    public int compareMinor(Version v) {
        if(this.minor < v.minor) {
            return -1;
        } else if(this.minor > v.minor) {
            return 1;
        }
        return 0;
    }

    @Override
    public int compareTo(Object o) {
        Version v = (Version)o;

        if(this.major < v.major) {
            return -1;
        } else if(this.major > v.major) {
            return 1;
        } else if(this.minor < v.minor) {
            return -1;
        } else if(this.minor > v.minor) {
            return 1;
        } else if(this.build < v.build) {
            return -1;
        } else if(this.build > v.build) {
            return 1;
        } else if(this.patch < v.patch) {
            return -1;
        } else if(this.patch > v.patch) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        if(this.major == -1) {
            return "0.0.0-0000";
        }
        StringBuilder b = new StringBuilder();
        b.append(String.valueOf(this.major));
        b.append('.');
        b.append(String.valueOf(this.minor));
        b.append('.');
        b.append(String.valueOf(this.build));
        b.append('-');

        if(this.patch < 10) {
            b.append("000");
        } else if(this.patch < 100) {
            b.append("00");
        } else if(this.patch < 1000) {
            b.append("0");
        }
        b.append(String.valueOf(this.patch));
        return b.toString();
    }

    @Override
    public String toJsonString(boolean formated, int indent) {
        if(this.major == -1) {
            return "\"0.0.0.0\"";
        }
        StringBuilder b = new StringBuilder();
        b.append('"');
        b.append(String.valueOf(this.major));
        b.append('.');
        b.append(String.valueOf(this.minor));
        b.append('.');
        b.append(String.valueOf(this.build));
        b.append('.');
        b.append(String.valueOf(this.patch));
        b.append('"');
        return b.toString();
    }
}
