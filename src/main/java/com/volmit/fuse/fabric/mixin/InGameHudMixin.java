package com.volmit.fuse.fabric.mixin;

import com.volmit.fuse.fabric.Fuse;
import com.volmit.fuse.fabric.management.data.Project;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    private final List<String> building = new ArrayList<>();
    @Shadow
    @Final
    private SubtitlesHud subtitlesHud;
    int llbuf = 0;

    @Inject(method = "render", at = @At("RETURN"))
    public void onRender(MatrixStack matrices, float tickDelta, CallbackInfo info) {
        for(Project i : Fuse.service.getWorkspace().getProjects()) {
            if(i.isBuilding()) {
                building.add(i.getName());
            }
        }
        Map<String, String> k = Fuse.service.getDevServer().getDebugKeys();

        if(!k.isEmpty()) {
            int w = MinecraftClient.getInstance().getWindow().getWidth();
            List<String> msgs = new ArrayList<>();
            int ll = 0;
            int h = MinecraftClient.getInstance().textRenderer.fontHeight;

            for(String i : k.keySet())
            {
                msgs.add(i + ": " + k.get(i));
            }

            for(String i : msgs) {
                ll = Math.max(ll, MinecraftClient.getInstance().textRenderer.getWidth(i));
            }

            if(llbuf > ll) {
                ll = llbuf;
            }

            else {
                llbuf = ll;
            }

            llbuf--;

            for(String i : msgs) {
                MinecraftClient.getInstance().textRenderer.draw(matrices, i, 5, 5 + (h * msgs.indexOf(i)), 0xAAAAAA);
            }
        }

        if(building.isEmpty()) {
            return;
        }

        MinecraftClient.getInstance().textRenderer.draw(matrices, "Building " + String.join(", ", building), 5, 5, -1);
        MinecraftClient.getInstance().textRenderer.draw(matrices, Fuse.lastLog, 5, 15, -1);
        building.clear();
    }
}
