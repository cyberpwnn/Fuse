package com.volmit.fuse.mixin;

import com.volmit.fuse.screen.WorkspaceScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgetsNormal")
	private void addCustomButton(int y, int spacingY, CallbackInfo ci) {
		this.addDrawableChild(ButtonWidget.builder(Text.of("\u2726"), (button) -> {
			this.client.setScreen(new WorkspaceScreen(this));
		}).dimensions(this.width / 2 + 104, y + spacingY, 20, 20).build());
	}
}
