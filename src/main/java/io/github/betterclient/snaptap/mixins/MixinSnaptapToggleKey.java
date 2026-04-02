package io.github.betterclient.snaptap.mixins;

import io.github.betterclient.snaptap.SnapTap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileOutputStream;

@Mixin(KeyBinding.class)
public class MixinSnaptapToggleKey {
    @Inject(method = "setPressed", at = @At("HEAD"))
    public void setPressed(boolean pressed, CallbackInfo ci) {
        if (pressed && ((Object) this) == SnapTap.TOGGLE_BIND) {
            SnapTap.TOGGLED = !SnapTap.TOGGLED;
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                    Text.translatable("text.snaptap.toggled",
                            Text.translatable(SnapTap.TOGGLED ? "text.snaptap.enabled" : "options.ao.off")
                                    .fillStyle(Style.EMPTY
                                            .withColor(SnapTap.TOGGLED ? Formatting.GREEN : Formatting.RED))));
        }
    }

    @Inject(method = "setBoundKey", at = @At("RETURN"))
    public void onSetBoundKey(InputUtil.Key boundKey, CallbackInfo ci) {
        if (((Object) this) == SnapTap.TOGGLE_BIND) {
            try {
                SnapTap.toggleFile.delete();
                SnapTap.toggleFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(SnapTap.toggleFile);
                fos.write(("" + boundKey.getCode()).getBytes());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
