package io.github.betterclient.snaptap.mixins;

import io.github.betterclient.snaptap.SnapTap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public class MixinOptions {
    @Mutable @Shadow @Final public KeyMapping[] keyMappings;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(Minecraft client, File optionsFile, CallbackInfo ci) {
        this.keyMappings = ArrayUtils.addAll(this.keyMappings, SnapTap.TOGGLE_BIND);
    }
}
