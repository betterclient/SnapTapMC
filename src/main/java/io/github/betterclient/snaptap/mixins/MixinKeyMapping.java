package io.github.betterclient.snaptap.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.betterclient.snaptap.SnapTap;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class MixinKeyMapping {
    @Shadow private boolean isDown;

    @Shadow @Final private InputConstants.Key defaultKey;

    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    public void onGetDown(CallbackInfoReturnable<Boolean> cir) {
        if (!SnapTap.TOGGLED) return;

        if (this.defaultKey.getValue() == InputConstants.KEY_A) {
            //Left
            if (this.isDown) {
                if (SnapTap.RIGHT_STRAFE_LAST_PRESS_TIME == 0) {
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }

                cir.setReturnValue(SnapTap.RIGHT_STRAFE_LAST_PRESS_TIME <= SnapTap.LEFT_STRAFE_LAST_PRESS_TIME);
                cir.cancel();
            }
        } else if (this.defaultKey.getValue() == InputConstants.KEY_D) {
            //Right
            if (this.isDown) {
                if (SnapTap.LEFT_STRAFE_LAST_PRESS_TIME == 0) {
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }

                cir.setReturnValue(SnapTap.LEFT_STRAFE_LAST_PRESS_TIME <= SnapTap.RIGHT_STRAFE_LAST_PRESS_TIME);
                cir.cancel();
            }
        } else if (this.defaultKey.getValue() == InputConstants.KEY_W) {
            //Forward
            if (this.isDown) {
                if (SnapTap.BACKWARD_STRAFE_LAST_PRESS_TIME == 0) {
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }

                cir.setReturnValue(SnapTap.BACKWARD_STRAFE_LAST_PRESS_TIME <= SnapTap.FORWARD_STRAFE_LAST_PRESS_TIME);
                cir.cancel();
            }
        } else if (this.defaultKey.getValue() == InputConstants.KEY_S) {
            //Backward
            if (this.isDown) {
                if (SnapTap.FORWARD_STRAFE_LAST_PRESS_TIME == 0) {
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }

                cir.setReturnValue(SnapTap.FORWARD_STRAFE_LAST_PRESS_TIME <= SnapTap.BACKWARD_STRAFE_LAST_PRESS_TIME);
                cir.cancel();
            }
        }
    }

    @Inject(method = "setDown", at = @At("HEAD"))
    public void setDown(boolean down, CallbackInfo ci) {
        if (!SnapTap.TOGGLED) return;

        if (this.defaultKey.getValue() == InputConstants.KEY_A) {
            //Left
            if (down) {
                SnapTap.LEFT_STRAFE_LAST_PRESS_TIME = System.currentTimeMillis();
            } else {
                SnapTap.LEFT_STRAFE_LAST_PRESS_TIME = 0;
            }
        } else if (this.defaultKey.getValue() == InputConstants.KEY_D) {
            //Right
            if (down) {
                SnapTap.RIGHT_STRAFE_LAST_PRESS_TIME = System.currentTimeMillis();
            } else {
                SnapTap.RIGHT_STRAFE_LAST_PRESS_TIME = 0;
            }
        } else if (this.defaultKey.getValue() == InputConstants.KEY_W) {
            //Forward
            if (down) {
                SnapTap.FORWARD_STRAFE_LAST_PRESS_TIME = System.currentTimeMillis();
            } else {
                SnapTap.FORWARD_STRAFE_LAST_PRESS_TIME = 0;
            }
        } else if (this.defaultKey.getValue() == InputConstants.KEY_S) {
            //Backward
            if (down) {
                SnapTap.BACKWARD_STRAFE_LAST_PRESS_TIME = System.currentTimeMillis();
            } else {
                SnapTap.BACKWARD_STRAFE_LAST_PRESS_TIME = 0;
            }
        }
    }
}
