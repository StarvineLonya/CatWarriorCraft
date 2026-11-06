package com.starvinelonya.catwarriorcraft.network.packets;

import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import com.starvinelonya.catwarriorcraft.client.gui.CatTextureSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2COpenCatTextureSelectionScreen() implements CustomPacketPayload {

    public static final Type<S2COpenCatTextureSelectionScreen> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "open_cat_texture_selection_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2COpenCatTextureSelectionScreen> STREAM_CODEC =
            StreamCodec.unit(new S2COpenCatTextureSelectionScreen());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2COpenCatTextureSelectionScreen payload, IPayloadContext context) {
        context.enqueueWork(() -> {


            Minecraft.getInstance().setScreen(new CatTextureSelectionScreen());


        });
    }
}
