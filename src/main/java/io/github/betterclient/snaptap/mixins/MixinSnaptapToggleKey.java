package io.github.betterclient.snaptap.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.betterclient.snaptap.SnapTap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileOutputStream;

@Mixin(KeyMapping.class)
public class MixinSnaptapToggleKey {
    @Inject(method = "setDown", at = @At("HEAD"))
    public void setDown(boolean down, CallbackInfo ci) {
        if (down && ((Object) this) == SnapTap.TOGGLE_BIND) {
            SnapTap.TOGGLED = !SnapTap.TOGGLED;
            Minecraft.getInstance().gui.getChat().addClientSystemMessage(
                    Component.translatable("text.snaptap.toggled",
                            Component.translatable(SnapTap.TOGGLED ? "text.snaptap.enabled" : "options.ao.off")
                                    .setStyle(Style.EMPTY
                                            .withColor(SnapTap.TOGGLED ? ChatFormatting.GREEN : ChatFormatting.RED))));
        }
    }

    @Inject(method = "setKey", at = @At("RETURN"))
    public void onSetBoundKey(InputConstants.Key key, CallbackInfo ci) {
        if (((Object) this) == SnapTap.TOGGLE_BIND) {
            try {
                SnapTap.toggleFile.delete();
                SnapTap.toggleFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(SnapTap.toggleFile);
                fos.write(("" + key.getValue()).getBytes());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
