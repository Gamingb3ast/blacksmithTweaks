package com.gamingb3ast.blacksmithTweaks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BT_ShiftHandler {
    private static final Map<UUID, Boolean> playerShiftStates = new HashMap();

    public static void setPlayerShiftState(UUID playerId, boolean isShiftDown) {
        playerShiftStates.put(playerId, isShiftDown);
    }

    public static boolean isPlayerShiftDown(UUID playerId) {
        return playerShiftStates.getOrDefault(playerId, false);
    }
}
