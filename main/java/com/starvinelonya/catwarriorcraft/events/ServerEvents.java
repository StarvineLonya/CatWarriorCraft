package com.starvinelonya.catwarriorcraft.events;

import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import com.starvinelonya.catwarriorcraft.network.packets.S2COpenCatTextureSelectionScreen;
import com.starvinelonya.catwarriorcraft.network.packets.S2CPlayerSyncCatTexture;
import com.starvinelonya.catwarriorcraft.registry.AttachmentRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CatWarriorCraft.MODID)

public class ServerEvents {


    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            if (!player.getData(AttachmentRegistry.HAS_CHOSEN_CAT)) {
                PacketDistributor.sendToPlayer(player, new S2COpenCatTextureSelectionScreen());


            }else{
                PacketDistributor.sendToPlayer(player, new S2CPlayerSyncCatTexture(player.getData(AttachmentRegistry.CAT_TEXTURE)));
            }

        }
    }
}
