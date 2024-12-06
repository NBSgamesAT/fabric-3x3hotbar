package at.nbsgames.customhotbar.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import at.nbsgames.customhotbar.config.Hotbar3x3Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Unique
	private Hotbar3x3Config hotbarConfig;

	@Unique
	private static final int HOTBAR_SLOT_SIZE = 20;

	@Unique
	private static final int HOTBAR_BORDER_THICKNESS = 1;

	@Unique
	private static final int HOTBAR_SLOT_BORDER_THICKNESS = 2;

	@Unique
	private static final int SINGLE_HOTBAR_WIDTH_BORDERLESS = 9 * HOTBAR_SLOT_SIZE;

	@Unique
	private static final int SINGLE_HOTBAR_WIDTH = SINGLE_HOTBAR_WIDTH_BORDERLESS + 2 * HOTBAR_BORDER_THICKNESS;

	@Unique
	private static final int SINGLE_HOTBAR_HEIGHT = HOTBAR_SLOT_SIZE + 2 * HOTBAR_BORDER_THICKNESS;

	@Unique
	private static final int _3X3_HOTBAR_WIDTH_BORDERLESS = SINGLE_HOTBAR_WIDTH_BORDERLESS / 3;

	@Unique
	private static final int _3X3_HOTBAR_HEIGHT_BORDERLESS = HOTBAR_SLOT_SIZE * 3;

	@Unique
	private static final int _3X3_HOTBAR_WIDTH = _3X3_HOTBAR_WIDTH_BORDERLESS + 2 * HOTBAR_BORDER_THICKNESS;

	@Unique
	private static final int _3X3_HOTBAR_HEIGHT = _3X3_HOTBAR_HEIGHT_BORDERLESS + 2 * HOTBAR_BORDER_THICKNESS;

	@Shadow
    protected abstract PlayerEntity getCameraPlayer();

	@Final
	@Shadow
	private static Identifier HOTBAR_TEXTURE;

	@Final
	@Shadow
	private static Identifier HOTBAR_SELECTION_TEXTURE;

	@Inject(at=@At("TAIL"), method="<init>")
	private void constructorMixin(CallbackInfo ci) {
		this.hotbarConfig = AutoConfig.getConfigHolder(Hotbar3x3Config.class).getConfig();
	}

	@Unique
	private int getHotbarSlotTopLeftX(DrawContext context, int hotbarItemIndex) {
		if (this.hotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) {
			int centerX = context.getScaledWindowWidth() / 2;
			return centerX - SINGLE_HOTBAR_WIDTH_BORDERLESS / 2 + hotbarItemIndex * HOTBAR_SLOT_SIZE;
		} else {
			int hIndex = hotbarItemIndex % 3;
			return get3x3HotbarTopLeftX(context) + HOTBAR_BORDER_THICKNESS + hIndex * HOTBAR_SLOT_SIZE;
		}
	}

	@Unique
	private int getHotbarItemTopLeftX(DrawContext context, int hotbarItemIndex) {
		return this.getHotbarSlotTopLeftX(context, hotbarItemIndex) + HOTBAR_SLOT_BORDER_THICKNESS;
	}

	@Unique
	private int getHotbarSlotTopLeftY(DrawContext context, int hotbarItemIndex) {
		if (this.hotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) {
			return context.getScaledWindowHeight() - HOTBAR_BORDER_THICKNESS - HOTBAR_SLOT_SIZE;
		}

		int vIndex = hotbarItemIndex / 3;

		if (this.hotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.THREE_BY_THREE) { // 789, 456, 123
			vIndex = 2 - vIndex;
		}

		return get3x3HotbarTopLeftY(context) + HOTBAR_BORDER_THICKNESS + vIndex * HOTBAR_SLOT_SIZE;
	}

	@Unique
	private int getHotbarItemTopLeftY(DrawContext context, int hotbarItemIndex) {
		return this.getHotbarSlotTopLeftY(context, hotbarItemIndex) + HOTBAR_SLOT_BORDER_THICKNESS;
	}

	@Unique
	private int get3x3HotbarTopLeftX(DrawContext context) {
		if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.TOP_LEFT || this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_LEFT) {
			return this.hotbarConfig.hOffset;
		} else {
			return context.getScaledWindowWidth() - _3X3_HOTBAR_WIDTH - this.hotbarConfig.hOffset;
		}
	}

	@Unique
	private int get3x3HotbarTopLeftY(DrawContext context) {
		if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.TOP_LEFT || this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.TOP_RIGHT) {
			return this.hotbarConfig.vOffset;
		} else {
			return context.getScaledWindowHeight() - _3X3_HOTBAR_HEIGHT - this.hotbarConfig.vOffset;
		}
	}

	@Redirect(method="renderHotbar", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
	private void drawHotbarTexture(DrawContext context, Function<Identifier, RenderLayer> renderLayers, Identifier _texture, int _x, int _y, int _width, int _height) {
		int centerX = context.getScaledWindowWidth() / 2;
		if (this.hotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) {
			context.drawGuiTexture(renderLayers, HOTBAR_TEXTURE, centerX - SINGLE_HOTBAR_WIDTH / 2, context.getScaledWindowHeight() - SINGLE_HOTBAR_HEIGHT, SINGLE_HOTBAR_WIDTH, SINGLE_HOTBAR_HEIGHT);
		} else {
			context.drawBorder(
					get3x3HotbarTopLeftX(context), get3x3HotbarTopLeftY(context),
					_3X3_HOTBAR_WIDTH, _3X3_HOTBAR_HEIGHT,
					0xFF000000
			);
			for (int row = 0; row < 3; row++) {
				context.drawGuiTexture(renderLayers, HOTBAR_TEXTURE,
						SINGLE_HOTBAR_WIDTH /* source texture width */, SINGLE_HOTBAR_HEIGHT /* source texture height */,
						HOTBAR_BORDER_THICKNESS + row * SINGLE_HOTBAR_WIDTH_BORDERLESS / 3 /* u */ , HOTBAR_BORDER_THICKNESS /* v */,
						get3x3HotbarTopLeftX(context) + HOTBAR_BORDER_THICKNESS, get3x3HotbarTopLeftY(context) + HOTBAR_BORDER_THICKNESS + row * HOTBAR_SLOT_SIZE,
						_3X3_HOTBAR_WIDTH_BORDERLESS /* texture output width */, HOTBAR_SLOT_SIZE /* texture output height */
				);
			}
		}
	}

	@Redirect(method="renderHotbar", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1))
	private void drawHotbarSelectionTexture(DrawContext context, Function<Identifier, RenderLayer> renderLayers, Identifier _texture, int _x, int _y, int _width, int _height) {
		int selectedSlot = this.getCameraPlayer().getInventory().selectedSlot;

		context.drawGuiTexture(
				renderLayers, HOTBAR_SELECTION_TEXTURE,  getHotbarSlotTopLeftX(context, selectedSlot) - 2, getHotbarSlotTopLeftY(context, selectedSlot) - 2, 24, 23
		);

		context.drawGuiTexture( // draw bottom border using top border of selection texture
				renderLayers,
				HOTBAR_SELECTION_TEXTURE,
				24, 23,
				0, 0,
				getHotbarSlotTopLeftX(context, selectedSlot) - 2, getHotbarSlotTopLeftY(context, selectedSlot) + HOTBAR_SLOT_SIZE + 1,
				24, 1
		);
	}

	@ModifyArg(method="renderHotbar", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal=0), index=1)
	private int modifyRenderHotbarItemX(DrawContext context, int _x, int _y, RenderTickCounter _tickCounter, PlayerEntity _player, ItemStack _stack, int seed) {
		int itemIndex = seed - 1; // could also be something like (x - context.getScaledWindowWidth() / 2 + 90 - 2) / 20
		return getHotbarItemTopLeftX(context, itemIndex);
	}

	@ModifyArg(method="renderHotbar", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal=0), index=2)
	private int modifyRenderHotbarItemY(DrawContext context, int _x, int _y, RenderTickCounter _tickCounter, PlayerEntity _player, ItemStack _stack, int seed) {
		int itemIndex = seed - 1; // could also be something like (x - context.getScaledWindowWidth() / 2 + 90 - 2) / 20
		return getHotbarItemTopLeftY(context, itemIndex);
	}
}