package com.starvinelonya.catwarriorcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import com.starvinelonya.catwarriorcraft.registry.AttachmentRegistry;
import com.starvinelonya.catwarriorcraft.util.ModPngCompositor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class CustomCatRenderer extends PlayerRenderer {



    public CustomCatRenderer(EntityRendererProvider.Context p_174557_) {
        super(p_174557_, false);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer p_117783_) {
        return ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "textures/cat/" + p_117783_.getData(AttachmentRegistry.CAT_TEXTURE.get()) + ".png");
    }

    public static ResourceLocation getTexture(AbstractClientPlayer p_117783_) {
        return ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "textures/cat/" + p_117783_.getData(AttachmentRegistry.CAT_TEXTURE.get()) + ".png");
    }

    @Override
    public void render(AbstractClientPlayer p_117788_, float p_117789_, float p_117790_, PoseStack p_117791_, MultiBufferSource p_117792_, int p_117793_) {
        super.render(p_117788_, p_117789_, p_117790_, p_117791_, p_117792_, p_117793_);
    }
}
