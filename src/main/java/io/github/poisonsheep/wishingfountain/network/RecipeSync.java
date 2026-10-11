package io.github.poisonsheep.wishingfountain.network;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipeSerializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.ArrayList;
import java.util.List;

/** Full custom recipes are not included in vanilla's 26.1 recipe display packets. */
public final class RecipeSync {
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(Payload.TYPE, Payload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> send(handler.player));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            if (success) server.getPlayerList().getPlayers().forEach(RecipeSync::send);
        });
    }

    private static void send(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, Payload.TYPE)) return;
        List<RecipeHolder<WFRecipe>> recipes = new ArrayList<>();
        for (var holder : player.level().getServer().getRecipeManager().getRecipes()) {
            if (holder.value() instanceof WFRecipe recipe) recipes.add(new RecipeHolder<>(holder.id(), recipe));
        }
        ServerPlayNetworking.send(player, new Payload(List.copyOf(recipes)));
    }

    public record Payload(List<RecipeHolder<WFRecipe>> recipes) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(WishingFountain.id("recipes"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.of(
                (buffer, payload) -> {
                    buffer.writeVarInt(payload.recipes.size());
                    for (var holder : payload.recipes) {
                        buffer.writeIdentifier(holder.id().identifier());
                        WFRecipeSerializer.STREAM_CODEC.encode(buffer, holder.value());
                    }
                }, buffer -> {
                    int size = buffer.readVarInt();
                    if (size < 0 || size > 65535) throw new IllegalArgumentException("Invalid fountain recipe count");
                    List<RecipeHolder<WFRecipe>> recipes = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        var id = ResourceKey.create(Registries.RECIPE, buffer.readIdentifier());
                        recipes.add(new RecipeHolder<>(id, WFRecipeSerializer.STREAM_CODEC.decode(buffer)));
                    }
                    return new Payload(List.copyOf(recipes));
                });

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
