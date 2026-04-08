# Combat Plugin

A comprehensive automated combat plugin for Microbot that handles NPC combat training, bossing, and PvM activities.

## Features

### Targeting System
- **Specific NPC Targeting**: Set a specific NPC name to attack
- **Smart Target Selection**: Prioritize by lowest health or closest distance
- **Auto-Retaliation**: Automatically fight back when attacked
- **Line of Sight Detection**: Only attack NPCs you can actually hit

### Survival Features
- **Automatic Eating**: Eat food when health drops below configurable threshold
- **Prayer Management**: Drink prayer potions when prayer is low
- **Super Antifire Support**: Automatically use super antifire against dragons
- **Player Safety**: Detect and avoid other players in PvP areas

### Looting System
- **Smart Looting**: Pick up loot after kills
- **Value Filtering**: Only loot items above a certain value
- **Specific Item Lists**: Configure exact items to always loot
- **Bone Collection**: Optional bone looting for Prayer training

### Special Attacks
- **Configurable Usage**: Enable/disable special attacks
- **Energy Threshold**: Set minimum special attack energy required
- **Low Health Trigger**: Use special only when NPC is below health threshold
- **Energy Management**: Preserve special energy for critical moments

### Overlay
- **Real-time Stats**: View kills, loot count, and session duration
- **Target Highlighting**: Visual highlight on current target
- **Health/Prayer Display**: Monitor your health and prayer levels
- **Status Indicators**: Color-coded status for quick assessment

## Configuration

### General Settings
- **NPC Name**: Name of the NPC to attack (leave empty for any hostile NPC)
- **Attack Style**: Choose between Accurate, Aggressive, Defensive, Controlled, etc.
- **Auto Retaliate**: Enable/disable automatic retaliation

### Targeting Settings
- **Prioritize Lowest Health**: Target wounded NPCs first
- **Prioritize Closest**: Target nearest NPCs first
- **Avoid Players**: Stop attacking if players are nearby
- **Player Safety Distance**: How close players can get before pausing

### Food & Healing
- **Eat At Health %**: Health percentage threshold for eating food (default: 50%)
- **Drink Prayer Potion**: Enable automatic prayer potion usage
- **Prayer Threshold %**: Prayer percentage to drink potion (default: 20%)
- **Use Super Antifire**: Auto-use super antifire against dragons

### Looting Settings
- **Enable Looting**: Turn loot collection on/off
- **Loot Value Threshold**: Minimum coin value to pick up (0 = loot everything)
- **Loot Specific Items**: Comma-separated list of items to always loot
- **Loot Bones**: Pick up bones for Prayer XP

### Special Attacks
- **Use Special Attack**: Enable special attacks
- **Special Attack Threshold %**: Minimum special energy required (default: 50%)
- **Special on Low Health**: Only use special when NPC is low health
- **Low Health Threshold %**: NPC health % to trigger special (default: 25%)

## Usage

### Basic Setup
1. Enable the Combat Plugin in the Microbot plugin hub
2. Configure your desired settings (NPC name, food thresholds, etc.)
3. Make sure you have appropriate gear and supplies in inventory
4. The plugin will automatically start if "Auto Start" is enabled, or manually start it

### Recommended Setup for Training
```
NPC Name: [Your training monster]
Attack Style: [Your preferred style]
Eat At Health %: 60-70%
Prioritize Closest: ✓
Enable Looting: ✓
Loot Bones: ✓ (for Prayer XP)
```

### Bossing Setup
```
NPC Name: [Boss name]
Attack Style: Optimal for boss
Eat At Health %: 70-80%
Drink Prayer Potion: ✓
Prayer Threshold %: 30-40%
Use Special Attack: ✓
Special Attack Threshold %: 50%
Avoid Players: ✓ (in wilderness)
Player Safety Distance: 10+ tiles
```

### Wilderness Safety
When fighting in the wilderness:
- Enable "Avoid Players"
- Set "Player Safety Distance" to 10+ tiles
- Consider setting "Loot Value Threshold" to only grab valuable items quickly
- Keep escape teleport readily available

## Supported Activities

### Training Locations
- Low level: Goblins, Chickens, Cows
- Mid level: Hill Giants, Moss Giants, Lesser Demons
- High level: Abyssal Demons, Dust Devils, Dragons

### Bosses
- General Graardor
- Kree'arra
- Commander Zilyana
- Kril Tsutsaroth
- Vorkath
- Zulrah
- And more...

### Slayer Tasks
The plugin works well for slayer tasks when combined with proper gear setup.

## API Integration

This plugin uses the following Microbot utilities:
- `Rs2Combat`: Combat mechanics, special attacks, attack styles
- `Rs2Npc`: NPC detection and interaction
- `Rs2Player`: Health, prayer, and player status
- `Rs2Inventory`: Food and potion management
- `Rs2GroundItem`: Loot collection
- `Rs2Walker`: Movement and positioning

## Troubleshooting

### Plugin won't start
- Ensure you're logged in to Old School RuneScape
- Check that you have the necessary permissions
- Verify the plugin is enabled in settings

### Not attacking NPCs
- Check that NPC name is spelled correctly (case-insensitive)
- Ensure you're in range of the NPCs
- Verify line of sight isn't blocked
- Check that you have appropriate weapon equipped

### Not eating food
- Make sure food is in your inventory
- Check that "Eat At Health %" is set appropriately
- Verify food names match exactly (e.g., "Shark", "Manta ray")

### Not looting items
- Ensure "Enable Looting" is checked
- Check "Loot Value Threshold" isn't too high
- Verify item names in "Loot Specific Items" are correct

## Safety Notice

⚠️ **Important**: Using automation plugins may violate Jagex's Terms of Service. Use at your own risk. This plugin is provided for educational purposes only.

Best practices:
- Don't use in crowded worlds
- Avoid peak hours
- Don't bot valuable accounts
- Take regular breaks
- Monitor the bot regularly

## Version History

### 1.0
- Initial release
- Basic combat automation
- Target selection system
- Food and prayer management
- Looting system
- Special attack support
- Player safety features
- Real-time overlay

## Contributing

Contributions are welcome! Please submit pull requests with:
- Clear description of changes
- Testing evidence
- Updated documentation if needed

## License

This plugin is part of the Microbot project. See the main project repository for license information.
