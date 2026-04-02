package io.github.betterclient.snaptap;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SnapTap implements ModInitializer {
    public static long LEFT_STRAFE_LAST_PRESS_TIME = 0;
    public static long RIGHT_STRAFE_LAST_PRESS_TIME = 0;

    public static long FORWARD_STRAFE_LAST_PRESS_TIME = 0;
    public static long BACKWARD_STRAFE_LAST_PRESS_TIME = 0;

    public static KeyMapping TOGGLE_BIND;
    public static boolean TOGGLED = true;

    public static File toggleFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "snaptap_toggle.txt");

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

        TOGGLE_BIND = new KeyMapping("text.snaptap.toggle", b1, KeyMapping.Category.MISC);
    }

    private int getOrCreateToggle() throws IOException {
        if (!toggleFile.exists()) {
            toggleFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(toggleFile);
            fos.write((InputConstants.KEY_F8 + "").getBytes());
            fos.close();

            return InputConstants.KEY_F8;
        }
        FileInputStream fis = new FileInputStream(toggleFile);
        byte[] bites = new byte[fis.available()];

        while (fis.available() > 0) fis.read(bites);

        fis.close();

        return Integer.parseInt(new String(bites));
    }
}