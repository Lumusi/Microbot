package net.runelite.client.plugins.microbot.staminapotions;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class StaminaPotionsOverlay extends OverlayPanel {

    @Inject
    StaminaPotionsOverlay(StaminaPotionsPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    private String formatTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(275, 0));
            panelComponent.getChildren().clear();

            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Microbot Stamina Potions")
                    .color(new Color(255, 215, 0))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(Microbot.status)
                    .leftColor(Color.CYAN)
                    .rightColor(Color.WHITE)
                    .build());

            long runtime = System.currentTimeMillis() - StaminaPotionsScript.startTime;
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Runtime:")
                    .right(formatTime(runtime))
                    .leftColor(Color.CYAN)
                    .rightColor(Color.WHITE)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Potions Made:")
                    .right(String.valueOf(StaminaPotionsScript.potionsMade))
                    .leftColor(Color.CYAN)
                    .rightColor(Color.WHITE)
                    .build());

            if (runtime > 0 && StaminaPotionsScript.potionsMade > 0) {
                long potionsPerHour = (long) (StaminaPotionsScript.potionsMade * 3600000.0 / runtime);
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Potions/Hr:")
                        .right(String.valueOf(potionsPerHour))
                        .leftColor(Color.CYAN)
                        .rightColor(Color.WHITE)
                        .build());
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}