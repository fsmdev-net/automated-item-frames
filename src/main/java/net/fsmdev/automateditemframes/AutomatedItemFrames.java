package net.fsmdev.automateditemframes;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomatedItemFrames implements ModInitializer {
	public static final String MOD_ID = "automated-item-frames";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Automated Item Frames initialized");
	}
}