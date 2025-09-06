package net.runelite.client.plugins.microbot.actionlogger;

import net.runelite.api.MenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class ActionLoggerOverlay extends OverlayPanel {

    @Inject
    ActionLoggerOverlay(ActionLoggerPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.TOP_RIGHT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        MenuEntry lastAction = ActionLoggerPlugin.lastAction;
        if (lastAction == null) {
            return null; // Don't draw anything if no action has been logged yet
        }

        panelComponent.setPreferredSize(new Dimension(300, 0));
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Last Action Details")
                .color(new Color(0, 255, 127)) // Spring Green
                .build());

        panelComponent.getChildren().add(LineComponent.builder().build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Option:")
                .right(lastAction.getOption())
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Target:")
                .right(lastAction.getTarget())
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Opcode:")
                .right(lastAction.getType().getId() + " (" + lastAction.getType().name() + ")")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Identifier:")
                .right(String.valueOf(lastAction.getIdentifier()))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Param0:")
                .right(String.valueOf(lastAction.getParam0()))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Param1:")
                .right(String.valueOf(lastAction.getParam1()))
                .build());

        return super.render(graphics);
    }
}