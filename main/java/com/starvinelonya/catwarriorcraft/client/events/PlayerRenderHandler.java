package com.starvinelonya.catwarriorcraft.client.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import com.starvinelonya.catwarriorcraft.client.CustomCatModel;
import com.starvinelonya.catwarriorcraft.client.CustomCatRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.jetbrains.annotations.UnknownNullability;



@EventBusSubscriber(modid = CatWarriorCraft.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PlayerRenderHandler {

    private static CustomCatModel catModel;

    private static CustomCatModel getOrInitCatModel() {
        if (catModel == null) {
            try {
                catModel = new CustomCatModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(CustomCatModel.LAYER_LOCATION)
                );
            } catch (Exception e) {
                CatWarriorCraft.LOGGER.error("Failed to initialize CustomCatModel", e);
                return null;
            }
        }
        return catModel;
    }


    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        // 取消原版玩家模型渲染
        event.setCanceled(true);

        // 获取事件数据
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = event.getPartialTick();

        // 从 Minecraft 模型系统获取已烘焙的猫模型
        Minecraft mc = Minecraft.getInstance();
        net.minecraft.client.model.geom.ModelPart root = mc.getEntityModels().bakeLayer(CustomCatModel.LAYER_LOCATION);
        CustomCatModel catModel = new CustomCatModel(root);

        // 计算动画参数（与原版 PlayerRenderer 逻辑一致）
        float limbSwing = computeLimbSwing(player, partialTick);
        float limbSwingAmount = computeLimbSwingAmount(player, partialTick);
        float ageInTicks = player.tickCount + partialTick;
        float netHeadYaw = computeNetHeadYaw(player, partialTick);
        float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

        // 先调用 prepareMobModel 设置基础姿态（潜行、骑乘等）
        catModel.prepareMobModel(player, limbSwing, limbSwingAmount, partialTick);
        // 再调用 setupAnim 设置行走/头部动画
        catModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 开始渲染
        poseStack.pushPose();

        // --- 定位与旋转 ---

        // 使用身体旋转（而非头部旋转）来旋转整个模型
        float bodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        // --- 缩放 ---
        // 猫模型比玩家小很多，放大以匹配玩家体型
        float scale = 1.8F;
        poseStack.scale(scale, scale, scale);

        // --- 垂直偏移 ---
        // 猫模型根骨骼 "bone" 的 PartPose.offset(0, 24, 0) 会在渲染时向下偏移 24 像素，
        // 我们需要向上补偿，使猫站在地面上
        poseStack.translate(0.0, -1.505, 0.0);

        // --- 渲染模型 ---
        var renderType = RenderType.entityCutoutNoCull(CustomCatRenderer.getTexture(player));
        catModel.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(renderType),
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                0xFFFFFFFF // 白色着色（ARGB），不改变纹理原色
        );

        poseStack.popPose();
    }

    // ==================== 动画参数计算辅助方法 ====================

    /**
     * 计算肢体摆动值（limb swing），驱动行走动画。
     * 使用插值确保帧间平滑。
     */
    private static float computeLimbSwing(LivingEntity entity, float partialTick) {
        float walkDist = Mth.lerp(partialTick, entity.walkDistO, entity.walkDist);
        return walkDist * 6.0F;
    }

    /**
     * 计算肢体摆动幅度（limb swing amount），控制摆动强度。
     * 站立时为 0，疾跑时接近 1。
     */
    private static float computeLimbSwingAmount(LivingEntity entity, float partialTick) {
        float currentWalkDist = entity.walkDist;
        float prevWalkDist = entity.walkDistO;
        float speed = (currentWalkDist - prevWalkDist) * 10.0F;
        return Mth.clamp(Mth.lerp(partialTick,
                        Math.abs(prevWalkDist - currentWalkDist) * 10.0F, Math.abs(speed)),
                0.0F, 1.0F);
    }

    /**
     * 计算净头部偏航角（head yaw - body yaw），用于头部独立旋转。
     */
    private static float computeNetHeadYaw(AbstractClientPlayer player, float partialTick) {
        float bodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float headRot = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float netYaw = headRot - bodyRot;
        // 骑乘时头部旋转不受身体限制
        if (player.isPassenger() && player.getVehicle() instanceof LivingEntity vehicle) {
            bodyRot = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            netYaw = headRot - bodyRot;
            // 限制范围
            netYaw = Mth.clamp(netYaw, -80.0F, 80.0F);
        }
        return netYaw;
    }


    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        event.setCanceled(true);
    }
}
