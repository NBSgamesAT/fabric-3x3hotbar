package at.nbsgames.customhotbar.client;

import at.nbsgames.customhotbar.EnumPixelMagicNumbers;
import at.nbsgames.customhotbar.config.Hotbar3x3Config;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Function;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Unique
	private Hotbar3x3Config hotbarConfig;
	@Unique
	private static Hotbar3x3Config staticHotbarConfig;

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

	@Inject(at = @At("TAIL"), method = "<init>")
	private void constructorMixin(CallbackInfo ci) {
		this.hotbarConfig = AutoConfig.getConfigHolder(Hotbar3x3Config.class).getConfig();
		InGameHudMixin.staticHotbarConfig = AutoConfig.getConfigHolder(Hotbar3x3Config.class).getConfig();
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
		} else if (this.isBottomMiddle()) {
			return (context.getScaledWindowWidth() - _3X3_HOTBAR_WIDTH) / 2;
		} else {
			return context.getScaledWindowWidth() - _3X3_HOTBAR_WIDTH - this.hotbarConfig.hOffset;
		}
	}

	@Unique
	private int get3x3HotbarTopLeftY(DrawContext context) {
		if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.TOP_LEFT || this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.TOP_RIGHT) {
			return this.hotbarConfig.vOffset;
		} else if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE) {
			return context.getScaledWindowHeight() - SINGLE_HOTBAR_HEIGHT - (HOTBAR_SLOT_SIZE * 2) - EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset();
		} else if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) {
			return context.getScaledWindowHeight() - SINGLE_HOTBAR_HEIGHT - (HOTBAR_SLOT_SIZE * 2) - EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_COMPACT_POSITION.getOffset();
		} else {
			return context.getScaledWindowHeight() - _3X3_HOTBAR_HEIGHT - this.hotbarConfig.vOffset;
		}
	}

	@Unique
	private boolean moveUIDown(){
		if((this.hotbarConfig.moveUIDDown ||
				this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE ||
				this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) && this.hotbarConfig.hotbarMode != Hotbar3x3Config.HotbarMode.VANILLA){
			return true;
		}
		return false;
	}

	@Unique
	private boolean isCompactOn(){
    return this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT && this.hotbarConfig.hotbarMode != Hotbar3x3Config.HotbarMode.VANILLA;
  }

	@Unique
	private boolean isBottomMiddle(){
		return (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT || this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE) && this.hotbarConfig.hotbarMode != Hotbar3x3Config.HotbarMode.VANILLA;
	}

	@Unique
	private void offhandSlotHeight(Args args, int i){
		if(this.isCompactOn()){
			args.set(i, (int) args.get(i) - EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() - EnumPixelMagicNumbers.UI_OFFHAND_SLOT_OFFSET_ON_COMPACT.getOffset());
		}
		else{
			args.set(i, (int) args.get(i) - EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset());
		}
	}

	@Redirect(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
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
					HOTBAR_BORDER_THICKNESS + row * SINGLE_HOTBAR_WIDTH_BORDERLESS / 3 /* u */, HOTBAR_BORDER_THICKNESS /* v */,
					get3x3HotbarTopLeftX(context) + HOTBAR_BORDER_THICKNESS, get3x3HotbarTopLeftY(context) + HOTBAR_BORDER_THICKNESS + row * HOTBAR_SLOT_SIZE,
					_3X3_HOTBAR_WIDTH_BORDERLESS /* texture output width */, HOTBAR_SLOT_SIZE /* texture output height */
				);
			}
		}
	}

	@Redirect(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1))
	private void drawHotbarSelectionTexture(DrawContext context, Function<Identifier, RenderLayer> renderLayers, Identifier _texture, int _x, int _y, int _width, int _height) {
		int selectedSlot = this.getCameraPlayer().getInventory().selectedSlot;

		context.drawGuiTexture(
			renderLayers, HOTBAR_SELECTION_TEXTURE, getHotbarSlotTopLeftX(context, selectedSlot) - 2, getHotbarSlotTopLeftY(context, selectedSlot) - 2, 24, 23
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

	@ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 0), index = 1)
	private int modifyRenderHotbarItemX(DrawContext context, int _x, int _y, RenderTickCounter _tickCounter, PlayerEntity _player, ItemStack _stack, int seed) {
		int itemIndex = seed - 1; // could also be something like (x - context.getScaledWindowWidth() / 2 + 90 - 2) / 20
		return getHotbarItemTopLeftX(context, itemIndex);
	}

	@ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 0), index = 2)
	private int modifyRenderHotbarItemY(DrawContext context, int _x, int _y, RenderTickCounter _tickCounter, PlayerEntity _player, ItemStack _stack, int seed) {
		int itemIndex = seed - 1; // could also be something like (x - context.getScaledWindowWidth() / 2 + 90 - 2) / 20
		return getHotbarItemTopLeftY(context, itemIndex);
	}

	@ModifyArgs(method = "renderHotbar",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V")
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0)
	)
	private void modifyOffhandSlotLeft(Args args){
		if (this.isBottomMiddle()) {
			args.set(2, (int) args.get(2) + (EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() * 2));
			this.offhandSlotHeight(args, 3);
		}
	}

	@ModifyArgs(method = "renderHotbar",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V")
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1)
	)
	private void modifyOffhandSlotRight(Args args){
		if (this.isBottomMiddle()) {
			args.set(2, (int) args.get(2) - (EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() * 2));
			this.offhandSlotHeight(args, 3);
		}
	}

	@ModifyArgs(method = "renderHotbar",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 0),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getAttackIndicator()Lnet/minecraft/client/option/SimpleOption;")
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 1)
	)
	private void modifyOffhandItemLeft(Args args){
		if (this.isBottomMiddle()) {
			args.set(1, (int) args.get(1) + (EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() * 2));
			this.offhandSlotHeight(args, 2);
		}
	}

	@ModifyArgs(method = "renderHotbar",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 0),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getAttackIndicator()Lnet/minecraft/client/option/SimpleOption;")
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 2)
	)
	private void modifyOffhandItemRight(Args args){
		if (this.isBottomMiddle()) {
			args.set(1, (int) args.get(1) - (EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() * 2));
			this.offhandSlotHeight(args, 2);
		}
	}

	// ------------------------------- All the NON HOT BAR RELATED STUFF.

	@ModifyVariable(method = "renderExperienceBar", at = @At("STORE"), ordinal=4)
	private int modifyExperienceBarY(int value) {
		if (this.moveUIDown()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return value;
	}

	@ModifyVariable(method="renderExperienceLevel", at=@At("STORE"), ordinal=2)
	private int modifyExperienceLevelY(int value){
		if (this.moveUIDown()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return value;
	}

	@ModifyVariable(method = "renderHealthBar", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private int modifyHealthBarY(int value){
		if (this.moveUIDown()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return value;
	}
	@ModifyVariable(method = "renderHealthBar", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private int modifyHealthBarX(int value){
		if (this.isCompactOn()) {
			return value - EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return value;
	}

	@ModifyVariable(method = "renderFood", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private int modifyFoodBarY(int value){
		if (this.moveUIDown()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return value;
	}
	@ModifyVariable(method = "renderFood", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private int modifyFoodBarX(int value){
		if (this.isCompactOn()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return value;
	}

	@ModifyVariable(method = "renderArmor", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static int modifyArmourY(int value){
		if (InGameHudMixin.staticHotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) return value;

		if (InGameHudMixin.staticHotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE || InGameHudMixin.staticHotbarConfig.moveUIDDown || InGameHudMixin.staticHotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return value;
	}

	@ModifyVariable(method = "renderArmor", at = @At("STORE"), ordinal = 7)
	private static int modifyArmourX(int value){
		if (InGameHudMixin.staticHotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) return value;

		if (InGameHudMixin.staticHotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) {
			return value - EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return value;
	}

	@ModifyVariable(method = "renderAirBubbles", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private int modifyAirBubblesY(int value){
		if (this.moveUIDown()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return value;
	}
	@ModifyVariable(method = "renderAirBubbles", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private int modifyAirBubblesX(int value){
		if (this.isCompactOn()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return value;
	}

	@ModifyVariable(method = "renderMountHealth", at = @At("STORE"), ordinal = 2)
	private int modifyMountHealthY(int value){
		if (this.moveUIDown()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return value;
	}
	@ModifyVariable(method = "renderMountHealth", at = @At("STORE"), ordinal = 3)
	private int modifyMountHealthX(int value){
		if (this.isCompactOn()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return value;
	}

	@ModifyVariable(method = "renderMountJumpBar", at = @At("STORE"), ordinal = 3)
	private int modifyMountJumpBarY(int value){
		if (this.moveUIDown()) {
			return value + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return value;
	}

	@ModifyVariable(method = "renderHeldItemTooltip", at = @At("STORE"), ordinal = 2)
	private int modifySelectedItemNameTextY(int value){
		if (this.moveUIDown()) {
			if(this.isCompactOn()){
				return value - EnumPixelMagicNumbers.UI_ITEM_TOOLTIP_OFFSET_ON_COMPACT_POSITION.getOffset();
			}
			else {
				return value - EnumPixelMagicNumbers.UI_ITEM_TOOLTIP_OFFSET_ON_BOTTOM_MIDDLE_POSITION.getOffset();
			}
		}
		return value;
	}

	@ModifyArg(method = "renderOverlayMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0), index = 1)
	private float modifyActionBarPositionY(float value){
		if (this.moveUIDown()) {
			if(this.isCompactOn()){
				return value - EnumPixelMagicNumbers.UI_OVERLAY_TEXT_OFFSET_ON_COMPACT_POSITION.getOffset();
			}
			else {
				return value - EnumPixelMagicNumbers.UI_OVERLAY_TEXT_OFFSET_ON_BOTTOM_MIDDLE_POSITION.getOffset();
			}
		}
		return value;
	}
}