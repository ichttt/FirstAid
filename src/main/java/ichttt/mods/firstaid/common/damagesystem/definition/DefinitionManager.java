package ichttt.mods.firstaid.common.damagesystem.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import ichttt.mods.firstaid.FirstAid;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DefinitionManager {
    private static final Map<ResourceLocation, DamageModelDefinition> MAP = new HashMap<>();
    private static DamageModelDefinition defaultDefinition;

    public static void load(Path configDir) throws IOException {
        Path zipPath = configDir.resolve("fistaid_entitydata.zip");
        if (!Files.exists(zipPath)) {
            Files.copy(DefinitionManager.class.getResourceAsStream("/fistaid_entitydata_example.zip"), zipPath);
        }
        MAP.clear();
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            JsonParser parser = new JsonParser();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".json")) continue;
                JsonElement element = parser.parse(new InputStreamReader(zipFile.getInputStream(entry)));
                try {
                    DamageModelDefinition definition = DamageModelDefinition.parse(element.getAsJsonObject());
                    MAP.put(definition.getRegistryName(), definition);
                } catch (RuntimeException e) {
                    FirstAid.LOGGER.error("Error reading " + entry.getName() + ", skipping!", e);
                }
            }
        }
        defaultDefinition = MAP.get(new ResourceLocation("firstaid", "default"));
        if (defaultDefinition == null) throw new RuntimeException("Missing default definition!");
    }

    public static DamageModelDefinition getDefinition(ResourceLocation location) {
        return MAP.getOrDefault(location, defaultDefinition);
    }
}
