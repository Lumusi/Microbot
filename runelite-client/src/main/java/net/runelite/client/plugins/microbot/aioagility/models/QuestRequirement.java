package net.runelite.client.plugins.microbot.aioagility.models;

public final class QuestRequirement {
    private final String name;
    private final int varpId;
    private final int requiredState;
    private final boolean isVarbit;

    public QuestRequirement(String name, int varpId, int requiredState, boolean isVarbit) {
        this.name = name;
        this.varpId = varpId;
        this.requiredState = requiredState;
        this.isVarbit = isVarbit;
    }

    public QuestRequirement(String name, int varpId, int requiredState) {
        this(name, varpId, requiredState, false);
    }

    public String getName() { return name; }
    public int getVarpId() { return varpId; }
    public int getRequiredState() { return requiredState; }
    public boolean isVarbit() { return isVarbit; }
}