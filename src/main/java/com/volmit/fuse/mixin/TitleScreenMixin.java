package com.volmit.fuse.mixin;

import com.volmit.fuse.Fuse;
import com.volmit.fuse.screen.WorkspaceScreen;
import com.volmit.fuse.screen.widget.FuseProgressWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgetsNormal")
	private void addCustomButton(int y, int spacingY, CallbackInfo ci) {
		this.addDrawable(new FuseProgressWidget(7, 7, width-14, 9));
		this.addDrawableChild(ButtonWidget.builder(Text.of("\u2726"), (button) -> {
			if(Fuse.service.isReady()) {
				ServerAddress address = ServerAddress.parse("localhost:24627");
				ConnectScreen.connect(this, this.client, address, new ServerInfo("Fuse Server", address.toString(), false));
			}

			else {
				MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING, Text.of("Fuse"), Text.of("Fuse is not ready yet!")));
			}
		}).dimensions(this.width / 2 + 104, y + spacingY, 20, 20).build());
	}
}
