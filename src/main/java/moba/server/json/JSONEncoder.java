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

package moba.server.json;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import moba.server.json.streamwriter.JSONStreamWriterI;

public class JSONEncoder {
    protected boolean          formated;
    protected int              indent = 0;
    protected static final int MAX_STR_LENGTH = 4096;

    protected StringBuilder sb = new StringBuilder(JSONEncoder.MAX_STR_LENGTH);

    protected ArrayList<JSONStreamWriterI> writers = new ArrayList<>();

    public JSONEncoder() {
        this(false);
    }

    public JSONEncoder(boolean formated) {
        this.formated = formated;
    }

    public JSONEncoder(JSONStreamWriterI writer)
    throws IOException {
        this(writer, false);
    }

    public JSONEncoder(JSONStreamWriterI writer, boolean formated)
    throws IOException {
        if(writer == null) {
            throw new IOException("stream-writer not set");
        }
        this.formated = formated;
        writers.add(writer);
    }

    public void addAdditionalWriter(JSONStreamWriterI writer)
    throws IOException {
        if(writer == null) {
            throw new IOException("stream-writer not set");
        }
        writers.add(writer);
    }

    public void encode(Object map)
    throws IOException, JSONException {
        addJSONValue(map);
        flush();
    }

    public void encode(Object map, int indent)
    throws IOException, JSONException {
        this.indent = indent;
        encode(map);
    }

    protected void addObject(Map map)
    throws IOException, JSONException {
        write('{');
        if(map != null) {
            Iterator iter = map.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                write('"');
                write((String)entry.getKey());
                write("\":");
                addJSONValue(entry.getValue());
                if(iter.hasNext()) {
                    write(',');
                }
            }
        }
        write('}');
    }

    protected void addJSONValue(Object object)
    throws IOException, JSONException {
        if(object == null) {
            addNull();
        } else if(object instanceof Map) {
            addObject((Map)object);
        } else if(object instanceof Boolean) {
            addBoolean((Boolean)object);
        } else if(object instanceof ArrayList) {
            addArray((ArrayList)object);
        } else if(object instanceof String) {
            addString((String)object);
        } else if(object instanceof Integer) {
            addLong(object);
        } else if(object instanceof Long) {
            addLong(object);
        } else if(object instanceof Double) {
            addDouble(object);
        } else if(object instanceof Float) {
            addDouble(object);
        } else if(object.getClass().isArray()) {
            addArray((Object[])object);
        } else if(object instanceof Date) {
            addDate((Date)object);
        } else if(object instanceof InetAddress) {
            addInetAddr((InetAddress)object);
        } else if(object instanceof JSONToStringI) {
            write(((JSONToStringI)object).toJsonString(formated, indent));
        } else if(object instanceof Set) {
            addSet((Set)object);
        } else {
            addString(object.toString());
        }
    }

    protected void addBoolean(boolean value)
    throws IOException {
        if(value) {
            write("true");
            return;
        }
        write("false");
    }

    protected void addArray(ArrayList arraylist)
    throws IOException, JSONException {
        write('[');
        for(int i = 0; i < arraylist.size(); ++i) {
            if(i != 0) {
                write(',');
            }
            addJSONValue(arraylist.get(i));
        }
        write(']');
    }

    protected void addSet(Set setlist)
    throws IOException, JSONException {
        write('[');
        boolean fr = true;
        for(Object item : setlist) {
            if(!fr) {
                write(',');
            }
            addJSONValue(item);
            fr = false;
        }
        write(']');
    }

    protected void addArray(Object[] array)
    throws IOException, JSONException {
        write('[');
        boolean fr = true;
        for(Object item : array) {
            if(!fr) {
                write(',');
            }
            addJSONValue(item);
            fr = false;
        }
        write(']');
    }

    protected void addString(String str)
    throws IOException {
        // TODO Sonderzeichen maskieren!!
        //str.replace("\n", "\\n");
        //str.replace("\n", "\\n");
        write('"');
        write(str);
        write('"');
    }

    protected void addNull()
    throws IOException {
        write("null");
    }

    protected void addLong(Object obj)
    throws IOException {
        write(String.valueOf(obj));
    }

    protected void addDouble(Object obj)
    throws IOException {
        write(String.valueOf(obj));
    }

    protected void addDate(Date date)
    throws IOException {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS");
        addString(df.format(date));
    }

    protected void addInetAddr(InetAddress addr)
    throws IOException {
        String str = addr.getHostAddress();
        if(str == null) {
            addString("0.0.0.0");
        } else {
            addString(str);
        }
    }

    protected void addFormatStr(int indent)
    throws IOException {
        if(!formated) {
            return;
        }

        indent += indent * 4;
        write(System.getProperty("line.separator"));
        for(int i = 0; i < indent; i++) {
            write(' ');
        }
    }

    protected void write(char c)
    throws IOException {

        switch(c) {
            case '{':
            case '[':
                sb.append(c);
                addFormatStr(1);
                break;

            case ',':
                sb.append(c);
                addFormatStr(0);
                break;

            case '}':
            case ']':
                addFormatStr(-1);
                sb.append(c);
                break;

            default:
                sb.append(c);
        }

        if(JSONEncoder.MAX_STR_LENGTH == sb.length()) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected void write(String s)
    throws IOException {
        if(JSONEncoder.MAX_STR_LENGTH < sb.length() + s.length()) {
            throw new IndexOutOfBoundsException();
        }
        sb.append(s);
    }

    protected void flush()
    throws IOException {
        String s = sb.toString();
//System.err.print(s);
        for(JSONStreamWriterI writer : writers) {
            writer.write(s);
            writer.close();
        }
        sb.delete(0, sb.length());
    }
}