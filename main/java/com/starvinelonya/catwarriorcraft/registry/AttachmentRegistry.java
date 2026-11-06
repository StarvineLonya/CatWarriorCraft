package com.starvinelonya.catwarriorcraft.registry;

import com.mojang.serialization.Codec;
import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AttachmentRegistry {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CatWarriorCraft.MODID);

    public static final Supplier<AttachmentType<Boolean>> HAS_CHOSEN_CAT = ATTACHMENT_TYPES.register(
            "has_chosen_cat",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<String>> CAT_TEXTURE = ATTACHMENT_TYPES.register(
            "cat_texture",
            () -> AttachmentType.builder(() -> "white_base_no_left_eye_no_right_eye")
                    .serialize(Codec.STRING)
                    .build()
    );


}
