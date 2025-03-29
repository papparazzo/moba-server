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

package moba.server.datatypes.objects;

import moba.server.datatypes.enumerations.NoticeType;

public class NoticeData {

    protected final NoticeType type;
    protected final String caption;
    protected final String text;

    public NoticeData(String caption) {
        this(caption, caption);
    }

    public NoticeData(String caption, String text) {
        this(NoticeType.INFO, caption, text);
    }

    public NoticeData(NoticeType noticeType, String caption, String text) {
        this.type = noticeType;
        this.caption = caption;
        this.text = text;
    }

    public NoticeType getType() {
        return type;
    }

    public String getCaption() {
        return caption;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "[" + type.toString() + "] " + caption + ": " + text;
    }
}
