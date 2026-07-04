package com.spiceflow.backend.sales.collection.workflow;

import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Finite State Machine transition registry for Cash Collection workflow.
 *
 * Valid transitions:
 *   PENDING -> CONFIRMED
 *   PENDING -> CANCELLED
 *   CONFIRMED -> CANCELLED (reversal support)
 */
public final class CashCollectionStateMachine {

    private static final Map<CashCollectionState, Set<CashCollectionState>> TRANSITIONS;

    static {
        Map<CashCollectionState, Set<CashCollectionState>> map = new EnumMap<>(CashCollectionState.class);

        map.put(CashCollectionState.PENDING, Set.of(
                CashCollectionState.CONFIRMED,
                CashCollectionState.CANCELLED
        ));

        map.put(CashCollectionState.CONFIRMED, Set.of(
                CashCollectionState.CANCELLED
        ));

        map.put(CashCollectionState.CANCELLED, Set.of());

        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(CashCollectionState from, CashCollectionState to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<CashCollectionState> allowedNext(CashCollectionState state) {
        return TRANSITIONS.getOrDefault(state, Set.of());
    }
}
