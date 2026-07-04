package com.spiceflow.backend.sales.loading.workflow;

import com.spiceflow.backend.sales.loading.domain.LoadingSheetState;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Finite State Machine transition registry for Van Loading Sheets.
 * Eliminates scattered if-else lifecycle checks across services.
 */
public final class LoadingSheetStateMachine {

    private static final Map<LoadingSheetState, Set<LoadingSheetState>> TRANSITIONS;

    static {
        Map<LoadingSheetState, Set<LoadingSheetState>> map = new EnumMap<>(LoadingSheetState.class);

        map.put(LoadingSheetState.DRAFT, Set.of(
                LoadingSheetState.CONFIRMED,
                LoadingSheetState.CANCELLED
        ));

        map.put(LoadingSheetState.CONFIRMED, Set.of());
        map.put(LoadingSheetState.CANCELLED, Set.of());

        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(LoadingSheetState from, LoadingSheetState to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<LoadingSheetState> allowedNext(LoadingSheetState state) {
        return TRANSITIONS.getOrDefault(state, Set.of());
    }

    private LoadingSheetStateMachine() {}
}
