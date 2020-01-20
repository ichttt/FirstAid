package ichttt.mods.firstaid.common.damagesystem.definition;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ichttt.mods.firstaid.common.damagesystem.EnumHeightOffset;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class DamageModelDefinition {
    private final ResourceLocation registryName;
    private final Multimap<EntityEquipmentSlot, DamageablePartDefinition> partDefinitions = MultimapBuilder.enumKeys(EntityEquipmentSlot.class).arrayListValues().build();
    private final Map<EnumHeightOffset, Float> heightOffsets = new EnumMap<>(EnumHeightOffset.class);

    private DamageModelDefinition(JsonObject obj) {
        this.registryName = new ResourceLocation(obj.get("reg_name").getAsString());
        for (JsonElement element : obj.get("parts").getAsJsonArray()) {
            DamageablePartDefinition definition = new DamageablePartDefinition(element.getAsJsonObject());
            this.partDefinitions.put(definition.getEquipmentSlot(), definition);
        }
        JsonObject height = obj.get("height").getAsJsonObject();
        for (EnumHeightOffset offset : EnumHeightOffset.values()) {
            this.heightOffsets.put(offset, height.get(offset.name().toLowerCase(Locale.ROOT)).getAsFloat());
        }
    }

    public static DamageModelDefinition parse(JsonObject obj) {
        if (obj.has("disable") && obj.get("disable").getAsBoolean())
            return null;
        else
            return new DamageModelDefinition(obj);
    }

    public Collection<DamageablePartDefinition> getPartDefinitions(EntityEquipmentSlot slot) {
        return partDefinitions.get(slot);
    }

    public Map<EnumHeightOffset, Float> getHeightOffsets() {
        return heightOffsets;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }
}
