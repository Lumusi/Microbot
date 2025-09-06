package net.runelite.client.plugins.microbot.bankskiller;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bankskiller.modes.HerbCleaner;
import net.runelite.client.plugins.microbot.bankskiller.modes.ItemCombiner;
import net.runelite.client.plugins.microbot.bankskiller.modes.StaminaPotionMaker;

import java.util.concurrent.TimeUnit;

public class BankskillerScript extends Script {

    private BankskillerConfig config;
    private HerbCleaner herbCleaner;
    private ItemCombiner itemCombiner;
    private StaminaPotionMaker staminaPotionMaker;
    private Session session;
    private boolean isRunning = false;

    public Session getSession() { return session; }

    public String getStatus() {
        if (!isRunning) return "STOPPED";
        if (config == null) return "CONFIGURING";

        switch (config.operatingMode()) {
            case HERB_CLEANING:
                return (herbCleaner != null) ? herbCleaner.getStatus() : "INITIALIZING";
            case STAMINA_POTIONS:
                return (staminaPotionMaker != null) ? staminaPotionMaker.getStatus() : "INITIALIZING";
            case ITEM_COMBINATION:
            default:
                return (itemCombiner != null) ? itemCombiner.getStatus() : "INITIALIZING";
        }
    }

    public void start(BankskillerConfig config) {
        if (isRunning) {
            Microbot.log("Script is already running.");
            return;
        }

        this.config = config;
        this.session = new Session(Microbot.getClient(), config.skillToTrack());
        this.isRunning = true;

        this.herbCleaner = new HerbCleaner(this, config, session);
        this.itemCombiner = new ItemCombiner(this, config, session);
        this.staminaPotionMaker = new StaminaPotionMaker(this, config, session);

        // A low delay allows the tick-based logic in StaminaPotionMaker to be very responsive
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(this::loop, 0, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        isRunning = false;
        herbCleaner = null;
        itemCombiner = null;
        staminaPotionMaker = null;
        session = null;
    }

    private void loop() {
        if (!Microbot.isLoggedIn() || !super.run() || !isRunning) return;

        try {
            switch (config.operatingMode()) {
                case HERB_CLEANING:
                    herbCleaner.run();
                    break;
                case STAMINA_POTIONS:
                    staminaPotionMaker.run();
                    break;
                case ITEM_COMBINATION:
                default:
                    itemCombiner.run();
                    break;
            }
        } catch (Exception ex) {
            System.err.println("Exception in Bankskiller script: " + ex.getMessage());
            ex.printStackTrace();
            shutdown();
        }
    }
}