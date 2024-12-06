package at.nbsgames.customhotbar;

import at.nbsgames.customhotbar.config.Hotbar3x3Config;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class Hotbar3x3Client implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("customhotbar");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Customizable Hotbar");
		AutoConfig.register(Hotbar3x3Config.class, GsonConfigSerializer::new);
		LOGGER.info("Initialized Customizable Hotbar");
	}
}