package ichttt.mods.firstaid.common.config;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtraConfig
{

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Sync
    {
    }

//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.FIELD)
//    @interface CustomGui
//    {
//        @Nonnull
//        String className();
//    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Advanced
    {
        @Nonnull
        String warningKey() default "";
    }
}
