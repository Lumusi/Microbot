package net.runelite.client.plugins.microbot.herbfarmrun;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InventorySetupsDropDown {

    private final ConfigManager configManager;
    private final Gson gson;

    @Inject
    public InventorySetupsDropDown(ConfigManager configManager, Gson gson) {
        this.configManager = configManager;
        this.gson = gson;
    }

    public List<String> getInventorySetups() {
        String json = configManager.getConfiguration("inventorysetups", "setupsV2");
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<InventorySetup>>() {}.getType();
        List<InventorySetup> allSetups = gson.fromJson(json, type);

        if (allSetups == null) {
            return new ArrayList<>();
        }

        return allSetups.stream()
                .map(InventorySetup::getName)
                .collect(Collectors.toList());
    }
}