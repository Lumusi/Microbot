package net.runelite.client.plugins.microbot.aioagility;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.aioagility.courses.AgilityCourseHandler;
import net.runelite.client.plugins.microbot.aioagility.enums.AgilityCourse;
import net.runelite.client.plugins.microbot.pluginscheduler.api.SchedulablePlugin;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.AndCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.LogicalCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.PredicateCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.event.PluginScheduleEntrySoftStopEvent;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "<html>[&#129302;] <font color='#FFD700'>AIO Agility</font>",
        description = "Microbot AIO agility plugin",
        tags = {"agility", "microbot", "aio"},
        enabledByDefault = false
)
@Slf4j
public class AioAgilityPlugin extends Plugin implements SchedulablePlugin {
    @Inject
    private AioAgilityConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AioAgilityOverlay agilityOverlay;
    @Inject
    private AioAgilityScript agilityScript;

    private LogicalCondition stopCondition;

    @Getter
    @Setter
    private AgilityCourseHandler courseHandler;

    // --- State Management ---
    @Getter
    private Instant startTime;
    @Getter
    private int initialXp;

    @Provides
    AioAgilityConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AioAgilityConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        // Initialize state here, once.
        startTime = Instant.now();
        initialXp = Microbot.getClient().getSkillExperience(Skill.AGILITY);

        if (overlayManager != null) {
            overlayManager.add(agilityOverlay);
        }
        agilityScript.reset();
        agilityScript.run();
    }

    protected void shutDown() {
        overlayManager.remove(agilityOverlay);
        agilityScript.shutdown();
        startTime = null;
        initialXp = 0;
    }

    // --- HELPER METHODS FOR OVERLAY ---
    public Duration getSessionDuration() {
        return startTime == null ? Duration.ZERO : Duration.between(startTime, Instant.now());
    }

    public int getXpGained() {
        int currentXp = Microbot.getClient().getSkillExperience(Skill.AGILITY);
        return initialXp > 0 ? currentXp - initialXp : 0;
    }

    // This method now correctly accepts an AgilityCourse enum
    public void setCourseHandler(AgilityCourse course) {
        this.courseHandler = course.getHandler();
    }

    @Subscribe
    public void onPluginScheduleEntrySoftStopEvent(PluginScheduleEntrySoftStopEvent event) {
        try {
            if (event.getPlugin() == this) {
                Microbot.getClientThread().runOnSeperateThread(() -> {
                    if (getCourseHandler() != null && getCourseHandler().getCurrentObstacleIndex() > 0) {
                        Global.sleepUntil(() -> getCourseHandler().getCurrentObstacleIndex() == 0, 10_000);
                    }
                    Microbot.stopPlugin(this);
                    return null;
                });
            }
        } catch (Exception e) {
            log.error("Error stopping plugin: ", e);
        }
    }

    @Override
    public LogicalCondition getStopCondition() {
        if (stopCondition == null) {
            LogicalCondition _stopCondition = new AndCondition();
            Supplier<Integer> currentIndexSupplier = () -> {
                if (getCourseHandler() == null) return 0;
                return getCourseHandler().getCurrentObstacleIndex();
            };
            Predicate<Integer> isAtStartPredicate = index -> index == 0;
            PredicateCondition<Integer> atStartCondition = new PredicateCondition<>(
                    isAtStartPredicate,
                    currentIndexSupplier,
                    "Player is at the start of the agility course (index 0)"
            );
            _stopCondition.addCondition(atStartCondition);
            stopCondition = _stopCondition;
        }
        return stopCondition;
    }

    public List<Rs2ItemModel> getInventoryFood() {
        return Rs2Inventory.getInventoryFood().stream().filter(i -> !(i.getName().toLowerCase().contains("summer pie"))).collect(Collectors.toList());
    }

    public List<Rs2ItemModel> getSummerPies() {
        return Rs2Inventory.getInventoryFood().stream().filter(i -> i.getName().toLowerCase().contains("summer pie")).collect(Collectors.toList());
    }

    public boolean hasRequiredLevel() {
        if (getCourseHandler() == null) return false;
        if (getSummerPies().isEmpty() || !getCourseHandler().canBeBoosted()) {
            return Rs2Player.getRealSkillLevel(Skill.AGILITY) >= getCourseHandler().getRequiredLevel();
        }
        return Rs2Player.getBoostedSkillLevel(Skill.AGILITY) >= getCourseHandler().getRequiredLevel();
    }
}