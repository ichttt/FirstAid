package ichttt.mods.firstaid.common.damagesystem.definition;

import com.google.gson.JsonObject;
import net.minecraft.inventory.EntityEquipmentSlot;

public class DamageablePartDefinition {
    private final String name;
    private final int maxHealth;
    private final boolean causeDeath;
    private final EntityEquipmentSlot equipmentSlot;

    public DamageablePartDefinition(JsonObject object) {
        this.name = object.get("name").getAsString();
        this.maxHealth = object.get("max_health").getAsInt();
        this.causeDeath = object.get("cause_death").getAsBoolean();
        this.equipmentSlot = EntityEquipmentSlot.fromString(object.get("equipment_slot").getAsString());
        if (equipmentSlot.getSlotType() != EntityEquipmentSlot.Type.ARMOR)
            throw new IllegalArgumentException("Invalid slot " + equipmentSlot + " at name " + name);
    }

    public String getName() {
        return name;
    }

    public EntityEquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isCauseDeath() {
        return causeDeath;
    }
}
