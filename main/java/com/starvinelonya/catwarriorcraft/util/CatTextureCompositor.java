package com.starvinelonya.catwarriorcraft.util;

import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CatTextureCompositor {

    public static final String[] BASE_TEXTURES = {"white_base", "black_base"};
    public static final String[] LEFT_EYE_TEXTURES = {"no_left_eye", "white_left_eye", "black_left_eye"};
    public static final String[] RIGHT_EYE_TEXTURES = {"no_right_eye", "white_right_eye", "black_right_eye"};


    public static void generateAll() {
        for (String base : BASE_TEXTURES) {
            for (String left : LEFT_EYE_TEXTURES) {
                for (String right : RIGHT_EYE_TEXTURES) {
                    compose(base, left, right);
                    deleteConfigPng(base + "_" + left);
                    deleteConfigPng(base + "_" + right);
                    deleteConfigPng(left + "_" + right);
                }
            }
        }
    }

    public static ResourceLocation getCatTexture(String catTexture) {
        return ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "cat/" + catTexture);
    }
    private static String compose(String bg, String fg) {
        try {
            ModPngCompositor.compositeTexture(bg, fg);
        } catch (IOException e) {
            CatWarriorCraft.LOGGER.error("Failed to compose texture: " + bg + "_" + fg);
            return "broken";
        }
        return bg + "_"+  fg;
    }

    public static String compose(String base, String ovl1, String ovl2) {
        String composed1 = compose(base, ovl1);
        return compose(composed1, ovl2);
    }
    public static String composeName(String base, String ovl1, String ovl2) {
        return  base + "_" + ovl1 + "_" + ovl2;
    }

    public static String readConfigPng(String fileName) throws IOException {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("catwarriorcraft/generated_textures/" + fileName + ".png");
        if (!Files.exists(configPath)) {
            throw new IOException("Config file not found: " + configPath);
        }
        return Files.readString(configPath);
    }


    public static boolean deleteConfigPng(String fileName){
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("catwarriorcraft/generated_textures/" + fileName + ".png");
        if (!Files.exists(configPath)) {
            CatWarriorCraft.LOGGER.info("Config file not found: " + configPath);
            return false;
        }
        try {
            Files.delete(configPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
