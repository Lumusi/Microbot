package net.runelite.client.plugins.microbot.aioquester;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class AIOQuesterOverlay extends OverlayPanel {

    private final AIOQuesterConfig config;

    @Inject
    AIOQuesterOverlay(AIOQuesterPlugin plugin, AIOQuesterConfig config) {
        super(plugin);
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Microbot AIO Quester")
                    .color(java.awt.Color.decode("#FFD700"))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Running Quest:")
                    .right(config.quest().getName())
                    .build());

            // New status line
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(AIOQuesterScript.status)
                    .build());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}