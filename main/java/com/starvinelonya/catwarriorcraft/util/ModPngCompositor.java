package com.starvinelonya.catwarriorcraft.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * NeoForge 1.21 兼容的零AWT PNG合成器
 * 使用 ResourceLocation 定位输入资源，UUID 命名输出文件
 */
public class ModPngCompositor {


    /**
     * 从 ResourceManager 读取 PNG 并解码
     * @param manager  服务端或客户端的 ResourceManager
     * @param location 资源标识符，如 ResourceLocation.fromNamespaceAndPath("your_mod_id", "cat/base")
     *                 实际读取 assets/your_mod_id/textures/cat/base.png
     */
    public static PurePngCompositor.PngImage loadFromResource(
            ResourceManager manager, ResourceLocation location) throws IOException {
        // 纹理资源需手动拼接 textures/ 前缀和 .png 后缀
        ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(
                location.getNamespace(),
                "textures/" + location.getPath() + ".png"
        );

        try (InputStream in = manager.getResource(textureLoc)
                .orElseThrow(() -> new FileNotFoundException(
                        "Resource not found: " + textureLoc))
                .open()) {
            return PurePngCompositor.decodePng(in);
        }
    }

    /**
     * 合成两张资源图并以 UUID 命名输出到模组专属目录
     * @return 输出文件的绝对路径
     */
    public static Path compositeAndSave(
            ResourceManager manager,
            ResourceLocation bgLoc,
            ResourceLocation fgLoc,
            String outputName
    ) throws IOException {

        // 1. 加载资源
        PurePngCompositor.PngImage bg = loadFromResource(manager, bgLoc);
        PurePngCompositor.PngImage fg = loadFromResource(manager, fgLoc);

        // 2. Alpha 混合（复用上一轮的纯Java实现）
        PurePngCompositor.PngImage result = PurePngCompositor.composite(bg, fg);


        // 4. 输出到服务端安全的模组目录（非 assets、非世界存档）
        Path outputDir = Paths.get("config", CatWarriorCraft.MODID, "generated_textures");
        Files.createDirectories(outputDir);
        Path outputPath = outputDir.resolve(outputName + ".png");

        try (OutputStream out = Files.newOutputStream(outputPath)) {
            PurePngCompositor.encodePng(result, out);
        }

        return outputPath;
    }

    public static Path compositeTexture(String bgLoc, String fgLoc) throws IOException {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        ResourceLocation bg = ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "cat/" + bgLoc);
        ResourceLocation fg = ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "cat/" + fgLoc);
        String textureName = bgLoc + "_" + fgLoc;
        return compositeAndSave(manager, bg, fg, textureName);
    }

}