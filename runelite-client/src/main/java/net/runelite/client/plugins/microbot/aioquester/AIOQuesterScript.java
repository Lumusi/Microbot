package net.runelite.client.plugins.microbot.aioquester;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.aioquester.quests.CooksAssistantScript;
import net.runelite.client.plugins.microbot.aioquester.quests.XMarksTheSpotScript;

import java.util.concurrent.TimeUnit;

public class AIOQuesterScript extends Script {

    public static String status = "Starting...";

    private final CooksAssistantScript cooksAssistantScript = new CooksAssistantScript();
    private final XMarksTheSpotScript xMarksTheSpotScript = new XMarksTheSpotScript();

    public boolean run(AIOQuesterConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) {
                    status = "Not logged in.";
                    return;
                }
                switch (config.quest()) {
                    case COOKS_ASSISTANT:
                        cooksAssistantScript.run();
                        break;
                    case X_MARKS_THE_SPOT:
                        xMarksTheSpotScript.run();
                        break;
                }
            } catch (Exception ex) {
                status = "Error: " + ex.getMessage();
                System.out.println("Error in AIO Quester Script: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        status = "Shutting down...";
    }
}