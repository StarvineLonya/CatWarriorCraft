package com.starvinelonya.catwarriorcraft;

import com.mojang.logging.LogUtils;
import com.starvinelonya.catwarriorcraft.client.CustomCatModel;
import com.starvinelonya.catwarriorcraft.registry.AttachmentRegistry;
import com.starvinelonya.catwarriorcraft.util.CatTextureCompositor;
import com.starvinelonya.catwarriorcraft.util.ModPngCompositor;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.io.IOException;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CatWarriorCraft.MODID)
public class CatWarriorCraft {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "catwarriorcraft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "catwarriorcraft" namespace
    // Create a Deferred Register to hold Items which will all be registered under the "catwarriorcraft" namespace
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "catwarriorcraft" namespace

    // Creates a new Block with the id "catwarriorcraft:example_block", combining the namespace and path
    // Creates a new BlockItem with the id "catwarriorcraft:example_block", combining the namespace and path

    // Creates a new food item with the id "catwarriorcraft:example_id", nutrition 1 and saturation 2

    // Creates a creative tab with the id "catwarriorcraft:example_tab" for the example item, that is placed after the combat tab


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CatWarriorCraft(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        // Register the Deferred Register to the mod event bus so items get registered
        // Register the Deferred Register to the mod event bus so tabs get registered

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (CatWarriorCraft) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        AttachmentRegistry.ATTACHMENT_TYPES.register(modEventBus);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }



    @SubscribeEvent
    public void onTest(ServerChatEvent event) {
        // Do Test

        CatTextureCompositor.generateAll();


    }





    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(CustomCatModel.LAYER_LOCATION, () -> CustomCatModel.createBodyLayer());


        }

    }
}
