package at.nbsgames.customhotbar;

import at.nbsgames.customhotbar.config.Hotbar3x3Config;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.spongepowered.asm.mixin.Unique;

public class Hotbar3x3Client implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("customhotbar");

  private static Hotbar3x3Config config;
  public static Hotbar3x3Config getConfig(){
    return config;
  }

  public static boolean moveUIDown(){
    return (Hotbar3x3Client.getConfig().moveUIDDown ||
       Hotbar3x3Client.getConfig().hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE ||
       Hotbar3x3Client.getConfig().hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) && Hotbar3x3Client.getConfig().hotbarMode != Hotbar3x3Config.HotbarMode.VANILLA;
  }

  public static boolean isCompactOn(){
    return Hotbar3x3Client.getConfig().hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT && Hotbar3x3Client.getConfig().hotbarMode != Hotbar3x3Config.HotbarMode.VANILLA;
  }

  public static boolean isBottomMiddle(){
    return (Hotbar3x3Client.getConfig().hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT || Hotbar3x3Client.getConfig().hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE) && Hotbar3x3Client.getConfig().hotbarMode != Hotbar3x3Config.HotbarMode.VANILLA;
  }

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Customizable Hotbar");
		AutoConfig.register(Hotbar3x3Config.class, GsonConfigSerializer::new);
    config = AutoConfig.getConfigHolder(Hotbar3x3Config.class).getConfig();
		LOGGER.info("Initialized Customizable Hotbar");
	}
}