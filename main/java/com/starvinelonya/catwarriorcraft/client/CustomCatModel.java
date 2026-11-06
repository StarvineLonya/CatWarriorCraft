package com.starvinelonya.catwarriorcraft.client;

import com.starvinelonya.catwarriorcraft.CatWarriorCraft;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

import java.util.List;

public class CustomCatModel extends HierarchicalModel<AbstractClientPlayer> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CatWarriorCraft.MODID, "cat"), "cat");

    private final AnimationState walkState = new AnimationState();

    private boolean wasWalking = false;


    private final ModelPart bone;
    private final ModelPart body;
    private final ModelPart legs;
    private final ModelPart back_left_leg;
    private final ModelPart front_left_leg;
    private final ModelPart back_right_leg;
    private final ModelPart front_right_leg;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart tail2;

    public CustomCatModel(ModelPart root) {
        this.bone = root.getChild("bone");
        this.body = this.bone.getChild("body");
        this.legs = this.body.getChild("legs");
        this.back_left_leg = this.legs.getChild("back_left_leg");
        this.front_left_leg = this.legs.getChild("front_left_leg");
        this.back_right_leg = this.legs.getChild("back_right_leg");
        this.front_right_leg = this.legs.getChild("front_right_leg");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 根骨骼：offset(0, 24, 0) 将模型从原点下移到地面
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 身体：水平放置（xRot = π/2），位于 bone 内部
        PartDefinition body = bone.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-2.0F, 3.0F, -8.0F, 4.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -12.0F, -10.0F, 1.5708F, 0.0F, 0.0F));

        // 腿部组
        PartDefinition legs = body.addOrReplaceChild("legs",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(1.1F, 15.0F, -6.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition back_left_leg = legs.addOrReplaceChild("back_left_leg",
                CubeListBuilder.create().texOffs(8, 13)
                        .addBox(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition front_left_leg = legs.addOrReplaceChild("front_left_leg",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.9F, -10.0F));

        PartDefinition back_right_leg = legs.addOrReplaceChild("back_right_leg",
                CubeListBuilder.create().texOffs(8, 13)
                        .addBox(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.2F, 0.0F, 0.0F));

        PartDefinition front_right_leg = legs.addOrReplaceChild("front_right_leg",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.2F, -3.9F, -10.0F));

        // 头部：含面部、鼻子和耳朵
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -0.995F, -2.625F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 24).addBox(-1.5F, 0.985F, -3.625F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 10).addBox(-2.0F, -1.995F, 0.375F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(6, 10).addBox(1.0F, -1.995F, 0.375F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.995F, -1.375F, -1.5708F, 0.0F, 0.0F));

        // 尾巴第一段
        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 18.5F, -4.0F));

        PartDefinition tail_r1 = tail.addOrReplaceChild("tail_r1",
                CubeListBuilder.create().texOffs(0, 15)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, -0.5236F, 0.0F, 0.0F));

        // 尾巴第二段
        PartDefinition tail2 = tail.addOrReplaceChild("tail2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 7.0F, -3.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition tail2_r1 = tail2.addOrReplaceChild("tail2_r1",
                CubeListBuilder.create().texOffs(4, 15)
                        .addBox(-0.5F, 0.2F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -0.7284F, 0.302F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }
    @Override
    public void setupAnim(AbstractClientPlayer entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        applyHeadRotation(netHeadYaw, headPitch);



// setupAnim 中
        boolean walking = limbSwingAmount > 0.01F;
        if (walking && !wasWalking) {
            this.walkState.start((int) entity.tickCount); // 用 tickCount，不要用 ageInTicks
        }
        this.wasWalking = walking;

        if (walking) {
            this.animateWalk(CustomCatModelAnimation.CAT_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
        } else {

            this.applyStatic(CustomCatModelAnimation.CAT_IDLE);
        }
    }


    private void applyHeadRotation(float headYaw, float headPitch) {
        headPitch = Mth.clamp(headPitch, -25f, 45);
        headYaw = Mth.clamp(headYaw, -80f, 80);

        this.head.zRot = (float) (- headYaw * Math.PI / 180f);

        this.head.xRot = (float) (headPitch * Math.PI / 180f - Math.PI / 2);
    }
    private ModelPart findPart(String boneName) {
        return switch (boneName) {
            case "bone" -> bone;
            case "body" -> body;
            case "legs" -> legs;
            case "head" -> head;
            case "front_left_leg" -> front_left_leg;
            case "front_right_leg" -> front_right_leg;
            case "back_left_leg" -> back_left_leg;
            case "back_right_leg" -> back_right_leg;
            case "tail" -> tail;
            case "tail2" -> tail2;
            default -> null;
        };
    }

    @Override
    public ModelPart root() {
        return bone;
    }
}