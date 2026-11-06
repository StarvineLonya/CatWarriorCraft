package com.starvinelonya.catwarriorcraft.network.packets;

import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import com.starvinelonya.catwarriorcraft.client.gui.CatTextureSelectionScreen;
import com.starvinelonya.catwarriorcraft.registry.AttachmentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CPlayerSyncCatTexture(String texture_name) implements CustomPacketPayload {

    public static final Type<S2CPlayerSyncCatTexture> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "sync_cat_texture_to_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CPlayerSyncCatTexture> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, S2CPlayerSyncCatTexture::texture_name,
                    S2CPlayerSyncCatTexture::new
            );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CPlayerSyncCatTexture payload, IPayloadContext context) {
        context.enqueueWork(() -> {


            context.player().setData(AttachmentRegistry.CAT_TEXTURE, payload.texture_name);


        });
    }
}
