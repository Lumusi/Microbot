package net.runelite.client.plugins.microbot.boltfletching;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class BoltFletchingOverlay extends OverlayPanel {

    private final BoltFletchingScript boltFletchingScript;

    @Inject
    public BoltFletchingOverlay(BoltFletchingScript boltFletchingScript) {
        this.boltFletchingScript = boltFletchingScript;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(200, 300));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Bolt Fletching")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(boltFletchingScript.getStatus().name())
                .build());

        return super.render(graphics);
    }
}