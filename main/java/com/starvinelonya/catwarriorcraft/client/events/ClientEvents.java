package com.starvinelonya.catwarriorcraft.client.events;

import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import com.starvinelonya.catwarriorcraft.client.gui.CatTextureSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = CatWarriorCraft.MODID)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("cattexture") // 命令名，例如 /cattexture
                        .executes(context -> {
                            // 在主线程安全地打开屏幕
                            Minecraft.getInstance().execute(() -> {
                                Minecraft.getInstance().setScreen(new CatTextureSelectionScreen());
                            });
                            return 1; // 返回成功
                        })
        );
    }
}
