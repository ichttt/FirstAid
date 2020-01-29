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

package ichttt.mods.firstaid.common.potion;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FirstAidPotion extends Potion {
    public FirstAidPotion(boolean isBadEffectIn, int liquidColorIn, String name) {
        super(isBadEffectIn, liquidColorIn);
        setRegistryName(new ResourceLocation(FirstAid.MODID, name));
        setPotionName("item." + name + ".name");
    }

    @Nonnull
    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.2, 1.2, 1.2);
        mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(FirstAidItems.MORPHINE), Math.round(x / 1.2F) + 4, Math.round(y / 1.2F) + 5);
        GlStateManager.popMatrix();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(@Nonnull PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(FirstAidItems.MORPHINE), x + 4, y + 4);
        GlStateManager.popMatrix();
    }
}
