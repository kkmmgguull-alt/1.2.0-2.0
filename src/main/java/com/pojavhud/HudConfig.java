package com.pojavhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("pojavhud.json").toFile();

    public boolean visible = true;
    public List<IconConfig> slots = new ArrayList<>();

    public static class IconConfig {
        public int x;
        public int y;
        public float scale = 2.0f;
        public float opacity = 1.0f;

        public IconConfig() {}

        public IconConfig(int x, int y, float scale, float opacity) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.opacity = opacity;
        }
    }

    public static HudConfig load(int screenWidth, int screenHeight) {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                HudConfig config = GSON.fromJson(reader, HudConfig.class);
                if (config != null && config.slots != null && config.slots.size() == 9) {
                    return config;
                }
            } catch (Exception ignored) {}
        }
        HudConfig def = createDefault(screenWidth, screenHeight);
        def.save();
        return def;
    }

    public static HudConfig createDefault(int screenWidth, int screenHeight) {
        HudConfig config = new HudConfig();
        config.visible = true;
        config.slots = new ArrayList<>();

        int iconSize = 32;
        int spacing = 8;
        int totalWidth = (9 * iconSize) + (8 * spacing);
        int startX = Math.max(10, (screenWidth - totalWidth) / 2);
        int startY = Math.max(10, (screenHeight - iconSize) / 2);

        for (int i = 0; i < 9; i++) {
            config.slots.add(new IconConfig(startX + i * (iconSize + spacing), startY, 2.0f, 1.0f));
        }
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (Exception ignored) {}
    }
}
