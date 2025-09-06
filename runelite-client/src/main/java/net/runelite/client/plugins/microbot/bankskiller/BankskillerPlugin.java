package net.runelite.client.plugins.microbot.bankskiller;

import com.google.inject.Provides;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.microbot.Microbot;

@PluginDescriptor(
        name = "<html>[&#129302;] <font color='#FFD700'>Bankskiller</font>",
        description = "Microbot bank skilling plugin",
        tags = {"bank", "skilling", "microbot"},
        enabledByDefault = false
)
public class BankskillerPlugin extends Plugin {

    private static final String CONFIG_GROUP = "bankskiller";

    @Inject
    private BankskillerConfig config;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BankskillerOverlay bankskillerOverlay;

    private BankskillerScript bankskillerScript;
    private String selectingItemFor = null;

    @Provides
    BankskillerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BankskillerConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        bankskillerScript = new BankskillerScript();
        overlayManager.add(bankskillerOverlay);
    }

    @Override
    protected void shutDown() throws Exception {
        if (bankskillerScript != null) {
            bankskillerScript.shutdown();
        }
        overlayManager.remove(bankskillerOverlay);
        selectingItemFor = null;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(CONFIG_GROUP) || !Boolean.parseBoolean(event.getNewValue())) return;

        // Reset the button after click
        configManager.setConfiguration(CONFIG_GROUP, event.getKey(), false);

        switch (event.getKey()) {
            case "startScript":
                bankskillerScript.start(config);
                break;
            case "stopScript":
                bankskillerScript.shutdown();
                break;
            case "selectGrimyHerb":
                selectingItemFor = "grimyHerbID";
                Microbot.showMessage("Click a grimy herb in your inventory to select it.");
                break;
            case "selectItem1":
                selectingItemFor = "itemID1";
                Microbot.showMessage("Click an item in your inventory to set Item 1 ID.");
                break;
            case "selectItem2":
                selectingItemFor = "itemID2";
                Microbot.showMessage("Click an item in your inventory to set Item 2 ID.");
                break;
            case "selectItem3":
                selectingItemFor = "itemID3";
                Microbot.showMessage("Click an item in your inventory to set Item 3 ID.");
                break;
            case "selectItem4":
                selectingItemFor = "itemID4";
                Microbot.showMessage("Click an item in your inventory to set Item 4 ID.");
                break;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (selectingItemFor == null) return;
        if (event.getWidget() != null && event.getWidget().getParentId() == 9764864 && event.getItemId() != -1) {
            configManager.setConfiguration(CONFIG_GROUP, selectingItemFor, event.getItemId());
            Microbot.showMessage("Item ID set to: " + event.getItemId());
            selectingItemFor = null;
            event.consume();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        if (bankskillerScript != null && bankskillerScript.getSession() != null &&
                statChanged.getSkill() == config.skillToTrack()) {
            bankskillerScript.getSession().updateXp(statChanged.getXp());
        }
    }

    public String getStatus() { return (bankskillerScript != null) ? bankskillerScript.getStatus() : "DISABLED"; }
    public Session getSession() { return (bankskillerScript != null) ? bankskillerScript.getSession() : null; }
}