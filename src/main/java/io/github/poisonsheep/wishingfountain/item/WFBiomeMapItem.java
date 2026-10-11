package io.github.poisonsheep.wishingfountain.item;

import io.github.poisonsheep.wishingfountain.config.CommonConfigs;
import io.github.poisonsheep.wishingfountain.util.ItemStackData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class WFBiomeMapItem extends WFMapItem {
    private static final Logger LOGGER = LoggerFactory.getLogger("WFBiomeMapItem");

    private final Identifier target_biome = Biomes.MUSHROOM_FIELDS.identifier();

    public WFBiomeMapItem(Item.Properties properties) {
        super(properties);
        type = "biome";
    }

    @Override
    protected Identifier getDefaultTarget() {
            return target_biome;
    }

    @Override
    protected ItemStack search(ItemStack stack, ServerLevel worldIn, Player player, int slot) {
        Identifier target = getTarget(stack);
        if(target == null) {
            return ItemStack.EMPTY;
        }
        MapSearchResult result = searchConcurrent(target, worldIn, stack, player);
        if(result.getResult() == InteractionResult.FAIL) {
            worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1F, 1F);
            return ItemStack.EMPTY;
        } else if(result.getResult() == InteractionResult.PASS) {
            return stack;
        } else {
            BlockPos corner = result.getObject();
            BlockPos found = calculateBiomeCenter(worldIn, corner, target);
            worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1F, 1F);
            return createMap(worldIn, found, target);
        }
    }

    //并发搜索模式
    protected MapSearchResult searchConcurrent(Identifier targetBiome, ServerLevel worldIn, ItemStack stack, Player player) {
        var tag = ItemStackData.get(stack);
        int centerX = tag.getIntOr(SOURCE_X, 0);
        int centerZ = tag.getIntOr(SOURCE_Z, 0);
        BlockPos centerPos = new BlockPos(centerX, 64, centerZ);
        Key key = new Key(GlobalPos.of(worldIn.dimension(), centerPos), targetBiome);
        if(COMPUTING.contains(key)) {
            return MapSearchResult.pass(BlockPos.ZERO);
        } else if(RESULTS.containsKey(key)) {
            var ret = RESULTS.get(key);
            if(ret.getResult() == InteractionResult.PASS) {
                return MapSearchResult.fail(BlockPos.ZERO);
            }
            return ret;
        } else {
            ItemStack dummy = stack.copy();
            EXECUTORS.submit(() -> {
                COMPUTING.add(key);
                RESULTS.put(key, searchIterative(targetBiome, dummy, worldIn, player));
                COMPUTING.remove(key);
            });
            return MapSearchResult.pass(BlockPos.ZERO);
        }
    }

    protected MapSearchResult searchIterative(Identifier targetBiome, ItemStack stack, ServerLevel worldIn, Player player) {
        BlockPos startPos = new BlockPos(
                ItemStackData.get(stack).getIntOr(SOURCE_X, 0),
                player.getBlockY(),
                ItemStackData.get(stack).getIntOr(SOURCE_Z, 0)
        );
        return searchBiome(worldIn, startPos, targetBiome);
    }

    private BlockPos calculateBiomeCenter(ServerLevel worldIn, BlockPos biomeCorner, Identifier biome) {
        ServerChunkCache cache = worldIn.getChunkSource();
        BiomeSource source = cache.getGenerator().getBiomeSource();
        Climate.Sampler sampler = cache.randomState().sampler();
        int biomeNorth = 0;
        int biomeSouth = 0;
        int biomeEast = 0;
        int biomeWest = 0;
        BlockPos yCentered;
        int biomeUp = 0;
        int biomeDown = 0;
        while (biomeUp < 32 && getNoiseBiomeAtPos(source, biomeCorner.above(biomeUp), sampler).is(biome)) {
            biomeUp += 8;
        }
        while (biomeDown < 64 && getNoiseBiomeAtPos(source, biomeCorner.below(biomeDown), sampler).is(biome)) {
            biomeDown += 8;
        }
        yCentered = biomeCorner.atY((int) (Math.floor(biomeUp * 0.25F)) - biomeDown);
        while (biomeNorth < 800 && getNoiseBiomeAtPos(source, yCentered.north(biomeNorth), sampler).is(biome)) {
            biomeNorth += 8;
        }
        while (biomeSouth < 800 && getNoiseBiomeAtPos(source, yCentered.south(biomeSouth), sampler).is(biome)) {
            biomeSouth += 8;
        }
        while (biomeEast < 800 && getNoiseBiomeAtPos(source, yCentered.east(biomeEast), sampler).is(biome)) {
            biomeEast += 8;
        }
        while (biomeWest < 800 && getNoiseBiomeAtPos(source, yCentered.west(biomeWest), sampler).is(biome)) {
            biomeWest += 8;
        }
        return yCentered.offset(biomeEast - biomeWest, 0, biomeSouth - biomeNorth);
    }

    private Holder<Biome> getNoiseBiomeAtPos(BiomeSource source, BlockPos pos, Climate.Sampler sampler){
        return source.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2, sampler);
    }

    public static MapSearchResult searchBiome(ServerLevel worldIn, BlockPos startPos, Identifier targetBiome) {
        int sourceX = startPos.getX();
        int sourceZ = startPos.getZ();
        int y = startPos.getY();
        int step = 32;
        int radius = CommonConfigs.SEARCHING_RADIUS.get();
        int maxLegs = 4 * Math.floorDiv(radius, step);

        ResourceKey<Biome> targetKey = ResourceKey.create(Registries.BIOME, targetBiome);

        BlockPos hit = checkBiomeAt(sourceX, sourceZ, y, worldIn, targetKey);
        if (hit != null) {
            return MapSearchResult.success(hit);
        }

        int x = 0, z = 0, leg = 0, legIndex = 0;

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            Direction dir = DIRECTIONS[(leg + 4) % 4];
            BlockPos cursor = new BlockPos(x, 0, z).relative(dir);
            x = cursor.getX();
            z = cursor.getZ();
            int legSize = leg / 2 + 1;

            if (legIndex >= legSize) {
                if (leg > maxLegs)
                    return MapSearchResult.fail(BlockPos.ZERO);
                leg++;
                legIndex = 0;
            }
            legIndex++;

            int testX = sourceX + x * step;
            int testZ = sourceZ + z * step;

            hit = checkBiomeAt(testX, testZ, y, worldIn, targetKey);
            if (hit != null) {
                return MapSearchResult.success(hit);
            }
        }
        return MapSearchResult.fail(BlockPos.ZERO);
    }

    private static BlockPos checkBiomeAt(int testX, int testZ, int playerY, ServerLevel worldIn, ResourceKey<Biome> targetKey) {
        int[] searchedHeights = Mth.outFromOrigin(playerY, worldIn.getMinY() + 1, worldIn.getMaxY(), 64).toArray();
        int quartX = QuartPos.fromBlock(testX);
        int quartZ = QuartPos.fromBlock(testZ);
        ServerChunkCache cache = worldIn.getChunkSource();
        BiomeSource source = cache.getGenerator().getBiomeSource();
        Climate.Sampler sampler = cache.randomState().sampler();

        for (int testY : searchedHeights) {
            int quartY = QuartPos.fromBlock(testY);
            Holder<Biome> holder = source.getNoiseBiome(quartX, quartY, quartZ, sampler);
            if (holder.is(targetKey)) {
                return new BlockPos(testX, testY, testZ);
            }
        }
        return null;
    }

    public static void searchBiomeAsync(ServerLevel worldIn, BlockPos startPos, Identifier targetBiome, Consumer<MapSearchResult> callback) {
        EXECUTORS.submit(() -> {
            try {
                MapSearchResult result = searchBiome(worldIn, startPos, targetBiome);
                callback.accept(result);
            } catch (Exception e) {
                callback.accept(MapSearchResult.fail(BlockPos.ZERO));
            }
        });
    }

    public static void setTarget(ItemStack itemStack, String target) {
        ItemStackData.update(itemStack, tag -> tag.putString(TARGET, target));
    }

    private record Key(GlobalPos pos, Identifier target) {}

    private static final Map<Key, MapSearchResult> RESULTS = new ConcurrentHashMap<>();
    private static final Set<Key> COMPUTING = ConcurrentHashMap.newKeySet();
    protected static final ExecutorService EXECUTORS = Executors.newFixedThreadPool(2);
}
