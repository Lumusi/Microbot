# Microbot Plugin Development Guide

This guide explains how to create plugins for the Microbot platform using the Microbot API.

## Table of Contents

1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Creating a Basic Plugin](#creating-a-basic-plugin)
4. [Plugin Components](#plugin-components)
5. [Using Microbot Utilities](#using-microbot-utilities)
6. [Integration with Microbot API](#integration-with-microbot-api)
7. [Best Practices](#best-practices)

## Overview

Microbot is a plugin framework built on top of RuneLite for Old School RuneScape. It provides:
- A robust plugin architecture
- Utility classes for common game interactions
- Script automation capabilities
- Integration with the Microbot cloud API for session management

## Project Structure

Plugins are located in: `runelite-client/src/main/java/net/runelite/client/plugins/microbot/`

### Example Plugin Structure

```
example/
├── ExamplePlugin.java          # Main plugin class
├── ExampleConfig.java          # Plugin configuration
├── ExampleOverlay.java         # Visual overlay
├── ExampleScript.java          # Automation script
└── README.md                   # Plugin documentation
```

## Creating a Basic Plugin

### Step 1: Create the Plugin Class

```java
@PluginDescriptor(
    name = "Your Plugin",
    description = "Description of your plugin",
    tags = {"tag1", "tag2"},
    enabledByDefault = false
)
@Slf4j
public class YourPlugin extends Plugin {
    // Plugin implementation
}
```

### Step 2: Add Configuration

```java
@ConfigGroup("yourplugin")
public interface YourConfig extends Config {
    @ConfigItem(
        keyName = "setting1",
        name = "Setting 1",
        description = "Description"
    )
    default boolean setting1() {
        return true;
    }
}
```

### Step 3: Create an Overlay (Optional)

```java
public class YourOverlay extends Overlay {
    @Override
    public Dimension render(Graphics2D graphics) {
        // Rendering logic
        return new Dimension(100, 50);
    }
}
```

## Plugin Components

### Main Plugin Class (`ExamplePlugin.java`)

The main plugin class handles:
- Lifecycle management (startUp, shutDown)
- Event handling (@Subscribe methods)
- Dependency injection
- Configuration management

**Key Methods:**
- `startUp()` - Called when plugin is enabled
- `shutDown()` - Called when plugin is disabled
- `onGameStateChanged()` - Handle login/logout events
- `onConfigChanged()` - Handle configuration changes

### Configuration (`ExampleConfig.java`)

Defines user-configurable settings:
- Boolean toggles
- Color pickers
- Text inputs
- Dropdown selections
- Hotkeys

### Overlay (`ExampleOverlay.java`)

Renders visual elements on the game client:
- Status displays
- Highlights
- Progress bars
- Custom UI elements

### Script (`ExampleScript.java`)

Automation logic for gameplay tasks:
- Walking/navigation
- Skill training
- Combat
- Resource gathering

## Using Microbot Utilities

Microbot provides extensive utility classes:

### Player Utilities
```java
Rs2Player.isMoving()
Rs2Player.getWorldLocation()
Rs2Player.getHealthPercentage()
```

### Inventory Utilities
```java
Rs2Inventory.useItem(String itemName)
Rs2Inventory.contains(String itemName)
Rs2Inventory.getCount()
```

### Walker Utilities
```java
Rs2Walker.walkTo(WorldPoint target)
Rs2Walker.canReach(WorldPoint target)
```

### Bank Utilities
```java
Rs2Bank.openBank()
Rs2Bank.depositAll()
Rs2Bank.withdrawItem(String itemName, int amount)
```

## Integration with Microbot API

The Microbot API (`MicrobotApi.java`) provides:

### Session Management
```java
// Open a new session
UUID sessionId = microbotApi.microbotOpen();

// Ping session to keep it alive
microbotApi.microbotPing(sessionId, isLoggedIn);

// Close session when done
microbotApi.microbotDelete(sessionId);
```

### External Plugin System

Plugins can be loaded dynamically through the external plugin system:

1. **Plugin Manifest** - Define metadata in `MicrobotPluginManifest`:
   - Internal name
   - Display name
   - Version
   - Download URL
   - SHA256 hash

2. **Plugin Manager** - `MicrobotPluginManager` handles:
   - Downloading plugins
   - Loading/unloading
   - Dependency resolution
   - Version checking

### Schedulable Plugins

For integration with the Scheduler:

```java
public class YourPlugin extends Plugin implements SchedulablePlugin {
    
    @Override
    public LogicalCondition getStopCondition() {
        // Define when scheduler should stop this plugin
        return new AndCondition();
    }
    
    @Override
    public void reportFinished(String reason, boolean success) {
        // Report completion to scheduler
    }
}
```

## Best Practices

### Code Style
- Follow Java 11 conventions
- Use Lombok for boilerplate reduction
- Keep lines under 120 characters
- Use tabs for indentation

### Documentation
- Add Javadoc comments to public methods
- Include usage examples
- Document configuration options
- Update AGENTS.md if adding new patterns

### Testing
- Write unit tests for utility methods
- Test edge cases
- Verify overlay rendering
- Test configuration changes

### Performance
- Avoid blocking the client thread
- Use async operations when possible
- Cache expensive computations
- Clean up resources properly

### Error Handling
- Log errors with context
- Handle null values gracefully
- Provide meaningful error messages
- Implement proper cleanup on failure

## Example Usage

### Starting a Script from Plugin

```java
@Override
protected void startUp() {
    if (config.autoStart()) {
        ExampleScript script = new ExampleScript();
        script.run(client);
    }
}
```

### Handling Events

```java
@Subscribe
public void onChatMessage(ChatMessage event) {
    if (event.getMessage().contains("target message")) {
        // Handle specific chat message
    }
}
```

### Accessing Game State

```java
Client client = getClient();
Player localPlayer = client.getLocalPlayer();
WorldPoint location = localPlayer.getWorldLocation();
List<NPC> npcs = client.getNpcs();
```

## Resources

- **Existing Examples**: See `VoxPlugins/schedulable/example/` for complete examples
- **Utilities**: Browse `util/` package for available helper classes
- **API Documentation**: Check `MicrobotApi.java` for API integration details
- **Scheduler API**: Review `SchedulablePlugin.java` for scheduler integration

## Getting Help

- Join the Microbot Discord server
- Check existing plugins for reference implementations
- Review the AGENTS.md file for development guidelines
- Consult the docs/ directory for additional documentation
