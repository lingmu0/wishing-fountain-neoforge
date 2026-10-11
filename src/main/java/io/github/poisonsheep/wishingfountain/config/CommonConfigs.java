package io.github.poisonsheep.wishingfountain.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Supplier;

public final class CommonConfigs {
    private static final Logger LOGGER = LoggerFactory.getLogger("wishing_fountain");
    private static final Properties VALUES = new Properties();
    public static final Supplier<String> TRIGGER_ITEM = () -> VALUES.getProperty("trigger_item", "minecraft:heart_of_the_sea");
    public static final Supplier<String> REQUIRED_BLUEPRINT = () -> VALUES.getProperty("required_blueprint", "butdaysblueprint:blueprint/residual_chapter_of_atlantis");
    private static int searchingRadius = 10000;
    public static final Supplier<Integer> SEARCHING_RADIUS = () -> searchingRadius;

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("wishing_fountain.properties");
        VALUES.setProperty("trigger_item", "minecraft:heart_of_the_sea");
        VALUES.setProperty("required_blueprint", "butdaysblueprint:blueprint/residual_chapter_of_atlantis");
        VALUES.setProperty("searching_radius", "10000");
        try {
            if (Files.exists(path)) {
                try (var reader = Files.newBufferedReader(path)) { VALUES.load(reader); }
            } else {
                Files.createDirectories(path.getParent());
                try (var writer = Files.newBufferedWriter(path)) {
                    VALUES.store(writer, "Wishing Fountain: trigger item, optional Butday's Blueprint requirement (empty disables), maximum map search radius");
                }
            }
            searchingRadius = Integer.parseInt(VALUES.getProperty("searching_radius").trim());
            if (searchingRadius < 1 || searchingRadius > 30000000) {
                searchingRadius = 10000;
                LOGGER.warn("Invalid searching_radius; using 10000");
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.warn("Could not read Wishing Fountain config; using default search radius", e);
            searchingRadius = 10000;
        }
    }
}
