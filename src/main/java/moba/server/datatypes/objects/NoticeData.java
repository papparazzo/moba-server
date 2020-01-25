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

package moba.server.datatypes.objects;

import java.io.IOException;
import java.util.HashMap;

import moba.server.datatypes.enumerations.NoticeType;
import moba.server.json.JSONEncoder;
import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;
import moba.server.json.streamwriter.JSONStreamWriterStringBuilder;

public class NoticeData implements JSONToStringI {

    protected final NoticeType noticeType;
    protected final String caption;
    protected final String text;

    public NoticeData(String caption) {
        this(caption, caption);
    }

    public NoticeData(String caption, String text) {
        this(NoticeType.INFO, caption, text);
    }

    public NoticeData(NoticeType noticeType, String caption, String text) {
        this.noticeType = noticeType;
        this.caption = caption;
        this.text = text;
    }

    public NoticeType getType() {
        return noticeType;
    }

    public String getCaption() {
        return caption;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type",    noticeType);
        map.put("caption", caption);
        map.put("text",    text);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
