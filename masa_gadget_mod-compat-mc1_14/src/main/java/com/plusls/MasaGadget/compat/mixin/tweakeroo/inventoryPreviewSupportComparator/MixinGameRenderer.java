package com.plusls.MasaGadget.compat.mixin.tweakeroo.inventoryPreviewSupportComparator;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStackCompat;
import com.plusls.MasaGadget.config.Configs;
import com.plusls.MasaGadget.tweakeroo.TraceUtil;
import com.plusls.MasaGadget.util.RenderUtil;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "render(FJ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"))
    private void postRender(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Level world = WorldUtils.getBestWorld(mc);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (world == null) {
            return;
        }

        if (!FeatureToggle.TWEAK_INVENTORY_PREVIEW.getBooleanValue() || !Hotkeys.INVENTORY_PREVIEW.getKeybind().isKeybindHeld() ||
                !Configs.inventoryPreviewSupportComparator) {
            return;
        }

        // 开始渲染
        BlockPos pos = TraceUtil.getTraceBlockPos();
        if (pos != null) {
            // 绕过线程检查
            BlockEntity blockEntity = world.getChunkAt(pos).getBlockEntity(pos);
            if (blockEntity instanceof ComparatorBlockEntity) {
                TextComponent literalText = new TextComponent(((ComparatorBlockEntity) blockEntity).getOutputSignal() + "");
                literalText.withStyle(ChatFormatting.GREEN);
                GlStateManager.disableDepthTest();
                PoseStackCompat poseStackCompat = new PoseStackCompat();
                RenderUtil.renderTextOnWorldCompat(poseStackCompat, camera, pos, literalText, true);
                GlStateManager.enableDepthTest();
            }
        }
    }
}
