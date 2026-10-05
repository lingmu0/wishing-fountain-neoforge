package io.github.poisonsheep.wishingfountain.item;

import io.github.poisonsheep.wishingfountain.config.CommonConfigs;
import io.github.poisonsheep.wishingfountain.util.ItemStackData;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class WFStructureMap extends WFMapItem{
    private final Identifier target_structure = BuiltinStructures.END_CITY.identifier();

    public WFStructureMap() {
        super();
        type = "structure";
    }

    @Override
    protected Identifier getDefaultTarget() {
        return target_structure;
    }

    @Override
    protected ItemStack search(ItemStack stack, ServerLevel worldIn, Player player, int slot) {
        Identifier target = getTarget(stack);
        if(target == null) {
            return ItemStack.EMPTY;
        }
        var tag = ItemStackData.get(stack);
        int centerX = tag.getIntOr(SOURCE_X, 0);
        int centerZ = tag.getIntOr(SOURCE_Z, 0);
        BlockPos centerPos = new BlockPos(centerX, 64, centerZ);
        SearchKey key = new SearchKey(GlobalPos.of(worldIn.dimension(), centerPos), target.toString());

        if(COMPUTING.contains(key)) {
            return stack;
        }

        MapSearchResult result = RESULTS.get(key);
        if(result != null) {
            if(result.getResult() == InteractionResult.FAIL) {
                worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1F, 1F);
                return ItemStack.EMPTY;
            }
            BlockPos found = result.getObject();
            worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1F, 1F);
            return createMap(worldIn, found, target);
        }

        COMPUTING.add(key);
        EXECUTORS.submit(() -> {
            try {
                MapSearchResult locateResult = searchStructure(worldIn, centerPos, target);
                RESULTS.put(key, locateResult);
            } finally {
                COMPUTING.remove(key);
            }
        });
        return stack;
    }

    public static Structure getStructureForKey(ServerLevel level, Identifier key) {
        return getStructureRegistry(level).getOptional(key).orElse(null);
    }

    private static Registry<Structure> getStructureRegistry(ServerLevel level) {
        return level.registryAccess().lookup(Registries.STRUCTURE).orElseThrow();
    }

    public static Holder<Structure> getHolderForStructure(ServerLevel level, Structure structure) {
        Optional<ResourceKey<Structure>> optional = getStructureRegistry(level).getResourceKey(structure);
        return optional.flatMap(level.registryAccess().lookupOrThrow(Registries.STRUCTURE)::get).orElse(null);
    }

    public static MapSearchResult searchStructure(ServerLevel worldIn, BlockPos startPos, Identifier targetStructure) {
        Structure structure = getStructureForKey(worldIn, targetStructure);
        if (structure == null) {
            return MapSearchResult.fail(BlockPos.ZERO);
        }

        Holder<Structure> holder = getHolderForStructure(worldIn, structure);
        if (holder == null) {
            return MapSearchResult.fail(BlockPos.ZERO);
        }

        int radius = CommonConfigs.SEARCHING_RADIUS.get();
        Pair<BlockPos, Holder<Structure>> pair = worldIn.getChunkSource().getGenerator()
                .findNearestMapStructure(worldIn, HolderSet.direct(List.of(holder)), startPos, radius, false);
        if (pair != null) {
            return MapSearchResult.success(pair.getFirst());
        }
        return MapSearchResult.fail(BlockPos.ZERO);
    }


    public static void searchStructureAsync(ServerLevel worldIn, BlockPos startPos, Identifier targetStructure, Consumer<MapSearchResult> callback) {
        EXECUTORS.submit(() -> {
            try {
                MapSearchResult result = searchStructure(worldIn, startPos, targetStructure);
                callback.accept(result);
            } catch (Exception e) {
                callback.accept(MapSearchResult.fail(BlockPos.ZERO));
            }
        });
    }

    public static void setTarget(ItemStack itemStack, String target) {
        ItemStackData.update(itemStack, tag -> tag.putString(TARGET, target));
    }

    private record SearchKey(GlobalPos pos, String target) {}
    private static final Set<SearchKey> COMPUTING = ConcurrentHashMap.newKeySet();
    private static final Map<SearchKey, MapSearchResult> RESULTS = new ConcurrentHashMap<>();
    protected static final ExecutorService EXECUTORS = Executors.newFixedThreadPool(2);
}
