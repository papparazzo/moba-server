/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2023 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.datatypes.objects.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import moba.server.json.JSONException;

public class ToObject {

    protected void addObject(Object object, Map<String, Object> map)
    throws JSONException {

        Class<?> cls = object.getClass();

        Method[] methods = cls.getMethods();
        for(final Method method : methods) {
            final int modifiers = method.getModifiers();

            if(Modifier.isStatic(modifiers)) {
                continue;
            }

            if(
                method.getParameterTypes().length != 1 || 
                method.getReturnType() != Void.TYPE
            ) {
                continue;
            }

            String methodName = method.getName();

            if(
                methodName.equals("getClass") || 
                methodName.equals("getDeclaringClass")
            ) {
                continue;
            }

            if(!methodName.startsWith("get") || methodName.length() < 4) {
                continue;
            }

            String key = methodName.substring(3);

            key =
                key.substring(0, 1).toLowerCase(Locale.ROOT) +
                key.substring(1);

            try {
                method.invoke(map.get(key));
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new JSONException(
                    "error in invoking method <" + methodName + ">",
                    exception
                );
            }
        }
    }
}
