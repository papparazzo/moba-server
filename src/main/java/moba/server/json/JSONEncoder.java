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
import java.util.Locale;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import moba.server.json.streamwriter.JSONStreamWriterI;

public class JSONEncoder {
    protected boolean          formated;
    protected int              indent = 0;

    protected JSONStreamWriterI writer = null;

    public JSONEncoder(JSONStreamWriterI writer)
    throws IOException {
        this(writer, false);
    }

    public JSONEncoder(JSONStreamWriterI writer, boolean formatted)
    throws IOException {
        if(writer == null) {
            throw new IOException("stream-writer not set");
        }
        this.formatted = formatted;
        this.writer = writer;
    }

    public void encode(Object map)
    throws IOException, JSONException {
        addJSONValue(map);
        writer.close();
    }

    public void encode(Object map, int indent)
    throws IOException, JSONException {
        this.indent = indent;
        encode(map);
    }

    protected void addObject(Map map)
    throws IOException, JSONException {
        writer.write('{');
        if(map != null) {
            Iterator iter = map.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                writer.write('"');
                writer.write((String)entry.getKey());
                writer.write("\":");
                addJSONValue(entry.getValue());
                if(iter.hasNext()) {
                    writer.write(',');
                }
            }
        }
        writer.write('}');
    }

    protected void addObject(Object object)
    throws IOException, JSONException {
        writer.write('{');

        Class<?> cls = object.getClass();

        boolean firstIteration = true;

        Method[] methods = cls.getMethods();
        for(final Method method : methods) {
            final int modifiers = method.getModifiers();

            if(Modifier.isStatic(modifiers)) {
                continue;
            }

            if(method.getParameterTypes().length > 0 || method.getReturnType() == Void.TYPE) {
                continue;
            }

            String methodName = method.getName();

            if(methodName.equals("getClass") || methodName.equals("getDeclaringClass")) {
                continue;
            }

            if(!methodName.startsWith("get") || methodName.length() <= 3) {
                continue;
            }

            String key = methodName.substring(3);

            key = 
                key.substring(0, 1).toLowerCase(Locale.ROOT) +
                key.substring(1);

            if(!firstIteration) {
                writer.write(',');
            }

            writer.write('"');
            writer.write(key);
            writer.write("\":");
            try {
                addJSONValue(method.invoke(object));
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new JSONException("error in invoking method <" + methodName + ">", exception);
            }
            firstIteration = false;
        }
        writer.write('}');
    }

    protected void addJSONValue(Object object)
    throws IOException, JSONException {
        if(object == null) {
            addNull();
        } else if(object instanceof Map map) {
            addObject(map);
        } else if(object instanceof Boolean boolean1) {
            addBoolean(boolean1);
        } else if(object instanceof ArrayList arrayList) {
            addArray(arrayList);
        } else if(object instanceof String string) {
            addString(string);
        } else if(object instanceof Integer) {
            addNumber(object);
        } else if(object instanceof Long) {
            addNumber(object);
        } else if(object instanceof Double) {
            addNumber(object);
        } else if(object instanceof Float) {
            addNumber(object);
        } else if(object.getClass().isArray()) {
            addArray((Object[])object);
        } else if(object instanceof Date date) {
            addDate(date);
        } else if(object instanceof Enum) {
            addString(object.toString());
        } else if(object instanceof InetAddress inetAddress) {
            addInetAddr(inetAddress);
        } else if(object instanceof JSONToStringI jSONToStringI) {
            writer.write(jSONToStringI.toJsonString(formated, indent));
        } else if(object instanceof Set set) {
            addSet(set);
        } else {
            addObject(object);
        }
    }

    protected void addBoolean(boolean value)
    throws IOException {
        if(value) {
            writer.write("true");
            return;
        }
        writer.write("false");
    }

    protected void addArray(ArrayList arraylist)
    throws IOException, JSONException {
        writer.write('[');
        for(int i = 0; i < arraylist.size(); ++i) {
            if(i != 0) {
                writer.write(',');
            }
            addJSONValue(arraylist.get(i));
        }
        writer.write(']');
    }

    protected void addSet(Set setList)
    throws IOException, JSONException {
        writer.write('[');
        boolean fr = true;
        for(Object item : setList) {
            if(!fr) {
                writer.write(',');
            }
            addJSONValue(item);
            fr = false;
        }
        writer.write(']');
    }

    protected void addArray(Object[] array)
    throws IOException, JSONException {
        writer.write('[');
        boolean fr = true;
        for(Object item : array) {
            if(!fr) {
                writer.write(',');
            }
            addJSONValue(item);
            fr = false;
        }
        writer.write(']');
    }

    protected void addString(String str)
    throws IOException {
        if(str == null || str.isEmpty()) {
            writer.write("\"\"");
            return;
        }

        writer.write('"');
        for(int i = 0; i < str.length(); i += 1) {
            char c = str.charAt(i);

            switch(c) {
                case '\\', '"', '/' -> {
                    writer.write('\\');
                    writer.write(c);
                }
                case '\b' -> writer.write("\\b");
                case '\t' -> writer.write("\\t");
                case '\n' -> writer.write("\\n");
                case '\f' -> writer.write("\\f");
                case '\r' -> writer.write("\\r");

                default -> {
                    if(c < '\u0020' || c >= '\u0080') {
                        writer.write("\\u");
                        writer.write(Integer.toHexString(0x10000 | c).substring(1).toUpperCase());
                    } else {
                        writer.write(c);
                    }
                }
            }
        }
        writer.write('"');
    }

    protected void addNull()
    throws IOException {
        writer.write("null");
    }

    protected void addNumber(Object obj)
    throws IOException {
        writer.write(String.valueOf(obj));
    }

    protected void addDate(Date date)
    throws IOException {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
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
        if(!formatted) {
            return;
        }

        indent += indent * 4;
        writer.write(System.getProperty("line.separator"));
        for(int i = 0; i < indent; i++) {
            write(' ');
        }
    }

    protected void write(char c)
    throws IOException {

        switch(c) {
            case '{', '[' -> {
                writer.write(c);
                addFormatStr(1);
            }
            case ',' -> {
                writer.write(c);
                addFormatStr(0);
            }

            case '}', ']' -> {
                addFormatStr(-1);
                writer.write(c);
            }

            default -> writer.write(c);
        }
    }
}