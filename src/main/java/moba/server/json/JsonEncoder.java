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

package moba.server.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Iterator;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import moba.server.json.streamwriter.JsonStreamWriterInterface;

public class JsonEncoder {

    protected JsonStreamWriterInterface writer;

    public JsonEncoder(JsonStreamWriterInterface writer)
    throws IOException {
        if(writer == null) {
            throw new IOException("stream-writer not set");
        }
        this.writer = writer;
    }

    public void encode(Object value)
    throws IOException, JsonException {
        addJSONValue(value);
        writer.close();
    }

    protected void addObject(Map<?, ?> map)
    throws IOException, JsonException {
        writer.write('{');
        if(map != null) {
            Iterator<?> iter = map.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>)iter.next();
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
    throws IOException, JsonException {
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

            String key = "";

            if(methodName.startsWith("get") && methodName.length() > 3) {
                key = methodName.substring(3);
            }

            if(methodName.startsWith("is") && methodName.length() > 2) {
                key = methodName.substring(2);
            }

            if(key.isEmpty()) {
                continue;
            }

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
                throw new JsonException("error in invoking method <" + methodName + ">", exception);
            }
            firstIteration = false;
        }
        writer.write('}');
    }

    protected void addRecord(Object object)
    throws IOException, JsonException {
        writer.write('{');

        Class<?> cls = object.getClass();

        Field[] fields = cls.getDeclaredFields();

        boolean firstIteration = true;

        for(Field field : fields) {
            field.setAccessible(true);

            if(!firstIteration) {
                writer.write(',');
            }

            String key = field.getName();

            writer.write('"');
            writer.write(key);
            writer.write("\":");

            try {
                addJSONValue(field.get(object));
            } catch (IllegalAccessException exception) {
                throw new JsonException("error in invoking method <" + key + ">", exception);
            }
            firstIteration = false;
        }
        writer.write('}');
    }

    protected void addJSONValue(Object object)
    throws IOException, JsonException {
        if(object == null) {
            addNull();
        } else if(object instanceof JsonSerializerInterface<?> jSONToStringI) {
            addJSONValue(jSONToStringI.toJson());
        } else if(object instanceof Map<?, ?> map) {
            addObject(map);
        } else if(object instanceof Boolean boolean1) {
            addBoolean(boolean1);
        } else if(object instanceof Iterable<?> arrayList) {
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
        } else if(object instanceof Enum) {
            addString(object.toString());
        } else if(object instanceof Record) {
            addRecord(object);
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

    protected void addArray(Iterable<?> array)
    throws IOException, JsonException {
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

    protected void addArray(Object[] array)
    throws IOException, JsonException {
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
                    if(c < ' ' || c >= '\u0080') {
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
}