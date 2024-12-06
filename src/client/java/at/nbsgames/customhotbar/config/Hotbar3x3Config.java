package at.nbsgames.customhotbar.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;



@Config(name="customhotbar")
public class Hotbar3x3Config implements ConfigData {
    public enum HotbarMode {
        VANILLA("Vanilla"),
        THREE_BY_THREE("3x3"),
        THREE_BY_THREE_SEQUENTIAL("3x3 (sequential)");

        final private String name;

        HotbarMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public enum HotbarPosition {
        TOP_LEFT("Top left"),
        TOP_RIGHT("Top right"),
        BOTTOM_LEFT("Bottom left"),
        BOTTOM_RIGHT("Bottom right"),
        BOTTOM_MIDDLE( "Bottem Middle");

        final private String name;

        HotbarPosition(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @ConfigEntry.BoundedDiscrete(min=0, max=500)
    public int hOffset = 10;

    @ConfigEntry.BoundedDiscrete(min=0, max=500)
    public int vOffset = 10;

    // public boolean moveBars = true;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public HotbarMode hotbarMode = HotbarMode.THREE_BY_THREE;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public HotbarPosition hotbarPosition = HotbarPosition.BOTTOM_LEFT;
}
