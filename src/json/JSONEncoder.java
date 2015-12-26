/*
 *  json
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

package json;

import json.streamwriter.JSONStreamWriterI;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JSONEncoder {
    protected boolean          formated;
    protected int              indent = 0;
    protected static final int MAX_STR_LENGTH = 4096;

    protected StringBuilder sb = new StringBuilder(JSONEncoder.MAX_STR_LENGTH);

    protected ArrayList<JSONStreamWriterI> writers = new ArrayList<>();

    public JSONEncoder() {
        this(false);
    }

    public JSONEncoder(boolean formated){
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
        this.writers.add(writer);
    }

    public void addAdditionalWriter(JSONStreamWriterI writer)
    throws IOException {
        if(writer == null) {
            throw new IOException("stream-writer not set");
        }
        this.writers.add(writer);
    }

    public void encode(Map map)
    throws IOException, JSONException {
        this.addObject(map);
        this.flush();
    }

    public void encode(Map map, int indent)
    throws IOException, JSONException {
        this.indent = indent;
        this.encode(map);
    }

    protected void addObject(Map map)
    throws IOException, JSONException {
        this.write('{');
        if(map != null) {
            Iterator iter = map.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                this.write('"');
                this.write((String)entry.getKey());
                this.write("\":");
                this.addJSONValue(entry.getValue());
                if(iter.hasNext()) {
                    this.write(',');
                }
            }
        }
        this.write('}');
    }

    protected void addJSONValue(Object object)
    throws IOException, JSONException {
        if(object == null) {
            this.addNull();
        } else if(object instanceof Map) {
            this.addObject((Map)object);
        } else if(object instanceof Boolean) {
            this.addBoolean((Boolean)object);
        } else if(object instanceof ArrayList) {
            this.addArray((ArrayList)object);
        } else if(object instanceof String) {
            this.addString((String)object);
        } else if(object instanceof Integer) {
            this.addLong(object);
        } else if(object instanceof Long) {
            this.addLong(object);
        } else if(object instanceof Double) {
            this.addDouble(object);
        } else if(object instanceof Float) {
            this.addDouble(object);
        } else if(object.getClass().isArray()) {
            this.addArray((Object[])object);
        } else if(object instanceof Date) {
            this.addDate((Date)object);
        } else if(object instanceof InetAddress) {
            this.addInetAddr((InetAddress)object);
        } else if(object instanceof JSONToStringI) {
            this.write(((JSONToStringI)object).toJsonString(this.formated, this.indent));
        } else if(object instanceof Set) {
            this.addSet((Set)object);
        } else {
            this.addString(object.toString());
        }
    }

    protected void addBoolean(boolean value)
    throws IOException {
        if(value) {
            this.write("true");
            return;
        }
        this.write("false");
    }

    protected void addArray(ArrayList arraylist)
    throws IOException, JSONException {
        this.write('[');
        for(int i = 0; i < arraylist.size(); ++i) {
            if(i != 0) {
                this.write(',');
            }
            this.addJSONValue(arraylist.get(i));
        }
        this.write(']');
    }

    protected void addSet(Set setlist)
    throws IOException, JSONException {
        this.write('[');
        boolean fr = true;
        for(Object item : setlist) {
            if(!fr) {
                this.write(',');
            }
            this.addJSONValue(item);
            fr = false;
        }
        this.write(']');
    }

    protected void addArray(Object[] array)
    throws IOException, JSONException {
        this.write('[');
        boolean fr = true;
        for(Object item : array) {
            if(!fr){
                this.write(',');
            }
            this.addJSONValue(item);
            fr = false;
        }
        this.write(']');
    }

    protected void addString(String str)
    throws IOException {
        // TODO Sonderzeichen maskieren!!
        //str.replace("\n", "\\n");
        //str.replace("\n", "\\n");
        this.write('"');
        this.write(str);
        this.write('"');
    }

    protected void addNull()
    throws IOException {
        this.write("null");
    }

    protected void addLong(Object obj)
    throws IOException {
        this.write(String.valueOf(obj));
    }

    protected void addDouble(Object obj)
    throws IOException {
        this.write(String.valueOf(obj));
    }

    protected void addDate(Date date)
    throws IOException {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS");
        this.addString(df.format(date));
    }

    protected void addInetAddr(InetAddress addr)
    throws IOException {
        String str = addr.getHostAddress();
        if(str == null) {
            this.addString("0.0.0.0");
        } else {
            this.addString(str);
        }
    }

    protected void addFormatStr(int indent)
    throws IOException {
        if(!this.formated) {
            return;
        }

        this.indent += indent * 4;
        this.write(System.getProperty("line.separator"));
        for(int i = 0; i < this.indent; i++) {
            this.write(' ');
        }
    }

    protected void write(char c)
    throws IOException {

        switch(c) {
            case '{':
            case '[':
                this.sb.append(c);
                this.addFormatStr(1);
                break;

            case ',':
                this.sb.append(c);
                this.addFormatStr(0);
                break;

            case '}':
            case ']':
                this.addFormatStr(-1);
                this.sb.append(c);
                break;

            default:
                this.sb.append(c);
        }

        if(JSONEncoder.MAX_STR_LENGTH == this.sb.length()) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected void write(String s)
    throws IOException {
        if(JSONEncoder.MAX_STR_LENGTH < this.sb.length() + s.length()) {
            throw new IndexOutOfBoundsException();
        }
        this.sb.append(s);
    }

    protected void flush()
    throws IOException {
        String s = this.sb.toString();

        for(JSONStreamWriterI writer : this.writers) {
            writer.write(s);
            writer.close();
        }
        this.sb.delete(0, this.sb.length());
    }
}