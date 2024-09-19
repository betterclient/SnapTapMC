package io.github.betterclient.snaptap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "snaptap_hud.txt");
    public File toggleFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "snaptap_toggle.txt");
    public File toggleKeystrokesFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "snaptap_toggle_keystrokes.txt");

    @Override
    public void onInitialize() {
        try {
            KEYSTROKES_TOGGLED = getOrCreateHud();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LEFT_STRAFE_LAST_PRESS_TIME = 0;
        RIGHT_STRAFE_LAST_PRESS_TIME = 0;
        FORWARD_STRAFE_LAST_PRESS_TIME = 0;
        BACKWARD_STRAFE_LAST_PRESS_TIME = 0;

        int b1;
        try {
            b1 = getOrCreateToggle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TOGGLE_BIND = new KeyBinding("text.snaptap.toggle", b1, "key.categories.misc") {
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

            @Override
            public void setBoundKey(InputUtil.Key boundKey) {
                super.setBoundKey(boundKey);

                try {
                    toggleFile.delete();
                    toggleFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(toggleFile);
                    fos.write(("" + boundKey.getCode()).getBytes());
                    fos.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to save snap-tap key", e);
                }
            }
        };

        int b2;
        try {
            b2 = getOrCreateKeystrokesToggle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        KEYSTROKES_TOGGLE_BIND = new KeyBinding("text.snaptap.keystrokestoggle", b2, "key.categories.misc") {
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
                    try {
                        configFile.delete();
                        configFile.createNewFile();
                        FileOutputStream fos = new FileOutputStream(configFile);
                        fos.write(("" + KEYSTROKES_TOGGLED).getBytes());
                        fos.close();
                    } catch (Exception e) {
                        LOGGER.error("Failed to save keystrokes settings", e);
                    }
                }

                super.setPressed(pressed);
            }

            @Override
            public void setBoundKey(InputUtil.Key boundKey) {
                super.setBoundKey(boundKey);

                try {
                    toggleKeystrokesFile.delete();
                    toggleKeystrokesFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(toggleKeystrokesFile);
                    fos.write(("" + boundKey.getCode()).getBytes());
                    fos.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to save keystrokes settings", e);
                }
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

    private int getOrCreateKeystrokesToggle() throws IOException {
        if (!toggleKeystrokesFile.exists()) {
            toggleKeystrokesFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(toggleKeystrokesFile);
            fos.write(InputUtil.GLFW_KEY_F7);
            fos.close();

            return InputUtil.GLFW_KEY_F7;
        }
        FileInputStream fis = new FileInputStream(toggleKeystrokesFile);
        byte[] bites = fis.readAllBytes();
        fis.close();

        return Integer.parseInt(new String(bites));
    }

    private int getOrCreateToggle() throws IOException {
        if (!toggleFile.exists()) {
            toggleFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(toggleFile);
            fos.write(InputUtil.GLFW_KEY_F8);
            fos.close();

            return InputUtil.GLFW_KEY_F8;
        }
        FileInputStream fis = new FileInputStream(toggleFile);
        byte[] bites = fis.readAllBytes();
        fis.close();

        return Integer.parseInt(new String(bites));
    }

    private boolean getOrCreateHud() throws IOException {
        if (!configFile.exists()) {
            configFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write("true".getBytes());
            fos.close();

            return true;
        }
        FileInputStream fis = new FileInputStream(configFile);
        byte[] bites = fis.readAllBytes();
        fis.close();

        return Boolean.parseBoolean(new String(bites));
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