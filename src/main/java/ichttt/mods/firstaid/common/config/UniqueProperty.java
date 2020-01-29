/*
 * FirstAid
 * Copyright (C) 2017-2020
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;

public class UniqueProperty
{
    public enum Type
    {
        STRING,
        INTEGER,
        BOOLEAN,
        DOUBLE,
        COLOR,
        MOD_ID,
        CATEGORY,
        UNKNOWN;

        public static Type fromType(Property.Type type) {
            if (type == null)
                return UNKNOWN;
            switch (type) {
                case STRING:
                    return STRING;
                case INTEGER:
                    return INTEGER;
                case BOOLEAN:
                    return BOOLEAN;
                case DOUBLE:
                    return DOUBLE;
                case COLOR:
                    return COLOR;
                case MOD_ID:
                    return MOD_ID;
                default:
                    throw new RuntimeException("Unknown property type " + type);
            }
        }

        public static Type fromType(ConfigGuiType type) {
            if (type == null)
                return UNKNOWN;
            switch (type) {
                case STRING:
                    return STRING;
                case INTEGER:
                    return INTEGER;
                case BOOLEAN:
                    return BOOLEAN;
                case DOUBLE:
                    return DOUBLE;
                case COLOR:
                    return COLOR;
                case MOD_ID:
                    return MOD_ID;
                case CONFIG_CATEGORY:
                    return CATEGORY;
                default:
                    throw new RuntimeException("Unknown property type " + type);
            }
        }
    }

    private final String name;
    private final String langKey;
    private final Type type;

    public static String getLangKey(Field field, String modid, String category) {
        Config.LangKey la = field.getAnnotation(Config.LangKey.class);
        if (la != null)
            return la.value();
        else
            return modid + "." + (category.isEmpty() ? "" : category + ".") + field.getName().toLowerCase(Locale.ENGLISH);
    }

    public static UniqueProperty fromProperty(Field field, String modid, String category, UniqueProperty.Type type) {
        String name;
        if (field.isAnnotationPresent(Config.Name.class))
            name = field.getAnnotation(Config.Name.class).value();
        else
            name = field.getName();

        String langKey = getLangKey(field, modid, category);
        return new UniqueProperty(name, langKey, type);
    }

    public UniqueProperty(String name, String langKey, Type type) {
        this.name = name;
        this.langKey = langKey;
        this.type = type;
    }

//    public boolean matches(Property property) {
//        return Type.fromType(property.getType()).equals(this.type) && property.getName().equals(this.name) &&
//                property.getLanguageKey().equals(this.langKey) && Objects.equals(property.getComment(), this.comment);
//    }
//
//    public boolean matches(ConfigCategory category) {
//        return Objects.equals(category.getComment(), comment) && category.getName().equals(name) &&
//                category.getLanguagekey().equals(langKey) && type == Type.CATEGORY;
//    }

    public boolean matches(IConfigElement element) {
        return element.getLanguageKey().equals(langKey) && (element.isProperty() ? Type.fromType(element.getType()).equals(type) : type == Type.CATEGORY)
                && Objects.equals(element.getName(), name);
    }
}
