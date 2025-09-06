package net.runelite.client.plugins.microbot.bankskiller;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import java.text.NumberFormat;
import java.util.Locale;

public class BankskillerOverlay extends OverlayPanel {
    private final BankskillerPlugin plugin;

    @Inject
    BankskillerOverlay(BankskillerPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(220, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Microbot Bankskiller")
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(plugin.getStatus())
                    .build());

            Session session = plugin.getSession();
            if (session != null) {
                NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
                panelComponent.getChildren().add(LineComponent.builder().build()); // Spacer
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Runtime:")
                        .right(session.getRuntime())
                        .build());
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("XP Gained:")
                        .right(nf.format(session.getXpGained()) + " (" + nf.format(session.getXpPerHour()) + "/hr)")
                        .build());
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Inventories:")
                        .right(nf.format(session.getInventoriesCompleted()) + " (" + nf.format(session.getInventoriesPerHour()) + "/hr)")
                        .build());
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}