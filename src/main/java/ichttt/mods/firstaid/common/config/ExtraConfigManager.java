package ichttt.mods.firstaid.common.config;

import com.google.common.collect.ImmutableList;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.FirstAidConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.FieldWrapper;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExtraConfigManager
{
    private static final Set<Class<?>> configClasses = Collections.singleton(FirstAidConfig.class);
    public static List<Pair<ExtraConfig.Advanced, UniqueProperty>> advancedConfigOptions;
    public static List<Pair<ExtraConfig.Sync, UniqueProperty>> syncedConfigOptions;

    public static void init()
    {
        for (Class clazz : configClasses) {
            FirstAid.logger.info("Setting up config for class " + clazz);
            if (!clazz.isAnnotationPresent(ExtraConfig.class)) {
                FirstAid.logger.error("Cannot setup config for class " + clazz + " as it does not have the ExtraConfig annotation!");
                continue;
            }
            advancedConfigOptions = getAnnotatedFields(ExtraConfig.Advanced.class, clazz);
            syncedConfigOptions = getAnnotatedFields(ExtraConfig.Sync.class, clazz);
        }
    }

    private static <T extends Annotation> List<Pair<T, UniqueProperty>> getAnnotatedFields(Class<T> annotationClass, Class<?> clazz) {
        return getAnnotatedFields(annotationClass, clazz, "general", null);
    }

    private static<T extends Annotation> List<Pair<T, UniqueProperty>> getAnnotatedFields(Class<T> annotationClass, Class<?> clazz, String category, Object instance) {
        ImmutableList.Builder<Pair<T, UniqueProperty>> listBuilder = ImmutableList.builder();
        for (Field f : clazz.getDeclaredFields()) {
            T annotation = f.getAnnotation(annotationClass);
            if (annotation != null) {
                FirstAid.logger.info("Found annotation {} for field {}", annotation, f);
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
                    listBuilder.add(Pair.of(annotation, UniqueProperty.fromProperty(f, FirstAid.MODID, category, type)));
                }
            }
            if (!FieldWrapper.hasWrapperFor(f) && f.getType().getSuperclass() != null && f.getType().getSuperclass().equals(Object.class)) { //next object
                String sub = (category.isEmpty() ? "" : category + ".") + getName(f).toLowerCase(Locale.ENGLISH);
                if (annotation != null) {
                    listBuilder.add(Pair.of(annotation, new UniqueProperty(sub, UniqueProperty.getLangKey(f, FirstAid.MODID, category), UniqueProperty.getComment(f), UniqueProperty.Type.CATEGORY)));
                }
                try {
                    Object newInstance = f.get(instance);
                    listBuilder.addAll(getAnnotatedFields(annotationClass, newInstance.getClass(), sub, newInstance));
                } catch (IllegalAccessException e) {
                    FirstAid.logger.error("Error creating new instance of field " + f, e);
                }
            }
        }
        return listBuilder.build();
    }

    private static String getName(Field f)
    {
        if (f.isAnnotationPresent(Config.Name.class))
            return f.getAnnotation(Config.Name.class).value();
        return f.getName();
    }
}
