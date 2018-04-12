package ichttt.mods.firstaid.common.config;

import com.google.common.collect.ImmutableList;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.FirstAidConfig;
import joptsimple.internal.Strings;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.FieldWrapper;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExtraConfigManager {
    private static List<String> toDeleteEntries = new ArrayList<>();
    private static final Field CONFIG_FIELD;
    static {
        FirstAid.logger.debug("Setting up forge internal reflection");
        Field field;
        try {
            field = ConfigManager.class.getDeclaredField("CONFIGS");
            field.setAccessible(true);
            field.get(null);
        } catch (ReflectiveOperationException |IllegalArgumentException e) {
            FirstAid.logger.error("Could not setup forge reflection - disabling config post processing", e);
            field = null;
        }
        CONFIG_FIELD = field;
    }

    public static void scheduleDelete(String name) {
        if (toDeleteEntries != null) toDeleteEntries.add(name);
        else throw new IllegalStateException("Already cleaned up configs");
    }

    @SuppressWarnings("unchecked")
    private static Configuration getConfigFromField(File f) {
        if (CONFIG_FIELD != null) {
            try {
                return ((Map<String, Configuration>) CONFIG_FIELD.get(null)).get(f.getAbsolutePath());
            } catch (Exception e) {
                FirstAid.logger.error("Could not get config from field - enabling fallback", e);
                return null;
            }
        }
        return null;
    }

    public static void postProccessConfigs() {
        Config annotation = FirstAidConfig.class.getAnnotation(Config.class);
        String name = annotation.name();
        if (Strings.isNullOrEmpty(name))
            name = annotation.modid();
        Configuration config = getConfigFromField(new File(Loader.instance().getConfigDir(), name + ".cfg"));

        if (config == null) {
            FirstAid.logger.warn("Skipping post processing due to null config");
            return;
        }

        for (String s : toDeleteEntries) {
            String path[] = s.split("\\.");
            String catString = Configuration.CATEGORY_GENERAL + (path.length > 1 ? "." + s.substring(0, s.length() - (path[path.length - 1].length() + 1)) : "");
            if (config.hasCategory(catString)) {
                ConfigCategory cat = config.getCategory(catString);
                if (cat.containsKey(path[path.length - 1])) {
                    FirstAid.logger.info("Removing prop " + s);
                    cat.remove(path[path.length - 1]);
                }
            } else {
                FirstAid.logger.warn("Unable to find config category {} for removal of old config options", catString);
            }
        }
        if (config.hasChanged()) config.save();
        toDeleteEntries = null;
    }

    public static <T extends Annotation> List<ConfigEntry<T>> getAnnotatedFields(Class<T> annotationClass, Class<?> clazz) {
        return getAnnotatedFields(annotationClass, clazz, "general", null);
    }

    private static<T extends Annotation> List<ConfigEntry<T>> getAnnotatedFields(Class<T> annotationClass, Class<?> clazz, String category, Object instance) {
        ImmutableList.Builder<ConfigEntry<T>> listBuilder = ImmutableList.builder();
        for (Field f : clazz.getDeclaredFields()) {
            T annotation = f.getAnnotation(annotationClass);
            if (annotation != null) {
//                FirstAid.logger.debug("Found annotation {} for field {}", annotation, field);
                if (FieldWrapper.hasWrapperFor(f)) {
                    Object typeAdapter = FieldWrapper.get(instance, f, category).getTypeAdapter();
                    UniqueProperty.Type type;
                    if (typeAdapter == null) {
                        type = UniqueProperty.Type.UNKNOWN;
                    } else {
                        try {
                            Method m = typeAdapter.getClass().getDeclaredMethod("getType");
                            m.setAccessible(true);
                            Property.Type propType = (Property.Type) m.invoke(typeAdapter);
                            type = UniqueProperty.Type.fromType(propType);
                        } catch (ReflectiveOperationException | ClassCastException e) {
                            FirstAid.logger.fatal("Error getting type from type adapter for field " + f, e);
                            type = UniqueProperty.Type.UNKNOWN;
                        }
                    }
                    listBuilder.add(new ConfigEntry<>(f, instance, annotation, annotationClass == ExtraConfig.Sync.class, UniqueProperty.fromProperty(f, FirstAid.MODID, category, type)));
                }
            }
            if (!FieldWrapper.hasWrapperFor(f) && f.getType().getSuperclass() != null && f.getType().getSuperclass().equals(Object.class)) { //next object
                String sub = (category.isEmpty() ? "" : category + ".") + getName(f).toLowerCase(Locale.ENGLISH);
                if (annotation != null) {
                    listBuilder.add(new ConfigEntry<>(f, instance, annotation, annotationClass == ExtraConfig.Sync.class, new UniqueProperty(getName(f), UniqueProperty.getLangKey(f, FirstAid.MODID, category), UniqueProperty.getComment(f), UniqueProperty.Type.CATEGORY)));
                }
                try {
                    Object newInstance = f.get(instance);
                    listBuilder.addAll(getAnnotatedFields(annotationClass, newInstance.getClass(), sub, newInstance));
                } catch (IllegalAccessException e) {
                    FirstAid.logger.error("Error creating new instance of field " + f, e);
                }
            }
        }
        List<ConfigEntry<T>> list = listBuilder.build();
        if (instance == null)
            FirstAid.logger.debug("Found {} annotations of the type {} for the {}", list.size(), annotationClass, clazz);
        return listBuilder.build();
    }

    private static String getName(Field f)
    {
        if (f.isAnnotationPresent(Config.Name.class))
            return f.getAnnotation(Config.Name.class).value();
        return f.getName();
    }
}
