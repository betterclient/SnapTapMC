package io.github.betterclient.snaptap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    public File toggleFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "snaptap_toggle.txt");

    @Override
    public void onInitialize() {
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
                    e.printStackTrace();
                }
            }
        };
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
        byte[] bites = new byte[fis.available()];

        while (fis.available() > 0) fis.read(bites);

        fis.close();

        return Integer.parseInt(new String(bites));
    }
}