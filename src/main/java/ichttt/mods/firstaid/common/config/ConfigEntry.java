package ichttt.mods.firstaid.common.config;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class ConfigEntry<T extends Annotation> {
    @Nonnull
    public final Field field;
    @Nullable
    private final Object fieldAccessor;
    private final boolean revert;
    @Nonnull
    public final UniqueProperty property;
    @Nonnull
    public final T annotation;
    private boolean hasRemoteData = false;
    @Nullable
    private Object originalState;

    public ConfigEntry(@Nonnull Field f, @Nullable Object fieldAccessor, @Nonnull T annotation, boolean revertOnSave, @Nonnull UniqueProperty property) {
        this.field = f;
        this.fieldAccessor = fieldAccessor;
        this.revert = revertOnSave;
        this.property = property;
        this.annotation = annotation;
        if (revertOnSave) {
            try {
                this.originalState = f.get(fieldAccessor);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field " + f, e);
            }

            //validate we can write to bytebuf
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            try {
                writeToBuf(buffer);
                readFromBuf(buffer);
                //noinspection ConstantConditions see above
                if (!originalState.equals(f.get(fieldAccessor)))
                    throw new RuntimeException("Error while writing and reading: Values do not match!");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field " + f, e);
            } finally {
                buffer.release();
                hasRemoteData = false;
            }
            //valid
        }
    }

    public boolean hasRemoteData() {
        return hasRemoteData;
    }

    @Nonnull
    public Object getCurrentState() {
        try {
            return field.get(fieldAccessor);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field " + field, e);
        }
    }

    public void setRemoteState(@Nonnull Object data) {
        hasRemoteData = true;
        try {
            field.setAccessible(true);
            field.set(fieldAccessor, Objects.requireNonNull(data));
            field.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field " + field, e);
        }
    }

    public void revert() {
        if (revert && hasRemoteData) {
            hasRemoteData = false;
            try {
                field.setAccessible(true);
                field.set(fieldAccessor, originalState);
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field " + field, e);
            }
        }
    }

    public void updateOrigState() {
        if (revert) {
            originalState = getCurrentState();
        }
    }

    public void writeToBuf(ByteBuf buf) {
        try {
            writeToByteBuf(field, fieldAccessor, buf);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to read config entry " + field + " for write to ByteBuffer!", e);
        }
    }

    public void readFromBuf(ByteBuf buf) {
        try {
            hasRemoteData = true;
            readFromByteBuf(field, fieldAccessor, buf);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to write config entry " + field + " from ByteBuffer!", e);
        }
    }

    //Ugly stuff down here...

    private static void readFromByteBuf(Field f, Object fieldAccessor, ByteBuf buf) throws IllegalAccessException {
        if (!Modifier.isPublic(f.getModifiers()))
            return;
        if (Modifier.isStatic(f.getModifiers()) != (fieldAccessor == null))
            return;
        Class<?> type = f.getType();
        if (type == boolean.class || type == Boolean.class) {
            f.setBoolean(fieldAccessor, buf.readBoolean());
        } else if (type == float.class || type == Float.class) {
            f.setFloat(fieldAccessor, buf.readFloat());
        } else if (type == double.class || type == Double.class) {
            f.setDouble(fieldAccessor, buf.readDouble());
        } else if (type == byte.class || type == Byte.class) {
            f.setByte(fieldAccessor, buf.readByte());
        } else if (type == char.class || type == Character.class) {
            f.setChar(fieldAccessor, buf.readChar());
        } else if (type == short.class || type == Short.class) {
            f.setShort(fieldAccessor, buf.readShort());
        } else if (type == int.class || type == Integer.class) {
            f.setInt(fieldAccessor, buf.readInt());
        } else if (type == String.class) {
            f.set(fieldAccessor, ByteBufUtils.readUTF8String(buf));
        } else if (type == boolean[].class || type == Boolean[].class) {
            throw new UnsupportedOperationException("Cannot read from buf: Class not implemented for sync: " + type);
        } else if (type == float[].class || type == Float[].class) {
            throw new UnsupportedOperationException("Cannot read from buf: Class not implemented for sync: " + type);
        } else if (type == double[].class || type == Double[].class) {
            throw new UnsupportedOperationException("Cannot read from buf: Class not implemented for sync: " + type);
        } else if (type == byte[].class || type == Byte[].class) {
            throw new UnsupportedOperationException("Cannot read from buf: Class not implemented for sync: " + type);
        } else if (type == char[].class || type == Character[].class) {
            throw new UnsupportedOperationException("Cannot read from buf: Class not implemented for sync: " + type);
        } else if (type == short[].class || type == Short[].class) {
            throw new UnsupportedOperationException("Cannot read from buf: Class not implemented for sync: " + type);
        } else if (type == int[].class || type == Integer[].class) {
            throw new UnsupportedOperationException("Cannot read from buf: Class not implemented for sync: " + type);
        } else if (type == String[].class) {
            throw new UnsupportedOperationException("Cannot read from buf: Class not implemented for sync: " + type);
        } else if (type.getSuperclass() == Object.class) {
            Object newInstance = f.get(fieldAccessor);
            for (Field newField : newInstance.getClass().getDeclaredFields())
                readFromByteBuf(newField, newInstance, buf);
        }
    }

    private static void writeToByteBuf(Field f, Object fieldAccessor, ByteBuf buf) throws IllegalAccessException {
        if (!Modifier.isPublic(f.getModifiers()))
            return;
        if (Modifier.isStatic(f.getModifiers()) != (fieldAccessor == null))
            return;
        //TODO ignore support

        Class<?> type = f.getType();
        if (type == boolean.class || type == Boolean.class) {
            buf.writeBoolean(f.getBoolean(fieldAccessor));
        } else if (type == float.class || type == Float.class) {
            buf.writeFloat(f.getFloat(fieldAccessor));
        } else if (type == double.class || type == Double.class) {
            buf.writeDouble(f.getDouble(fieldAccessor));
        } else if (type == byte.class || type == Byte.class) {
            buf.writeByte(f.getByte(fieldAccessor));
        } else if (type == char.class || type == Character.class) {
            buf.writeChar(f.getChar(fieldAccessor));
        } else if (type == short.class || type == Short.class) {
            buf.writeChar(f.getShort(fieldAccessor));
        } else if (type == int.class || type == Integer.class) {
            buf.writeInt(f.getInt(fieldAccessor));
        } else if (type == String.class) {
            ByteBufUtils.writeUTF8String(buf, (String) f.get(fieldAccessor));
        } else if (type == boolean[].class || type == Boolean[].class) {
            throw new UnsupportedOperationException("Cannot write to buf: Class not implemented for sync: " + type);
        } else if (type == float[].class || type == Float[].class) {
            throw new UnsupportedOperationException("Cannot write to buf: Class not implemented for sync: " + type);
        } else if (type == double[].class || type == Double[].class) {
            throw new UnsupportedOperationException("Cannot write to buf: Class not implemented for sync: " + type);
        } else if (type == byte[].class || type == Byte[].class) {
            throw new UnsupportedOperationException("Cannot write to buf: Class not implemented for sync: " + type);
        } else if (type == char[].class || type == Character[].class) {
            throw new UnsupportedOperationException("Cannot write to buf: Class not implemented for sync: " + type);
        } else if (type == short[].class || type == Short[].class) {
            throw new UnsupportedOperationException("Cannot write to buf: Class not implemented for sync: " + type);
        } else if (type == int[].class || type == Integer[].class) {
            throw new UnsupportedOperationException("Cannot write to buf: Class not implemented for sync: " + type);
        } else if (type == String[].class) {
            throw new UnsupportedOperationException("Cannot write to buf: Class not implemented for sync: " + type);
        } else if (type.getSuperclass() == Object.class) {
            Object newInstance = f.get(fieldAccessor);
            for (Field newField : newInstance.getClass().getDeclaredFields())
                writeToByteBuf(newField, newInstance, buf);
        }
    }
}
