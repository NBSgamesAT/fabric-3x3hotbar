package at.nbsgames.customhotbar.client;

import at.nbsgames.customhotbar.EnumPixelMagicNumbers;
import at.nbsgames.customhotbar.config.Hotbar3x3Config;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Hud.class)
public abstract class HudMixin {
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
	protected abstract Player getCameraPlayer();

	@Final
	@Shadow
	private static Identifier HOTBAR_SPRITE;

	@Final
	@Shadow
	private static Identifier HOTBAR_SELECTION_SPRITE;

	@Inject(at = @At("TAIL"), method = "<init>")
	private void constructorMixin(final Minecraft minecraft, CallbackInfo ci) {
		this.hotbarConfig = AutoConfig.getConfigHolder(Hotbar3x3Config.class).getConfig();
		HudMixin.staticHotbarConfig = AutoConfig.getConfigHolder(Hotbar3x3Config.class).getConfig();
	}

	@Unique
	private int getHotbarSlotTopLeftX(GuiGraphicsExtractor context, int hotbarItemIndex) {
		if (this.hotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) {
			int centerX = context.guiWidth() / 2;
			return centerX - SINGLE_HOTBAR_WIDTH_BORDERLESS / 2 + hotbarItemIndex * HOTBAR_SLOT_SIZE;
		} else {
			int hIndex = hotbarItemIndex % 3;
			return get3x3HotbarTopLeftX(context) + HOTBAR_BORDER_THICKNESS + hIndex * HOTBAR_SLOT_SIZE;
		}
	}

	@Unique
	private int getHotbarItemTopLeftX(GuiGraphicsExtractor context, int hotbarItemIndex) {
		return this.getHotbarSlotTopLeftX(context, hotbarItemIndex) + HOTBAR_SLOT_BORDER_THICKNESS;
	}

	@Unique
	private int getHotbarSlotTopLeftY(GuiGraphicsExtractor context, int hotbarItemIndex) {
		if (this.hotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) {
			return context.guiWidth() - HOTBAR_BORDER_THICKNESS - HOTBAR_SLOT_SIZE;
		}

		int vIndex = hotbarItemIndex / 3;

		if (this.hotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.THREE_BY_THREE) { // 789, 456, 123
			vIndex = 2 - vIndex;
		}

		return get3x3HotbarTopLeftY(context) + HOTBAR_BORDER_THICKNESS + vIndex * HOTBAR_SLOT_SIZE;
	}

	@Unique
	private int getHotbarItemTopLeftY(GuiGraphicsExtractor context, int hotbarItemIndex) {
		return this.getHotbarSlotTopLeftY(context, hotbarItemIndex) + HOTBAR_SLOT_BORDER_THICKNESS;
	}

	@Unique
	private int get3x3HotbarTopLeftX(GuiGraphicsExtractor context) {
		if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.TOP_LEFT || this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_LEFT) {
			return this.hotbarConfig.hOffset;
		} else if (this.isBottomMiddle()) {
			return (context.guiWidth() - _3X3_HOTBAR_WIDTH) / 2;
		} else {
			return context.guiWidth() - _3X3_HOTBAR_WIDTH - this.hotbarConfig.hOffset;
		}
	}

	@Unique
	private int get3x3HotbarTopLeftY(GuiGraphicsExtractor context) {
		if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.TOP_LEFT || this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.TOP_RIGHT) {
			return this.hotbarConfig.vOffset;
		} else if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE) {
			return context.guiHeight() - SINGLE_HOTBAR_HEIGHT - (HOTBAR_SLOT_SIZE * 2) - EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset();
		} else if (this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) {
			return context.guiHeight() - SINGLE_HOTBAR_HEIGHT - (HOTBAR_SLOT_SIZE * 2) - EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_COMPACT_POSITION.getOffset();
		} else {
			return context.guiHeight() - _3X3_HOTBAR_HEIGHT - this.hotbarConfig.vOffset;
		}
	}

	@Unique
	private boolean moveUIDown(){
    return (this.hotbarConfig.moveUIDDown ||
       this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE ||
       this.hotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) && this.hotbarConfig.hotbarMode != Hotbar3x3Config.HotbarMode.VANILLA;
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


	@Redirect(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
	private void drawHotbarTexture(GuiGraphicsExtractor context, RenderPipeline renderPipeline, Identifier _texture, int _x, int _y, int _width, int _height) {
		int centerX = context.guiWidth() / 2;
		if (this.hotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) {
			context.blitSprite(renderPipeline, HOTBAR_SPRITE, centerX - SINGLE_HOTBAR_WIDTH / 2, context.guiHeight() - SINGLE_HOTBAR_HEIGHT, SINGLE_HOTBAR_WIDTH, SINGLE_HOTBAR_HEIGHT);
		} else {
      drawBorderNbs(context, get3x3HotbarTopLeftX(context), get3x3HotbarTopLeftY(context), _3X3_HOTBAR_WIDTH, _3X3_HOTBAR_HEIGHT, 0xff000000);
      //context.draw
			for (int row = 0; row < 3; row++) {
				context.blitSprite(renderPipeline, HOTBAR_SPRITE,
					SINGLE_HOTBAR_WIDTH /* source texture width */, SINGLE_HOTBAR_HEIGHT /* source texture height */,
					HOTBAR_BORDER_THICKNESS + row * SINGLE_HOTBAR_WIDTH_BORDERLESS / 3 /* u */, HOTBAR_BORDER_THICKNESS /* v */,
					get3x3HotbarTopLeftX(context) + HOTBAR_BORDER_THICKNESS, get3x3HotbarTopLeftY(context) + HOTBAR_BORDER_THICKNESS + row * HOTBAR_SLOT_SIZE,
					_3X3_HOTBAR_WIDTH_BORDERLESS /* texture output width */, HOTBAR_SLOT_SIZE /* texture output height */
				);
			}
		}
	}

  @Unique
  public void drawBorderNbs(GuiGraphicsExtractor context, int leftX, int topY, int width, int height, int color){
    // Calculate Points
    int rightX = leftX + width - 1;
    int bottomY = topY + height - 1;
    context.horizontalLine(leftX, rightX, topY, color);
    context.horizontalLine(leftX, rightX, bottomY, color);

    context.verticalLine(rightX, topY, bottomY, color);
    context.verticalLine(leftX, topY, bottomY, color);
  }

	@Redirect(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
	private void drawHotbarSelectionTexture(GuiGraphicsExtractor context, RenderPipeline renderPipeline, Identifier _texture, int _x, int _y, int _width, int _height) {
		int selectedSlot = this.getCameraPlayer().getInventory().getSelectedSlot();

		context.blitSprite(
      renderPipeline, HOTBAR_SELECTION_SPRITE, getHotbarSlotTopLeftX(context, selectedSlot) - 2, getHotbarSlotTopLeftY(context, selectedSlot) - 2, 24, 23
		);

		context.blitSprite( // draw bottom border using top border of selection texture
      renderPipeline,
			HOTBAR_SELECTION_SPRITE,
			24, 23,
			0, 0,
			getHotbarSlotTopLeftX(context, selectedSlot) - 2, getHotbarSlotTopLeftY(context, selectedSlot) + HOTBAR_SLOT_SIZE + 1,
			24, 1
		);
	}

	@ModifyArg(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0), index = 1)
	private int modifyRenderHotbarItemX(GuiGraphicsExtractor context, int _x, int _y, DeltaTracker _tickCounter, Player _player, ItemStack _stack, int seed) {
		int itemIndex = seed - 1; // could also be something like (x - context.guiWidth() / 2 + 90 - 2) / 20
		return getHotbarItemTopLeftX(context, itemIndex);
	}

	@ModifyArg(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0), index = 2)
	private int modifyRenderHotbarItemY(GuiGraphicsExtractor context, int _x, int _y, DeltaTracker _tickCounter, Player _player, ItemStack _stack, int seed) {
		int itemIndex = seed - 1; // could also be something like (x - context.guiWidth() / 2 + 90 - 2) / 20
		return getHotbarItemTopLeftY(context, itemIndex);
	}

	@ModifyArgs(method = "extractItemHotbar",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiHeight()I", ordinal = 4)
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0)
	)
	private void modifyOffhandSlotLeft(Args args){
		if (this.isBottomMiddle()) {
			args.set(2, (int) args.get(2) + (EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() * 2));
			this.offhandSlotHeight(args, 3);
		}
	}

	@ModifyArgs(method = "extractItemHotbar",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiHeight()I", ordinal = 4)
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1)
	)
	private void modifyOffhandSlotRight(Args args){
		if (this.isBottomMiddle()) {
			args.set(2, (int) args.get(2) - (EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() * 2));
			this.offhandSlotHeight(args, 3);
		}
	}


	@ModifyArgs(method = "extractItemHotbar",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;attackIndicator()Lnet/minecraft/client/OptionInstance;")
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 1)
	)
	private void modifyOffhandItemLeft(Args args){
		if (this.isBottomMiddle()) {
			args.set(1, (int) args.get(1) + (EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() * 2));
			this.offhandSlotHeight(args, 2);
		}
	}
	@ModifyArgs(method = "extractItemHotbar",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;attackIndicator()Lnet/minecraft/client/OptionInstance;")
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 2)
	)
	private void modifyOffhandItemRight(Args args){
		if (this.isBottomMiddle()) {
			args.set(1, (int) args.get(1) - (EnumPixelMagicNumbers.UI_HOTBAR_OFFSET_ON_BUTTOM_MIDDLE_POSITION.getOffset() * 2));
			this.offhandSlotHeight(args, 2);
		}
	}

	// ------------------------------- All the NON HOT BAR RELATED STUFF.

	@ModifyVariable(method = "extractHearts", at = @At("HEAD"), argsOnly = true, name = "yLineBase")
	private int modifyHealthBarY(int yLineBase){
		if (this.moveUIDown()) {
			return yLineBase + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return yLineBase;
	}
	@ModifyVariable(method = "extractHearts", at = @At("HEAD"), argsOnly = true, name = "xLeft")
	private int modifyHealthBarX(int xLeft){
		if (this.isCompactOn()) {
			return xLeft - EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return xLeft;
	}

	@ModifyVariable(method = "extractFood", at = @At("HEAD"), argsOnly = true, name = "yLineBase")
	private int modifyFoodBarY(int yLineBase){
		if (this.moveUIDown()) {
			return yLineBase + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return yLineBase;
	}
	@ModifyVariable(method = "extractFood", at = @At("HEAD"), argsOnly = true, name = "xRight")
	private int modifyFoodBarX(int xRight){
		if (this.isCompactOn()) {
			return xRight + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return xRight;
	}

	@ModifyVariable(method = "extractArmor", at = @At("HEAD"), argsOnly = true, name = "yLineBase")
	private static int modifyArmourY(int yLineBase){
		if (HudMixin.staticHotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) return yLineBase;

		if (HudMixin.staticHotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BOTTOM_MIDDLE || HudMixin.staticHotbarConfig.moveUIDDown || HudMixin.staticHotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) {
			return yLineBase + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return yLineBase;
	}

	@ModifyVariable(method = "extractArmor", at = @At("STORE"), name = "xo")
	private static int modifyArmourX(int xo){
		if (HudMixin.staticHotbarConfig.hotbarMode == Hotbar3x3Config.HotbarMode.VANILLA) return xo;

		if (HudMixin.staticHotbarConfig.hotbarPosition == Hotbar3x3Config.HotbarPosition.BUTTOM_MIDDLE_COMPACT) {
			return xo - EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return xo;
	}

	@ModifyVariable(method = "extractAirBubbles", at = @At("HEAD"), argsOnly = true, name = "yLineAir")
	private int modifyAirBubblesY(int yLineAir){
		if (this.moveUIDown()) {
			return yLineAir + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return yLineAir;
	}
	@ModifyVariable(method = "extractAirBubbles", at = @At("HEAD"), argsOnly = true, name = "xRight")
	private int modifyAirBubblesX(int xRight){
		if (this.isCompactOn()) {
			return xRight + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return xRight;
	}

	@ModifyVariable(method = "extractVehicleHealth", at = @At("STORE"), name = "yLine1")
	private int modifyMountHealthY(int yLine1){
		if (this.moveUIDown()) {
			return yLine1 + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
		}
		return yLine1;
	}
	@ModifyVariable(method = "extractVehicleHealth", at = @At("STORE"), name = "xRight")
	private int modifyMountHealthX(int xRight){
		if (this.isCompactOn()) {
			return xRight + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_SIDE_ON_COMPACT.getOffset();
		}
		return xRight;
	}

	@ModifyVariable(method = "extractSelectedItemName", at = @At("STORE"), name = "y")
	private int modifySelectedItemNameTextY(int y){
		if (this.moveUIDown()) {
			if(this.isCompactOn()){
				return y - EnumPixelMagicNumbers.UI_ITEM_TOOLTIP_OFFSET_ON_COMPACT_POSITION.getOffset() - 14;
			}
			else {
				return y - EnumPixelMagicNumbers.UI_ITEM_TOOLTIP_OFFSET_ON_BOTTOM_MIDDLE_POSITION.getOffset() -14;
			}
		}
		return y;
	}

	@Redirect(method = "extractSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z", ordinal = 0))
	private boolean modifySelectedItemNameContextCheck(MultiPlayerGameMode interactionManager){
		if(this.moveUIDown()){
			return false;
		}
		return interactionManager.canHurtPlayer();
	}

	@ModifyArg(method = "extractOverlayMessage", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", ordinal = 0), index = 1)
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