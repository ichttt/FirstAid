/*
 * FirstAid
 * Copyright (C) 2017-2018
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

package ichttt.mods.firstaid.common.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("FirstAid CoreMod")
@IFMLLoadingPlugin.SortingIndex(1025)
@IFMLLoadingPlugin.TransformerExclusions("ichttt.mods.firstaid.common.asm.")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class FirstAidCoremod implements IFMLLoadingPlugin {
    public static final Logger LOGGER = LogManager.getLogger("FirstAidCore");

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"ichttt.mods.firstaid.common.asm.PotionTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return "ichttt.mods.firstaid.common.asm.CoreModContainer";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
