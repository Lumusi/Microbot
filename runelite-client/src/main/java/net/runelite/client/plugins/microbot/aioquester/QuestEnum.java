package net.runelite.client.plugins.microbot.aioquester;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestEnum {
    COOKS_ASSISTANT("Cook's Assistant"),
    X_MARKS_THE_SPOT("X Marks the Spot");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}