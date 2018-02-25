package ichttt.mods.firstaid.common.config;

import com.google.common.base.Joiner;
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

    private static final Joiner NEW_LINE = Joiner.on('\n');
    private final String name;
    private final String langKey;
    private final String comment;
    private final Type type;

    public static String getLangKey(Field field, String modid, String category) {
        Config.LangKey la = field.getAnnotation(Config.LangKey.class);
        if (la != null)
            return la.value();
        else
            return modid + "." + (category.isEmpty() ? "" : category + ".") + field.getName().toLowerCase(Locale.ENGLISH);
    }

    public static String getComment(Field field) {
        Config.Comment ca = field.getAnnotation(Config.Comment.class);
        if (ca != null)
            return NEW_LINE.join(ca.value());
        else
            return null;
    }

    public static UniqueProperty fromProperty(Field field, String modid, String category, UniqueProperty.Type type) {
        String name;
        if (field.isAnnotationPresent(Config.Name.class))
            name = field.getAnnotation(Config.Name.class).value();
        else
            name = field.getName();

        String langKey = getLangKey(field, modid, category);

        String comment = getComment(field);
        return new UniqueProperty(name, langKey, comment, type);
    }

    public UniqueProperty(String name, String langKey, String comment, Type type) {
        this.name = name;
        this.langKey = langKey;
        this.comment = comment;
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
