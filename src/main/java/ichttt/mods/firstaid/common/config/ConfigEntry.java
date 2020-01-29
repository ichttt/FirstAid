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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.minecraftforge.common.config.Config;
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
    private ByteBuf originalState;

    public ConfigEntry(@Nonnull Field f, @Nullable Object fieldAccessor, @Nonnull T annotation, boolean revertOnSave, @Nonnull UniqueProperty property) {
        this.field = f;
        this.fieldAccessor = fieldAccessor;
        this.revert = revertOnSave;
        this.property = property;
        this.annotation = annotation;
        if (revertOnSave) {
            //validate we can write to bytebuf
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            try {
                Object originalState = f.get(fieldAccessor);
                writeToBuf(buffer);
                readFromBuf(buffer);
                //noinspection ConstantConditions see above
                if (!originalState.equals(f.get(fieldAccessor)))
                    throw new RuntimeException("Error while writing and reading: Values do not match!");
                buffer.release();
            } catch (IllegalAccessException e) {
                buffer.release();
                throw new RuntimeException("Failed to access field " + f, e);
            } finally {
                hasRemoteData = false;
            }
            //valid
            this.originalState = ByteBufAllocator.DEFAULT.buffer();
            writeToBuf(this.originalState);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (originalState != null && originalState.refCnt() > 0)
            originalState.release(originalState.refCnt());
    }

    public boolean hasRemoteData() {
        return hasRemoteData;
    }

    public void revert() {
        if (revert && hasRemoteData) {
            readFromBuf(Objects.requireNonNull(this.originalState).copy());
            hasRemoteData = false;
        }
    }

    public void updateOrigState() {
        if (revert) {
            Objects.requireNonNull(originalState).release();
            originalState = ByteBufAllocator.DEFAULT.buffer();
            writeToBuf(originalState);
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

    private static boolean noSync(Field f, boolean isStatic) {
        if (!Modifier.isPublic(f.getModifiers()))
            return true;
        if (Modifier.isStatic(f.getModifiers()) != isStatic)
            return true;
        return f.isAnnotationPresent(Config.Ignore.class);
    }

    //Ugly stuff down here...

    private static void readFromByteBuf(Field f, Object fieldAccessor, ByteBuf buf) throws IllegalAccessException {
        if (noSync(f, fieldAccessor == null))
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
        if (noSync(f, fieldAccessor == null))
            return;

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
