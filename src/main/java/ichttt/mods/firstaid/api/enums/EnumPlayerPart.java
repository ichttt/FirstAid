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
import net.minecraft.inventory.EquipmentSlotType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public enum EnumPlayerPart {
    HEAD(EquipmentSlotType.HEAD), LEFT_ARM(EquipmentSlotType.CHEST), LEFT_LEG(EquipmentSlotType.LEGS), LEFT_FOOT(EquipmentSlotType.FEET),
    BODY(EquipmentSlotType.CHEST), RIGHT_ARM(EquipmentSlotType.CHEST), RIGHT_LEG(EquipmentSlotType.LEGS), RIGHT_FOOT(EquipmentSlotType.FEET);

    public static final EnumPlayerPart[] VALUES = values();

    static {
        for (EnumPlayerPart value : VALUES) {
            List<EnumPlayerPart> neighbours = value.getNeighbours();
            if (neighbours.contains(value))
                throw new RuntimeException(value + " contains itself as a neighbour!");
            if (neighbours.isEmpty())
                throw new RuntimeException(value + " does not have any neighbours!");
            if (new HashSet<>(neighbours).size() != neighbours.size())
                throw new RuntimeException(value + " neighbours contain the same part multiple times!");

            // Check that the parts can be reached by calling neighbours recursively
            Set<EnumPlayerPart> hopefullyAllParts = new HashSet<>(neighbours);
            int oldSize = -1;
            while (oldSize != hopefullyAllParts.size()) {
                oldSize = hopefullyAllParts.size();
                Set<EnumPlayerPart> neighboursOfNeighbours = new HashSet<>();
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

    private ImmutableList<EnumPlayerPart> neighbours;
    public final EquipmentSlotType slot;

    EnumPlayerPart(EquipmentSlotType slot) {
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
        if (this == BODY)
            throw new RuntimeException("There is no part up from " + this);
        return VALUES[this.ordinal() - 1];
    }

    @Deprecated //TODO remove in 1.17. This never worked as one expected
    public EnumPlayerPart getDown() {
        if (this == LEFT_FOOT)
            throw new RuntimeException("There is no part down from " + this);
        return VALUES[this.ordinal() + 1];
    }

    @Deprecated //TODO remove in 1.17. This never worked as one expected
    public EnumPlayerPart getLeft() {
        return VALUES[this.ordinal() - 4];
    }

    @Deprecated //TODO remove in 1.17. This never worked as one expected
    public EnumPlayerPart getRight() {
        return VALUES[this.ordinal() + 4];
    }

}
