package net.runelite.client.plugins.microbot.superglassmake;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.concurrent.TimeUnit;

public class SuperglassMakeOverlay extends OverlayPanel {

    @Inject
    SuperglassMakeOverlay(SuperglassMakePlugin plugin) {
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

            // Title with version
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Microbot Superglass Make v" + SuperglassMakeScript.version)
                    .color(new Color(255, 215, 0)) // Gold color
                    .build());

            // Separator
            panelComponent.getChildren().add(LineComponent.builder().build());

            // Status
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(Microbot.status)
                    .leftColor(Color.CYAN)
                    .rightColor(Color.WHITE)
                    .build());

            // Runtime
            long runtime = System.currentTimeMillis() - SuperglassMakeScript.startTime;
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Runtime:")
                    .right(formatTime(runtime))
                    .leftColor(Color.CYAN)
                    .rightColor(Color.WHITE)
                    .build());

            // Casts Made
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Casts Made:")
                    .right(String.valueOf(SuperglassMakeScript.castsMade))
                    .leftColor(Color.CYAN)
                    .rightColor(Color.WHITE)
                    .build());

            // Casts per Hour
            if (runtime > 0 && SuperglassMakeScript.castsMade > 0) {
                long castsPerHour = (long) (SuperglassMakeScript.castsMade * 3600000.0 / runtime);
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Casts/Hr:")
                        .right(String.valueOf(castsPerHour))
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