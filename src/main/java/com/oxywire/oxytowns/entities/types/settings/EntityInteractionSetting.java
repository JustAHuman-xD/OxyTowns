package com.oxywire.oxytowns.entities.types.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EntityInteractionSetting {
    ALL("All"),
    TAMED("Tamed"),
    NONE("None");

    private final String name;

    public EntityInteractionSetting getNext() {
        return getNextSetting(this);
    }

    /**
     * Helper method to toggle through settings.
     *
     * @param setting the setting to get from the enum
     * @return enum setting value
     */
    public static EntityInteractionSetting getNextSetting(final EntityInteractionSetting setting) {
        final int index = setting.ordinal();
        int nextIndex = index + 1;
        final EntityInteractionSetting[] interactionsSettings = EntityInteractionSetting.values();
        nextIndex %= interactionsSettings.length;
        return interactionsSettings[nextIndex];
    }
}
