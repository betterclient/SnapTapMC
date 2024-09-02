package io.github.betterclient.snaptap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapTap implements ModInitializer {
    public static long LEFT_STRAFE_LAST_PRESS_TIME = 0;
    public static long RIGHT_STRAFE_LAST_PRESS_TIME = 0;

    public static long FORWARD_STRAFE_LAST_PRESS_TIME = 0;
    public static long BACKWARD_STRAFE_LAST_PRESS_TIME = 0;

    public static KeyBinding TOGGLE_BIND;
    public static boolean TOGGLED = true;

    public static KeyBinding KEYSTROKES_TOGGLE_BIND;
    public static boolean KEYSTROKES_TOGGLED = true;

    private static boolean SERVER_ALLOWS = true;
    private static boolean PRE_SERVER_ALLOWS = true;

    public static Logger LOGGER = LoggerFactory.getLogger("SnapTap");

    @Override
    public void onInitialize() {
        LEFT_STRAFE_LAST_PRESS_TIME = 0;
        RIGHT_STRAFE_LAST_PRESS_TIME = 0;
        FORWARD_STRAFE_LAST_PRESS_TIME = 0;
        BACKWARD_STRAFE_LAST_PRESS_TIME = 0;

        TOGGLE_BIND = new KeyBinding("text.snaptap.toggle", InputUtil.GLFW_KEY_F8, "key.categories.misc") {
            @Override
            public void setPressed(boolean pressed) {
                if (!SERVER_ALLOWS) {
                    TOGGLED = false;
                    super.setPressed(pressed);
                    return;
                }

                if(pressed) {
                    TOGGLED = !TOGGLED;
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                            Text.translatable("text.snaptap.toggled",
                                    Text.translatable(TOGGLED ? "text.snaptap.enabled" : "options.ao.off")
                                            .fillStyle(Style.EMPTY
                                                    .withColor(TOGGLED ? Formatting.GREEN : Formatting.RED))));
                }

                super.setPressed(pressed);
            }
        };

        KEYSTROKES_TOGGLE_BIND = new KeyBinding("text.snaptap.keystrokestoggle", InputUtil.GLFW_KEY_F7, "key.categories.misc") {
            @Override
            public void setPressed(boolean pressed) {
                if (!SERVER_ALLOWS) {
                    TOGGLED = false;
                }

                if(pressed) {
                    KEYSTROKES_TOGGLED = !KEYSTROKES_TOGGLED;
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                            Text.translatable("text.snaptap.toggledkeystokes",
                                    Text.translatable(KEYSTROKES_TOGGLED ? "text.snaptap.enabled" : "options.ao.off")
                                            .fillStyle(Style.EMPTY
                                                    .withColor(KEYSTROKES_TOGGLED ? Formatting.GREEN : Formatting.RED))));
                }

                super.setPressed(pressed);
            }
        };

        PayloadTypeRegistry.playS2C().register(SnapTapPayload.PAYLOAD_ID, new SnapTapPayload.Codec());

        ClientPlayNetworking.registerGlobalReceiver(SnapTapPayload.PAYLOAD_ID, (payload, context) -> {
            PRE_SERVER_ALLOWS = TOGGLED;
            TOGGLED = false;
            SERVER_ALLOWS = payload.allowed;
            if (!SERVER_ALLOWS) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("text.snaptap.serverdisallow"));
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            TOGGLED = PRE_SERVER_ALLOWS;
            SERVER_ALLOWS = true;
        });
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        KeyBinding leftKey = client.options.leftKey;
        KeyBinding rightKey = client.options.rightKey;
        KeyBinding forwardKey = client.options.forwardKey;
        KeyBinding backwardKey = client.options.backKey;

        KeybindingAccess left = (KeybindingAccess) leftKey;
        KeybindingAccess right = (KeybindingAccess) rightKey;
        KeybindingAccess forward = (KeybindingAccess) forwardKey;
        KeybindingAccess backward = (KeybindingAccess) backwardKey;

        if (left.snapTap$isPressedReal()) {
            context.fill(5, 30, 25, 50, 0xFF444444);
        } else {
            context.fill(5, 30, 25, 50, 0xFF000000);
        }

        if (backward.snapTap$isPressedReal()) {
            context.fill(30, 30, 50, 50, 0xFF444444);
        } else {
            context.fill(30, 30, 50, 50, 0xFF000000);
        }

        if (right.snapTap$isPressedReal()) {
            context.fill(55, 30, 75, 50, 0xFF444444);
        } else {
            context.fill(55, 30, 75, 50, 0xFF000000);
        }

        if (forward.snapTap$isPressedReal()) {
            context.fill(30, 5, 50, 25, 0xFF444444);
        } else {
            context.fill(30, 5, 50, 25, 0xFF000000);
        }

        context.drawCenteredTextWithShadow(client.textRenderer, leftKey.getBoundKeyLocalizedText(), 15, 36, -1);
        context.drawCenteredTextWithShadow(client.textRenderer, rightKey.getBoundKeyLocalizedText(), 65, 36, -1);

        context.drawCenteredTextWithShadow(client.textRenderer, backwardKey.getBoundKeyLocalizedText(), 40, 36, -1);
        context.drawCenteredTextWithShadow(client.textRenderer, forwardKey.getBoundKeyLocalizedText(), 40, 11, -1);
    }
}
