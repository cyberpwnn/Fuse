package com.volmit.fuse.fabric.mixin;

import com.volmit.fuse.fabric.Fuse;
import javafx.scene.paint.Color;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin( PlayerListHud.class)
public abstract class GuiPlayerTabOverlayMixin {
    @Final
    @Shadow
    private net.minecraft.client.MinecraftClient client;

    @Inject(method = "render", at = @At("RETURN"))
    public void render(MatrixStack matrixStack,
                       int i, Scoreboard scoreboard,
                       ScoreboardObjective scoreboardObjective,
                       CallbackInfo ci) {

        int w = this.client.getWindow().getWidth();
        int h = this.client.getWindow().getHeight();
        float scale = 0.5f;
        int ch = this.client.textRenderer.fontHeight+1;
        DrawableHelper.fill(matrixStack, 0, 0, w, h, 0x88000000);
        List<String> logs = Fuse.service.getDevServer().getLogs();

        if(logs.isEmpty()) {
            this.client.textRenderer.drawWithShadow(matrixStack, "<no logs>", 2, 2, 0xAAAAAA);
        }

        else {
            int hh = 0;
            int maxLogs = (int) Math.floor(((h / ch) - 2) / scale) - 20;
            for(int j = Math.max(0, logs.size() - maxLogs); j < logs.size(); j++)
            {
                String log = logs.get(j);
                matrixStack.scale(scale, scale, scale);
                this.client.textRenderer.draw(matrixStack, log, 2, 2 + (hh * ch), 0xAAAAAA);
                matrixStack.scale(1f / scale, 1f / scale, 1f / scale);
                hh++;
            }
        }
    }
}
