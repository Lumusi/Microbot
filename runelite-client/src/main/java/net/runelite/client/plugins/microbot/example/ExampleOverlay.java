package net.runelite.client.plugins.microbot.example;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;

/**
 * Example Overlay for the Example Plugin.
 * 
 * This overlay demonstrates how to create custom overlays that render
 * information on top of the game client.
 * 
 * Overlays can be used to display:
 * - Status information
 * - Progress bars
 * - Highlights for game objects
 * - Custom UI elements
 */
public class ExampleOverlay extends Overlay {

    private final Client client;
    private final ExampleConfig config;

    @Inject
    public ExampleOverlay(Client client, ExampleConfig config) {
        super();
        this.client = client;
        this.config = config;
        
        // Set overlay properties
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // Don't render if overlay is disabled
        if (!config.enableOverlay()) {
            return null;
        }

        // Set rendering color from config
        graphics.setColor(config.overlayColor());
        
        // Render example text
        renderText(graphics, "Example Plugin Active");
        
        // You can add more rendering logic here:
        // - Draw shapes (rectangles, circles, lines)
        // - Highlight game objects
        // - Display player stats
        // - Show progress bars
        
        return new Dimension(150, 30);
    }

    /**
     * Renders text with a shadow effect for better visibility.
     * 
     * @param graphics The graphics context
     * @param text The text to render
     */
    private void renderText(Graphics2D graphics, String text) {
        // Draw shadow
        graphics.setColor(Color.BLACK);
        graphics.drawString(text, 2, 16);
        
        // Draw main text
        graphics.setColor(config.overlayColor());
        graphics.drawString(text, 1, 15);
    }

    /**
     * Renders a highlighted box around a point in the game world.
     * Useful for highlighting tiles, NPCs, or objects.
     * 
     * @param graphics The graphics context
     * @param point The screen coordinates to highlight
     * @param size The size of the highlight box
     */
    public void renderHighlight(Graphics2D graphics, Point point, int size) {
        graphics.setColor(config.overlayColor());
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRect(point.getX() - size / 2, point.getY() - size / 2, size, size);
    }

    /**
     * Renders a progress bar.
     * 
     * @param graphics The graphics context
     * @param x X position
     * @param y Y position
     * @param width Width of the progress bar
     * @param height Height of the progress bar
     * @param progress Current progress (0.0 to 1.0)
     */
    public void renderProgressBar(Graphics2D graphics, int x, int y, int width, int height, double progress) {
        // Background
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(x, y, width, height);
        
        // Progress
        graphics.setColor(config.overlayColor());
        int progressWidth = (int) (width * Math.min(1.0, Math.max(0.0, progress)));
        graphics.fillRect(x, y, progressWidth, height);
        
        // Border
        graphics.setColor(Color.WHITE);
        graphics.drawRect(x, y, width, height);
    }
}
