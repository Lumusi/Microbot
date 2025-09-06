package net.runelite.client.plugins.microbot.aioagility.enums;

import lombok.Getter;
import net.runelite.client.plugins.microbot.aioagility.courses.*;
import net.runelite.client.plugins.microbot.aioagility.models.QuestRequirement;

@Getter
public enum AgilityCourse {
    DRAYNOR_VILLAGE_ROOFTOP_COURSE("Draynor Village", 1, new DraynorCourse(), true),
    AL_KHARID_ROOFTOP_COURSE("Al Kharid", 20, new AlKharidCourse(), true),
    VARROCK_ROOFTOP_COURSE("Varrock", 30, new VarrockCourse(), true),
    CANIFIS_ROOFTOP_COURSE("Canifis", 40, new QuestRequirement("Priest in Peril", 302, 6), new CanafisCourse(), true),
    FALADOR_ROOFTOP_COURSE("Falador", 50, new FaladorCourse(), true),
    SEERS_VILLAGE_ROOFTOP_COURSE("Seers' Village", 60, new QuestRequirement("Kandarin Hard Diary", 4563, 1, true), new SeersCourse(), true),
    POLLNIVNEACH_ROOFTOP_COURSE("Pollnivneach", 70, new QuestRequirement("Desert Hard Diary", 4558, 1, true), new PollnivneachCourse(), true),
    PRIFDDINAS_AGILITY_COURSE("Prifddinas", 75, new QuestRequirement("Song of the Elves", 2028, 135), new PrifddinasCourse(), true),
    RELLEKKA_ROOFTOP_COURSE("Rellekka", 80, new QuestRequirement("Fremennik Hard Diary", 4560, 1, true), new RellekkaCourse(), true),
    ARDOUGNE_ROOFTOP_COURSE("Ardougne", 90, new QuestRequirement("Ardougne Elite Diary", 4555, 1, true), new ArdougneCourse(), true),

    GNOME_STRONGHOLD_AGILITY_COURSE("Gnome Stronghold", 1, new GnomeStrongholdCourse(), false),
    APE_ATOLL_AGILITY_COURSE("Ape Atoll", 48, new ApeAtollCourse(), false),
    WEREWOLF_COURSE("Werewolf", 60, new WerewolfCourse(), false),
    SHAYZIEN_BASIC_COURSE("Shayzien Basic", 5, new ShayzienBasicCourse(), false),
    SHAYZIEN_ADVANCED_COURSE("Shayzien Advanced", 48, new ShayzienAdvancedCourse(), false),
    COLOSSAL_WYRM_BASIC_COURSE("Colossal Wyrm Basic", 50, new ColossalWyrmBasicCourse(), false),
    COLOSSAL_WYRM_ADVANCED_COURSE("Colossal Wyrm Advanced", 62, new ColossalWyrmAdvancedCourse(), false);

    private final String tooltip;
    private final int levelReq;
    private final QuestRequirement questRequirement;
    private final AgilityCourseHandler handler;
    private final boolean isAioCourse;

    AgilityCourse(String tooltip, int levelReq, QuestRequirement questRequirement, AgilityCourseHandler handler, boolean isAioCourse) {
        this.tooltip = tooltip;
        this.levelReq = levelReq;
        this.questRequirement = questRequirement;
        this.handler = handler;
        this.isAioCourse = isAioCourse;
    }

    AgilityCourse(String tooltip, int levelReq, AgilityCourseHandler handler, boolean isAioCourse) {
        this(tooltip, levelReq, null, handler, isAioCourse);
    }
}