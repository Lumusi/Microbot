package net.runelite.client.plugins.microbot.combat;

/**
 * Enum representing different attack styles in combat.
 */
public enum AttackStyle {
    ACCURATE("Accurate"),
    AGGRESSIVE("Aggressive"),
    DEFENSIVE("Defensive"),
    CONTROLLED("Controlled"),
    RAPID("Rapid"),
    LONGRANGE("Longrange"),
    RANGING("Ranging"),
    MAGIC("Magic");

    private final String displayName;

    AttackStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
