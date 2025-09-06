package net.runelite.client.plugins.microbot.bankskiller;

import net.runelite.api.Client;
import net.runelite.api.Skill;

public class Session {
    private final long startTime;
    private final long startXp;
    private long currentXp;
    private int inventoriesCompleted;

    public Session(Client client, Skill skill) {
        this.startTime = System.currentTimeMillis();
        this.startXp = client.getSkillExperience(skill);
        this.currentXp = this.startXp;
        this.inventoriesCompleted = 0;
    }

    public void incrementInventories() {
        this.inventoriesCompleted++;
    }

    public void updateXp(long newXp) {
        this.currentXp = newXp;
    }

    public String getRuntime() {
        long millis = System.currentTimeMillis() - startTime;
        long hours = millis / 3600000;
        millis %= 3600000;
        long minutes = millis / 60000;
        millis %= 60000;
        long seconds = millis / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public long getXpGained() {
        return currentXp - startXp;
    }

    public int getXpPerHour() {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        if (elapsedMillis == 0) return 0;
        return (int) ((getXpGained() * 3600000.0) / elapsedMillis);
    }

    public int getInventoriesCompleted() {
        return inventoriesCompleted;
    }

    public int getInventoriesPerHour() {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        if (elapsedMillis == 0) return 0;
        return (int) ((inventoriesCompleted * 3600000.0) / elapsedMillis);
    }
}