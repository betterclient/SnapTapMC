package io.github.betterclient.snaptap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SnapTap implements ModInitializer {
    public static long LEFT_STRAFE_LAST_PRESS_TIME = 0;
    public static long RIGHT_STRAFE_LAST_PRESS_TIME = 0;

    public static long FORWARD_STRAFE_LAST_PRESS_TIME = 0;
    public static long BACKWARD_STRAFE_LAST_PRESS_TIME = 0;

    public static KeyBinding TOGGLE_BIND;
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

        TOGGLE_BIND = createToggleBind(b1);
    }

    private int getOrCreateToggle() throws IOException {
        if (!toggleFile.exists()) {
            toggleFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(toggleFile);
            fos.write((InputUtil.GLFW_KEY_F8 + "").getBytes());
            fos.close();

            return InputUtil.GLFW_KEY_F8;
        }
        FileInputStream fis = new FileInputStream(toggleFile);
        byte[] bites = new byte[fis.available()];

        while (fis.available() > 0) fis.read(bites);

        fis.close();

        return Integer.parseInt(new String(bites));
    }

    //reflection stuff
    private static KeyBinding createToggleBind(int keyCode) {
        try {
            Class<KeyBinding> kbClass = KeyBinding.class;

            Class<?> categoryClass = null;
            for (Constructor<?> ctor : kbClass.getConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 3 && params[0] == String.class && params[1] == int.class) {
                    Class<?> thirdParam = params[2];
                    if (thirdParam == String.class) {
                        //1.16-1.21.8
                        return (KeyBinding) ctor.newInstance("text.snaptap.toggle", keyCode, "key.categories.misc");
                    } else {
                        categoryClass = thirdParam;
                        break;
                    }
                }
            }
            //1.21.9+
            if (categoryClass == null) throw new NoSuchMethodException();
            Object miscCategory = findMiscCategory(categoryClass);

            Constructor<KeyBinding> constructor = KeyBinding.class.getConstructor(String.class, int.class, categoryClass);
            return constructor.newInstance("text.snaptap.toggle", keyCode, miscCategory);
        } catch (Exception e1) {
            throw new RuntimeException("SnapTap: Failed to create KeyBinding for either version!", e1);
        }
    }

    private static Object findMiscCategory(Class<?> categoryClass) {
        try {
            for (Field field : categoryClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == categoryClass) {
                    Object va = field.get(null);
                    if (va.toString().contains("minecraft:misc")) {
                        return va;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}