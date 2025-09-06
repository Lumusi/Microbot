package net.runelite.client.plugins.microbot.superglassmake;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

@Getter
@RequiredArgsConstructor
public enum GlassmakingMethod {
    GIANT_SEAWEED_SAND("Giant Seaweed (3) & Sand (18)", ItemID.GIANT_SEAWEED, 3, ItemID.BUCKET_SAND, 18) {
        @Override
        public void withdrawMaterials() {
            if (Rs2Bank.hasItem(this.getPrimaryIngredientId())) {
                for (int i = 0; i < 3; i++) {
                    if (Rs2Inventory.isFull()) break;
                    Rs2Bank.withdrawOne(this.getPrimaryIngredientId());
                    sleep(150, 250);
                }
            }
            if (Rs2Bank.hasItem(this.getSecondaryIngredientId())) {
                Rs2Bank.withdrawX(this.getSecondaryIngredientId(), 18);
            }
        }
    },
    SEAWEED_SAND("Seaweed (13) & Sand (13)", ItemID.SEAWEED, 13, ItemID.BUCKET_SAND, 13),
    SODA_ASH_SAND("Soda Ash (13) & Sand (13)", ItemID.SODA_ASH, 13, ItemID.BUCKET_SAND, 13),
    SWAMP_WEED_SAND("Swamp Weed (13) & Sand (13)", ItemID.DORGESH_SWAMP_WEED, 13, ItemID.BUCKET_SAND, 13);

    private final String name;
    private final int primaryIngredientId;
    private final int primaryIngredientAmount;
    private final int secondaryIngredientId;
    private final int secondaryIngredientAmount;

    @Override
    public String toString() {
        return name;
    }

    public void withdrawMaterials() {
        if (Rs2Bank.hasItem(this.getPrimaryIngredientId())) {
            Rs2Bank.withdrawX(this.getPrimaryIngredientId(), this.getPrimaryIngredientAmount());
            sleep(150, 250);
        }
        if (Rs2Bank.hasItem(this.getSecondaryIngredientId())) {
            Rs2Bank.withdrawX(this.getSecondaryIngredientId(), this.getSecondaryIngredientAmount());
            sleep(150, 250);
        }
    }
}