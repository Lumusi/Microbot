package net.runelite.client.plugins.microbot.aioagility;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Locale;

public class AioAgilityOverlay extends OverlayPanel {
    private final AioAgilityPlugin plugin;
    private final AioAgilityConfig config;
    private final BufferedImage agilityIcon;

    private final PanelComponent headerPanel = new PanelComponent();

    @Inject
    AioAgilityOverlay(AioAgilityPlugin plugin, AioAgilityConfig config) {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();

        final BufferedImage agiIcon = ImageUtil.loadImageResource(getClass(), "/skill_icons/agility.png");
        this.agilityIcon = ImageUtil.resizeImage(agiIcon, 36, 36);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.setFont(FontManager.getRunescapeBoldFont());

        panelComponent.setPreferredSize(new Dimension(240, 0));
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("AIO Agility " + AioAgilityScript.version)
                .color(new Color(0, 170, 255))
                .build());

        if (agilityIcon != null) {
            panelComponent.getChildren().add(new ImageComponent(agilityIcon));
        }

        String runtimeString = formatDuration(plugin.getSessionDuration());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Runtime:")
                .right(runtimeString)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(Microbot.status)
                .rightColor(Color.ORANGE)
                .build());

        if (config.aioMode()) {
            panelComponent.getChildren().add(LineComponent.builder().left("Mode:").right("AIO").rightColor(Color.YELLOW).build());
        } else {
            panelComponent.getChildren().add(LineComponent.builder().left("Mode:").right("Manual").rightColor(Color.YELLOW).build());
            panelComponent.getChildren().add(LineComponent.builder().left("Course:").right(config.agilityCourse().getTooltip()).build());
        }

        panelComponent.getChildren().add(LineComponent.builder().build());

        int currentXp = Microbot.getClient().getSkillExperience(Skill.AGILITY);
        int xpGained = plugin.getXpGained();

        panelComponent.getChildren().add(LineComponent.builder()
                .left("XP Gained:")
                .right("+" + NumberFormat.getNumberInstance(Locale.US).format(xpGained))
                .rightColor(Color.YELLOW)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Agility XP:")
                .right(NumberFormat.getNumberInstance(Locale.US).format(currentXp))
                .rightColor(Color.CYAN)
                .build());

        String obstacleIndex = plugin.getCourseHandler() != null ?
                Integer.toString(plugin.getCourseHandler().getCurrentObstacleIndex()) : "N/A";
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Obstacle Index:")
                .right(obstacleIndex)
                .build());

        return super.render(graphics);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}