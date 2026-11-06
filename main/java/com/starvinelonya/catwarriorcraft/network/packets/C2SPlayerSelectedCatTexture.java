package com.starvinelonya.catwarriorcraft.network.packets;

import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import com.starvinelonya.catwarriorcraft.registry.AttachmentRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SPlayerSelectedCatTexture(String texture_name) implements CustomPacketPayload {

    public static final Type<C2SPlayerSelectedCatTexture> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "texture_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPlayerSelectedCatTexture> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, C2SPlayerSelectedCatTexture::texture_name,
                    C2SPlayerSelectedCatTexture::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SPlayerSelectedCatTexture payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                player.setData(AttachmentRegistry.HAS_CHOSEN_CAT, true);
                player.setData(AttachmentRegistry.CAT_TEXTURE, payload.texture_name);
            }
        });
    }
}
