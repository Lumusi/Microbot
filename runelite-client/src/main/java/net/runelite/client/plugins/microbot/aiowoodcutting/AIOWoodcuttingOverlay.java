package net.runelite.client.plugins.microbot.aiowoodcutting;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Skill;

public class AIOWoodcuttingOverlay extends OverlayPanel {

    private final AIOWoodcuttingScript script;

    @Inject
    AIOWoodcuttingOverlay(AIOWoodcuttingPlugin plugin, AIOWoodcuttingScript script) {
        super(plugin);
        this.script = script;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!script.isRunning() || script.startTime == 0) {
            return null;
        }

        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            // UPDATED: Now displays the script version in the title
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text(String.format("AIO Woodcutter v%s", AIOWoodcuttingScript.VERSION))
                    .build());

            long elapsedTime = System.currentTimeMillis() - script.startTime;
            int xpGained = Microbot.getClient().getSkillExperience(Skill.WOODCUTTING) - script.initialXp;
            int xpPerHour = (int)(xpGained / ((double)elapsedTime / 3600000.0));

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(script.status)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time Ran:")
                    .right(formatTime(elapsedTime))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("XP Gained:")
                    .right(String.format("%,d (%,d/hr)", xpGained, xpPerHour))
                    .build());

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }

    private String formatTime(long duration) {
        long hours = duration / 3600000;
        long minutes = (duration % 3600000) / 60000;
        long seconds = ((duration % 3600000) % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}