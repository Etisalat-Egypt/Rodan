/*
 * Etisalat Egypt, Open Source
 * Copyright 2021, Etisalat Egypt and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

/**
 * @author Ayman ElSherif
 */

package com.rodan.library.model.config;

import com.rodan.library.model.annotation.Option;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

public abstract class AbstractConfig {
    final static Logger logger = LogManager.getLogger(AbstractConfig.class);

    public AbstractConfig() {
    }

    public static boolean isOptionField(Field field) {
        return field.getAnnotation(Option.class) != null;
    }

    public static String getFieldDisplayName(Field field) {
        return field.getAnnotation(Option.class).name();
    }

    public static String getFieldDescription(Field field) {
        return field.getAnnotation(Option.class).description();
    }

    public static boolean isFieldMandatory(Field field) {
        return field.getAnnotation(Option.class).mandatory();
    }

    public static boolean isFieldDisplayable(Field field) {
        return field.getAnnotation(Option.class).display();
    }

    public static boolean isFieldReadonly(Field field) {
        return field.getAnnotation(Option.class).readonly();
    }

    public String getFieldValue(AbstractConfig obj, Field field) throws SystemException {
        var handle = AbstractConfig.getFieldHandle(getClass(), field);
        var value = handle.get(obj);
        return value != null ? value.toString() : null;
    }

    public boolean trySetFieldByName(String name, String value) throws SystemException {
        var result = false;
        var directFilds = getClass().getDeclaredFields();
        var parentFields = getClass().getSuperclass().getDeclaredFields();
        var fields = Stream.concat(Arrays.stream(directFilds), Arrays.stream(parentFields)).filter(AbstractConfig::isOptionField).toArray(Field[]::new);
        for (Field field : fields) {
            if (!AbstractConfig.isFieldReadonly(field)) {
                var displayName = AbstractConfig.getFieldDisplayName(field);
                if (displayName.equals(name)) {
                    result = true;
                    var handle = AbstractConfig.getFieldHandle(getClass(), field);
                    handle.set(this, value);
                    break;
                }
            }
        }
        return result;
    }

    protected static boolean hasFieldWithName(Class clazz, String name) {
        var result = false;
        var directFilds = clazz.getDeclaredFields();
        var parentFields = clazz.getSuperclass().getDeclaredFields();
        var fields = Stream.concat(Arrays.stream(directFilds), Arrays.stream(parentFields)).filter(AbstractConfig::isOptionField).toArray(Field[]::new);
        for (Field field : fields) {
            var displayName = AbstractConfig.getFieldDisplayName(field);
            if (displayName.equals(name)) {
                result = true;
                break;
            }
        }

        return result;
    }

    public static final VarHandle getFieldHandle(Class clazz, Field field) throws SystemException {
        VarHandle handle;
        try {
            handle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).unreflectVarHandle(field);

        } catch (IllegalAccessException e) {
            String msg = "Failed to get a field handler for: " + field;
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.FIELD_REFLECTION_ERROR).message(msg).parent(e).build();
        }
        return handle;
    }
}
