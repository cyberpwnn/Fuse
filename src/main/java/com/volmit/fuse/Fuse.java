package com.volmit.fuse;

import com.volmit.fuse.management.FuseService;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Fuse implements ModInitializer {
	public static FuseService service;
	public static final Logger LOGGER = LoggerFactory.getLogger("fuse");

	@Override
	public void onInitialize() {
		service = new FuseService(new File(MinecraftClient.getInstance().getLevelStorage().getSavesDirectory().getParent().toFile(), "fuse"));
		LOGGER.info("Starting Fuse Backend Service");
		service.open();
	}
}
