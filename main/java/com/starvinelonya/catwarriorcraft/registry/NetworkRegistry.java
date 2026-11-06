package com.starvinelonya.catwarriorcraft.registry;

import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import com.starvinelonya.catwarriorcraft.network.packets.C2SPlayerSelectedCatTexture;
import com.starvinelonya.catwarriorcraft.network.packets.S2COpenCatTextureSelectionScreen;
import com.starvinelonya.catwarriorcraft.network.packets.S2CPlayerSyncCatTexture;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CatWarriorCraft.MODID)
public class NetworkRegistry {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                S2COpenCatTextureSelectionScreen.TYPE,
                S2COpenCatTextureSelectionScreen.STREAM_CODEC,
                S2COpenCatTextureSelectionScreen::handle
        );
        registrar.playToClient(
                S2CPlayerSyncCatTexture.TYPE,
                S2CPlayerSyncCatTexture.STREAM_CODEC,
                S2CPlayerSyncCatTexture::handle
        );

        registrar.playToServer(
                C2SPlayerSelectedCatTexture.TYPE,
                C2SPlayerSelectedCatTexture.STREAM_CODEC,
                C2SPlayerSelectedCatTexture::handle
        );


    }
}
