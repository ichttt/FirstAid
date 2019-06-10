/*
 * FirstAid
 * Copyright (C) 2017-2019
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

package ichttt.mods.firstaid.common.potion;

import com.mojang.blaze3d.platform.GlStateManager;
import ichttt.mods.firstaid.FirstAid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirstAidPotion extends Effect {
    private final ItemStack stack;
    public FirstAidPotion(EffectType type, int liquidColorIn, Item item) {
        super(type, liquidColorIn);
        setRegistryName(new ResourceLocation(FirstAid.MODID, Objects.requireNonNull(item.getRegistryName()).getPath()));
        this.stack = new ItemStack(item);
    }

    @Nonnull
    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, int x, int y, float z) {
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1.2F, 1.2F, 1.2F);
        Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(stack, Math.round(x / 1.2F) + 4, Math.round(y / 1.2F) + 5);
        GlStateManager.popMatrix();
    }
}
