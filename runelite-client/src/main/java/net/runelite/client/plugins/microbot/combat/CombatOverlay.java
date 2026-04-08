package net.runelite.client.plugins.microbot.combat;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;

import javax.inject.Inject;
import java.awt.*;

/**
 * Overlay for the Combat Plugin.
 * 
 * Displays combat information including:
 * - Current target info
 * - Combat stats (kills, loot)
 * - Health and prayer status
 * - Target highlight on game canvas
 */
public class CombatOverlay extends Overlay {

    private final Client client;
    private final CombatPlugin plugin;
    private final CombatConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private CombatOverlay(Client client, CombatPlugin plugin, CombatConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isScriptRunning()) {
            return null;
        }

        panelComponent.getChildren().clear();
        
        // Add title with color
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Combat Script")
                .color(Color.WHITE)
                .build());

        // Add script status
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right(plugin.isScriptRunning() ? "Running" : "Stopped")
                .rightColor(plugin.isScriptRunning() ? Color.GREEN : Color.RED)
                .build());

        // Get current target from script
        CombatScript script = plugin.getScript();
        if (script != null) {
            Rs2NpcModel target = script.getCurrentTarget();
            
            if (target != null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Target:")
                        .right(target.getName())
                        .build());
                
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Target Health:")
                        .right(target.getHealthRatio() + "/" + target.getHealthScale())
                        .rightColor(getHealthColor((int)target.getHealthPercentage()))
                        .build());
                
                // Highlight target on canvas
                highlightTarget(graphics, target);
            } else {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Target:")
                        .right("None")
                        .rightColor(Color.GRAY)
                        .build());
            }

            // Add stats
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Kills:")
                    .right(String.valueOf(script.getKillsCount()))
                    .build());
            
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Looted:")
                    .right(String.valueOf(script.getLootCount()))
                    .build());
        }

        // Add player stats
        Player player = client.getLocalPlayer();
        if (player != null) {
            int healthPercent = getHealthPercent();
            int prayerPercent = getPrayerPercent();
            
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Health:")
                    .right(healthPercent + "%")
                    .rightColor(getHealthColor(healthPercent))
                    .build());
            
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Prayer:")
                    .right(prayerPercent + "%")
                    .rightColor(getPrayerColor(prayerPercent))
                    .build());
        }

        return panelComponent.render(graphics);
    }

    /**
     * Highlight the current target on the game canvas.
     */
    private void highlightTarget(Graphics2D graphics, Rs2NpcModel target) {
        if (target == null || target.getRuneliteNpc() == null) {
            return;
        }

        NPC npc = target.getRuneliteNpc();
        LocalPoint localPoint = npc.getLocalLocation();
        
        if (localPoint == null) {
            return;
        }

        // Get the convex hull of the NPC for accurate highlighting
        Shape objectClickBox = Perspective.getCanvasTileAreaPoly(client, localPoint, npc.getComposition().getSize());
        
        if (objectClickBox != null) {
            Graphics2D g2d = (Graphics2D) graphics.create();
            Color overlayColor = config.overlayColor() != null ? config.overlayColor() : Color.RED;
            g2d.setColor(overlayColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(objectClickBox);
            g2d.setColor(new Color(overlayColor.getRed(), overlayColor.getGreen(), 0, 50));
            g2d.fill(objectClickBox);
            g2d.dispose();
        }
    }

    /**
     * Get player health percentage.
     */
    private int getHealthPercent() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return 0;
        }
        
        int maxHealth = player.getCombatLevel() > 0 ? player.getCombatLevel() * 10 : 100;
        int currentHealth = player.getHealthRatio();
        
        if (currentHealth <= 0) {
            return 0;
        }
        
        // Approximate health percentage based on health ratio
        return (int) ((currentHealth / 30.0) * 100);
    }

    /**
     * Get player prayer percentage.
     */
    private int getPrayerPercent() {
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        
        if (maxPrayer <= 0) {
            return 0;
        }
        
        return (int) ((currentPrayer / (double) maxPrayer) * 100);
    }

    /**
     * Get color based on health percentage.
     */
    private Color getHealthColor(int percent) {
        if (percent > 70) {
            return Color.GREEN;
        } else if (percent > 40) {
            return Color.YELLOW;
        } else if (percent > 20) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }

    /**
     * Get color based on prayer percentage.
     */
    private Color getPrayerColor(int percent) {
        if (percent > 50) {
            return Color.CYAN;
        } else if (percent > 20) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }
}
