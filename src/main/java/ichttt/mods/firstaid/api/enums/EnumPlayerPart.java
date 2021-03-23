/*
 * FirstAid API
 * Copyright (c) 2017-2020
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ichttt.mods.firstaid.api.enums;

import com.google.common.collect.ImmutableList;
import net.minecraft.inventory.EntityEquipmentSlot;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public enum EnumPlayerPart {
    HEAD(EntityEquipmentSlot.HEAD), LEFT_ARM(EntityEquipmentSlot.CHEST), LEFT_LEG(EntityEquipmentSlot.LEGS), LEFT_FOOT(EntityEquipmentSlot.FEET),
    BODY(EntityEquipmentSlot.CHEST), RIGHT_ARM(EntityEquipmentSlot.CHEST), RIGHT_LEG(EntityEquipmentSlot.LEGS), RIGHT_FOOT(EntityEquipmentSlot.FEET);

    public static final EnumPlayerPart[] VALUES = values();

    static {
        for (EnumPlayerPart value : VALUES) {
            List<EnumPlayerPart> neighbours = value.getNeighbours();
            if (neighbours.contains(value))
                throw new RuntimeException(value + " contains itself as a neighbour!");
            if (neighbours.isEmpty())
                throw new RuntimeException(value + " does not have any neighbours!");
            if (EnumSet.copyOf(neighbours).size() != neighbours.size())
                throw new RuntimeException(value + " neighbours contain the same part multiple times!");

            // Check that the parts can be reached by calling neighbours recursively
            Set<EnumPlayerPart> hopefullyAllParts = EnumSet.copyOf(neighbours);
            int oldSize = -1;
            while (oldSize != hopefullyAllParts.size()) {
                oldSize = hopefullyAllParts.size();
                Set<EnumPlayerPart> neighboursOfNeighbours = EnumSet.noneOf(EnumPlayerPart.class);
                for (EnumPlayerPart part : hopefullyAllParts) {
                    neighboursOfNeighbours.addAll(part.getNeighbours());
                }
                hopefullyAllParts.addAll(neighboursOfNeighbours);
            }
            if (hopefullyAllParts.size() != VALUES.length) {
                throw new RuntimeException(value + " could not read all player parts " + Arrays.toString(hopefullyAllParts.toArray(new EnumPlayerPart[0])));
            }
        }
    }

    public final byte id;
    private ImmutableList<EnumPlayerPart> neighbours;
    public final EntityEquipmentSlot slot;

    EnumPlayerPart(EntityEquipmentSlot slot) {
        this.id = (byte) (ordinal() + 1);
        this.slot = slot;
    }

    public ImmutableList<EnumPlayerPart> getNeighbours() {
        if (neighbours == null) { // Need to do lazy init to avoid crashes when initializing class
            synchronized (this) {
                if (neighbours == null) {
                    ImmutableList.Builder<EnumPlayerPart> builder = ImmutableList.builder();
                    builder.addAll(getNeighboursDown());
                    builder.addAll(getNeighboursUp());
                    builder.addAll(getNeighboursLeft());
                    builder.addAll(getNeighboursRight());
                    neighbours = builder.build();
                }
            }
        }
        return neighbours;
    }

    public static EnumPlayerPart fromID(int id) {
        switch (id) {
            case 1:
                return HEAD;
            case 2:
                return LEFT_ARM;
            case 3:
                return LEFT_LEG;
            case 4:
                return LEFT_FOOT;
            case 5:
                return BODY;
            case 6:
                return RIGHT_ARM;
            case 7:
                return RIGHT_LEG;
            case 8:
                return RIGHT_FOOT;
        }
        throw new IndexOutOfBoundsException("Invalid id " + id);
    }

    @Nonnull
    private List<EnumPlayerPart> getNeighboursUp() {
        switch (this) {
            case BODY:
                return singletonList(HEAD);
            case LEFT_LEG:
            case RIGHT_LEG:
                return singletonList(BODY);
            case LEFT_FOOT:
                return singletonList(LEFT_LEG);
            case RIGHT_FOOT:
                return singletonList(RIGHT_LEG);
            default:
                return emptyList();
        }
    }

    @Nonnull
    private List<EnumPlayerPart> getNeighboursDown() {
        switch (this) {
            case HEAD:
                return singletonList(BODY);
            case BODY:
                return Arrays.asList(LEFT_LEG, RIGHT_LEG);
            case LEFT_LEG:
                return singletonList(LEFT_FOOT);
            case RIGHT_LEG:
                return singletonList(RIGHT_FOOT);
            default:
                return emptyList();
        }
    }

    @Nonnull
    private List<EnumPlayerPart> getNeighboursLeft() {
        switch (this) {
            case RIGHT_ARM:
                return singletonList(BODY);
            case RIGHT_LEG:
                return singletonList(LEFT_LEG);
            case RIGHT_FOOT:
                return singletonList(LEFT_FOOT);
            case BODY:
                return singletonList(LEFT_ARM);
            default:
                return emptyList();
        }
    }

    @Nonnull
    private List<EnumPlayerPart> getNeighboursRight() {
        switch (this) {
            case LEFT_ARM:
                return singletonList(BODY);
            case LEFT_LEG:
                return singletonList(RIGHT_LEG);
            case LEFT_FOOT:
                return singletonList(RIGHT_FOOT);
            case BODY:
                return singletonList(RIGHT_ARM);
            default:
                return emptyList();
        }
    }

    @Deprecated //TODO remove in 1.17. This never worked as one expected
    public EnumPlayerPart getUp() {
        if (this.id == 5)
            throw new IndexOutOfBoundsException("There is no part up from " + this.id);
        return fromID(this.id - 1);
    }

    @Deprecated //TODO remove in 1.17. This never worked as one expected
    public EnumPlayerPart getDown() {
        if (this.id == 4)
            throw new IndexOutOfBoundsException("There is no part down from " + this.id);
        return fromID(this.id + 1);
    }

    @Deprecated //TODO remove in 1.17. This never worked as one expected
    public EnumPlayerPart getLeft() {
        return fromID(this.id - 4);
    }

    @Deprecated //TODO remove in 1.17. This never worked as one expected
    public EnumPlayerPart getRight() {
        return fromID(this.id + 4);
    }

}
