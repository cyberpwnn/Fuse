package com.volmit.fuse;

import com.volmit.fuse.management.FuseService;
import com.volmit.fuse.screen.WorkspaceScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Fuse implements ModInitializer {
	public static long ll = -1;
	public static String lastLog = "";
	public static FuseService service;
	public static final Logger LOG = LoggerFactory.getLogger("fuse");
	private static KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
		"Workspace Manager",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_SEMICOLON,
		"Fuse"
	));

	@Override
	public void onInitialize() {
		service = new FuseService(new File("fuse"));
		LOG.info("Starting Fuse Backend Service");
		service.open();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (keyBinding.wasPressed()) {
				client.setScreen(new WorkspaceScreen(client.currentScreen));
			}
		});
	}

	public static void log(String msg) {
		ll = System.currentTimeMillis();
		if(msg.equals("... done"))
		{
			return;
		}

		lastLog = msg;
		Fuse.LOG.info(msg);
	}

	public static void err(String msg) {
		ll = System.currentTimeMillis();
		if(msg.equals("... done"))
		{
			return;
		}

		lastLog = msg;
		Fuse.LOG.error(msg);
	}
}
