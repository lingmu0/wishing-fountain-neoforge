package io.github.poisonsheep.wishingfountain.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfigs {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Integer> SEARCHING_RADIUS;

    public static final ModConfigSpec.ConfigValue<String> TRIGGER_ITEM;

    public static final ModConfigSpec.ConfigValue<String> REQUIRED_BLUEPRINT;

    static {
        BUILDER.push("Wishing Fountain Configs");
        TRIGGER_ITEM = BUILDER
                .comment("Transforms multi-block structure items. DEFAULT: minecraft:heart_of_the_sea")
                .define("trigger item", "minecraft:heart_of_the_sea");
        REQUIRED_BLUEPRINT = BUILDER
                .comment("Blueprint required to build the fountain (requires Butday's Blueprint mod). Leave empty to disable. DEFAULT: butdaysblueprint:blueprint/residual_chapter_of_atlantis")
                .define("required blueprint", "butdaysblueprint:blueprint/residual_chapter_of_atlantis");
        BUILDER.pop();

        BUILDER.push("Buddelschiff Configs");
        SEARCHING_RADIUS = BUILDER
                .comment("Maximum radius of buddelschiff search. DEFAULT: 10000")
                .define("searching radius",10000);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
