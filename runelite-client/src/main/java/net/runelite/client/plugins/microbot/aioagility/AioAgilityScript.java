package net.runelite.client.plugins.microbot.aioagility;

import lombok.Getter;
import net.runelite.api.NpcID;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.agility.AgilityPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.aioagility.courses.AgilityCourseHandler;
import net.runelite.client.plugins.microbot.aioagility.courses.GnomeStrongholdCourse;
import net.runelite.client.plugins.microbot.aioagility.courses.PrifddinasCourse;
import net.runelite.client.plugins.microbot.aioagility.courses.WerewolfCourse;
import net.runelite.client.plugins.microbot.aioagility.enums.AgilityCourse;
import net.runelite.client.plugins.microbot.aioagility.models.QuestRequirement;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class AioAgilityScript extends Script {

    public static String version = "2.9.1";
    final AioAgilityPlugin plugin;
    final AioAgilityConfig config;

    public enum State { INITIALIZING, DETERMINING_COURSE, TRAVELLING, RUNNING_COURSE }
    private State currentState = State.INITIALIZING;
    private AgilityCourse targetCourse = null;
    private int lastAgilityLevel = 0;
    private boolean reEvaluationNeeded = false;

    @Getter
    private long startTime = 0;
    @Getter
    private int initialXp = 0;

    private static final int SHANTAY_NPC_ID = NpcID.SHANTAY;
    private static final int SHANTAY_PASS_ID = ItemID.SHANTAY_PASS;
    private static final WorldPoint SHANTAY_HUT_LOCATION = new WorldPoint(3304, 3122, 0);
    private static final WorldPoint CARPET_MERCHANT_LOCATION = new WorldPoint(3311, 3108, 0);

    @Inject
    public AioAgilityScript(AioAgilityPlugin plugin, AioAgilityConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void reset() {
        currentState = State.INITIALIZING;
        lastAgilityLevel = 0;
        targetCourse = null;
        reEvaluationNeeded = false;
        startTime = System.currentTimeMillis();
        initialXp = Microbot.getClient().getSkillExperience(Skill.AGILITY);
    }

    public boolean run() {
        Microbot.enableAutoRunOn = true;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyAgilitySetup();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;
                if (Rs2AntibanSettings.actionCooldownActive) return;

                if (config.aioMode()) {
                    aioStateMachine();
                } else {
                    if (plugin.getCourseHandler() == null) {
                        plugin.setCourseHandler(config.agilityCourse());
                    }
                    executeCourseLogic(config.agilityCourse().getHandler());
                }
            } catch (Exception ex) {
                Microbot.log("An error occurred: " + ex.getMessage(), ex);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    private void aioStateMachine() {
        int currentAgilityLevel = Rs2Player.getRealSkillLevel(Skill.AGILITY);
        if (lastAgilityLevel == 0) lastAgilityLevel = currentAgilityLevel;
        if (currentAgilityLevel > lastAgilityLevel) {
            lastAgilityLevel = currentAgilityLevel;
            reEvaluationNeeded = true;
            Microbot.status = "Level up! Finishing lap...";
        }

        switch (currentState) {
            case INITIALIZING:
                Microbot.status = "Initializing AIO Agility...";
                currentState = State.DETERMINING_COURSE;
                break;
            case DETERMINING_COURSE:
                Microbot.status = "Determining best course...";
                AgilityCourse bestCourse = getBestAvailableCourse();
                if (bestCourse == null) {
                    Microbot.showMessage("No available agility course found.");
                    shutdown();
                    return;
                }
                targetCourse = bestCourse;
                // The call to the plugin's method is now correct
                plugin.setCourseHandler(targetCourse);
                if (Rs2Player.getWorldLocation().distanceTo(targetCourse.getHandler().getStartPoint()) > 15) {
                    currentState = State.TRAVELLING;
                } else {
                    currentState = State.RUNNING_COURSE;
                }
                break;
            case TRAVELLING:
                if (targetCourse == null) { currentState = State.DETERMINING_COURSE; return; }
                Microbot.status = "Travelling to " + targetCourse.getTooltip();
                if (targetCourse == AgilityCourse.POLLNIVNEACH_ROOFTOP_COURSE) {
                    if (travelToPollnivneach()) currentState = State.RUNNING_COURSE;
                } else {
                    if (Rs2Player.getWorldLocation().distanceTo(targetCourse.getHandler().getStartPoint()) < 15) {
                        currentState = State.RUNNING_COURSE;
                    } else {
                        Rs2Walker.walkTo(targetCourse.getHandler().getStartPoint());
                    }
                }
                break;
            case RUNNING_COURSE:
                AgilityCourseHandler courseHandler = plugin.getCourseHandler();
                if (courseHandler == null) { currentState = State.DETERMINING_COURSE; return; }
                if (reEvaluationNeeded && courseHandler.getCurrentObstacleIndex() == 0) {
                    reEvaluationNeeded = false;
                    currentState = State.DETERMINING_COURSE;
                    return;
                }
                Microbot.status = "Running " + targetCourse.getTooltip();
                executeCourseLogic(courseHandler);
                break;
        }
    }

    private void executeCourseLogic(AgilityCourseHandler courseHandler) {
        if (courseHandler == null) return;
        if (!plugin.hasRequiredLevel()) {
            Microbot.showMessage("You do not have the required level for this course.");
            if (!config.aioMode()) shutdown(); else currentState = State.DETERMINING_COURSE;
            return;
        }
        if (handleFood() || handleSummerPies()) return;
        if (courseHandler.getCurrentObstacleIndex() > 0 && (Rs2Player.isMoving() || Rs2Player.isAnimating())) return;
        if (lootMarksOfGrace()) return;
        if (config.alchemy()) { getAlchItem().ifPresent(item -> Rs2Magic.alch(item, 50, 75)); }

        if (courseHandler instanceof PrifddinasCourse) {
            PrifddinasCourse course = (PrifddinasCourse) courseHandler;
            if (course.handlePortal() || course.handleWalkToStart(Rs2Player.getWorldLocation())) return;
        } else if (courseHandler instanceof WerewolfCourse) {
            WerewolfCourse course = (WerewolfCourse) courseHandler;
            if (course.handleFirstSteppingStone(Rs2Player.getWorldLocation()) || course.handleStickPickup(Rs2Player.getWorldLocation()) || course.handleSlide() || course.handleStickReturn(Rs2Player.getWorldLocation())) return;
        } else if (!(courseHandler instanceof GnomeStrongholdCourse)) {
            if (courseHandler.handleWalkToStart(Rs2Player.getWorldLocation())) return;
        }
        final int agilityExp = Microbot.getClient().getSkillExperience(Skill.AGILITY);
        TileObject gameObject = courseHandler.getCurrentObstacle();
        if (gameObject == null) return;
        if (!Rs2Camera.isTileOnScreen(gameObject)) { Rs2Walker.walkMiniMap(gameObject.getWorldLocation()); }
        if (Rs2GameObject.interact(gameObject)) {
            courseHandler.waitForCompletion(agilityExp, Rs2Player.getWorldLocation().getPlane());
            Rs2Antiban.actionCooldown();
            Rs2Antiban.takeMicroBreakByChance();
        }
    }

    private AgilityCourse getBestAvailableCourse() {
        int agilityLevel = Rs2Player.getRealSkillLevel(Skill.AGILITY);
        AgilityCourse bestCourse = null;
        for (AgilityCourse course : AgilityCourse.values()) {
            if (course.isAioCourse() && agilityLevel >= course.getLevelReq() && checkQuestRequirements(course)) {
                bestCourse = course;
            }
        }
        return bestCourse;
    }

    private boolean checkQuestRequirements(AgilityCourse course) {
        QuestRequirement questReq = course.getQuestRequirement();
        if (questReq != null) {
            return questReq.isVarbit() ?
                    Microbot.getVarbitValue(questReq.getVarpId()) >= questReq.getRequiredState() :
                    Microbot.getClient().getVarpValue(questReq.getVarpId()) >= questReq.getRequiredState();
        }
        return true;
    }

    private boolean travelToPollnivneach() {
        WorldPoint startPoint = AgilityCourse.POLLNIVNEACH_ROOFTOP_COURSE.getHandler().getStartPoint();
        if (Rs2Player.getWorldLocation().distanceTo(startPoint) < 20) return true;
        if (Rs2Player.getWorldLocation().getRegionID() == 13358) {
            Rs2Walker.walkTo(startPoint);
            return Rs2Player.getWorldLocation().distanceTo(startPoint) < 20;
        }
        if (!Rs2Inventory.hasItem(SHANTAY_PASS_ID)) {
            Microbot.status = "Buying a Shantay pass...";
            if (Rs2Player.getWorldLocation().distanceTo(SHANTAY_HUT_LOCATION) > 10) {
                Rs2Walker.walkTo(SHANTAY_HUT_LOCATION);
                return false;
            }
            if (!Rs2Dialogue.isInDialogue()) {
                Rs2Npc.interact(SHANTAY_NPC_ID, "Shantay-pass");
                sleepUntil(Rs2Dialogue::isInDialogue, 5000);
            }
            if (Rs2Dialogue.hasSelectAnOption()) {
                Rs2Dialogue.keyPressForDialogueOption(1);
                sleepUntil(() -> Rs2Inventory.hasItem(SHANTAY_PASS_ID), 5000);
            }
            return false;
        }
        if (Rs2Inventory.hasItem(SHANTAY_PASS_ID)) {
            Microbot.status = "Using magic carpet...";
            if (Rs2Player.getWorldLocation().distanceTo(CARPET_MERCHANT_LOCATION) > 8) {
                Rs2Walker.walkTo(CARPET_MERCHANT_LOCATION);
                return false;
            }
            if (!Rs2Dialogue.isInDialogue()) {
                Rs2GameObject.interact("Magic carpet");
                sleepUntil(Rs2Dialogue::isInDialogue, 5000);
            }
            if (Rs2Dialogue.hasSelectAnOption()) {
                Rs2Dialogue.keyPressForDialogueOption("Pollnivneach");
                sleep(5000, 7000);
            }
            return false;
        }
        return false;
    }

    private Optional<String> getAlchItem() {
        String itemsInput = config.itemsToAlch().trim();
        if (itemsInput.isEmpty()) return Optional.empty();
        List<String> itemsToAlch = Arrays.stream(itemsInput.split(","))
                .map(String::trim).map(s -> s.toLowerCase(Locale.ROOT)).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        if (itemsToAlch.isEmpty()) return Optional.empty();
        for (String itemName : itemsToAlch) {
            if (Rs2Inventory.hasItem(itemName)) return Optional.of(itemName);
        }
        return Optional.empty();
    }

    private boolean lootMarksOfGrace() {
        final List<RS2Item> marksOfGrace = AgilityPlugin.getMarksOfGrace();
        if (plugin.getCourseHandler() == null) return false;
        final int lootDistance = plugin.getCourseHandler().getLootDistance();
        if (!marksOfGrace.isEmpty() && !Rs2Inventory.isFull()) {
            for (RS2Item markOfGraceTile : marksOfGrace) {
                if (Microbot.getClient().getTopLevelWorldView().getPlane() != markOfGraceTile.getTile().getPlane()) continue;
                if (!Rs2GameObject.canReach(markOfGraceTile.getTile().getWorldLocation(), lootDistance, lootDistance, lootDistance, lootDistance)) continue;
                Rs2GroundItem.loot(markOfGraceTile.getItem().getId());
                Rs2Player.waitForWalking();
                return true;
            }
        }
        return false;
    }

    private boolean handleFood() {
        if (config.hitpoints() == 0 || Rs2Player.getHealthPercentage() > config.hitpoints()) {
            return false;
        }
        List<Rs2ItemModel> foodItems = plugin.getInventoryFood();
        if (foodItems.isEmpty()) {
            Microbot.status = "No food found!";
            return false;
        }
        Rs2ItemModel foodToEat = foodItems.get(0);
        Microbot.status = "Eating " + foodToEat.getName();
        String action = foodToEat.getName().toLowerCase(Locale.ROOT).contains("jug of wine") ? "drink" : "eat";
        Rs2Inventory.interact(foodToEat, action);
        sleep(600, 1200);
        // Cleaned up varargs call
        Rs2Inventory.dropAll(229, 1935, 1937, 2313); // VIAL, JUG, EMPTY_JUG, PIE_DISH
        return true;
    }

    private boolean handleSummerPies() {
        if (plugin.getCourseHandler() == null || plugin.getCourseHandler().getCurrentObstacleIndex() > 0) return false;
        if (Rs2Player.getBoostedSkillLevel(Skill.AGILITY) > plugin.getCourseHandler().getRequiredLevel()) return false;
        List<Rs2ItemModel> summerPies = plugin.getSummerPies();
        if (summerPies.isEmpty()) return false;
        Rs2ItemModel summerPie = summerPies.get(0);
        Rs2Inventory.interact(summerPie, "eat");
        Rs2Inventory.waitForInventoryChanges(1800);
        if (Rs2Inventory.hasItem(2313)) { // Pie dish
            Rs2Inventory.dropAll(2313);
        }
        return true;
    }
}