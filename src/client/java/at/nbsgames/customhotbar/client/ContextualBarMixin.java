package at.nbsgames.customhotbar.client;

import at.nbsgames.customhotbar.EnumPixelMagicNumbers;
import at.nbsgames.customhotbar.Hotbar3x3Client;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContextualBar.class)
public interface ContextualBarMixin {

  @ModifyVariable(method = "extractExperienceLevel", at = @At(value = "STORE"), name = "y")
  private static int adjustExpLevelDown(int initialValue){
    if(Hotbar3x3Client.moveUIDown()){
      return initialValue + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset();
    }
    return initialValue;
  }
  @Inject(method = "top", at = @At(value = "RETURN"), cancellable = true)
  private static void adjustBarDown(CallbackInfoReturnable<Integer> cir){
    if(Hotbar3x3Client.moveUIDown()){
      cir.setReturnValue(cir.getReturnValue() + EnumPixelMagicNumbers.UI_ELEMENTS_MOVE_DOWN_ON_BOTTOM_MIDDLE_POSITION.getOffset());
    }
  }

}
