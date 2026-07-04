package com.spiceflow.backend.purchasing.workflow;

import com.spiceflow.backend.purchasing.domain.PurchaseOrderState;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class PurchaseOrderStateMachine {

    private static final Map<PurchaseOrderState, Set<PurchaseOrderState>> TRANSITIONS;

    static {
        Map<PurchaseOrderState, Set<PurchaseOrderState>> map = new EnumMap<>(PurchaseOrderState.class);

        map.put(PurchaseOrderState.DRAFT, Set.of(
                PurchaseOrderState.SUBMITTED
        ));

        map.put(PurchaseOrderState.SUBMITTED, Set.of(
                PurchaseOrderState.APPROVED,
                PurchaseOrderState.REJECTED
        ));

        map.put(PurchaseOrderState.APPROVED, Set.of(
                PurchaseOrderState.ORDERED
        ));

        map.put(PurchaseOrderState.ORDERED, Set.of(
                PurchaseOrderState.PARTIALLY_RECEIVED,
                PurchaseOrderState.RECEIVED
        ));

        map.put(PurchaseOrderState.PARTIALLY_RECEIVED, Set.of(
                PurchaseOrderState.RECEIVED
        ));

        map.put(PurchaseOrderState.RECEIVED, Set.of(
                PurchaseOrderState.CLOSED
        ));

        map.put(PurchaseOrderState.REJECTED, Set.of());

        map.put(PurchaseOrderState.CLOSED, Set.of());

        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(PurchaseOrderState from, PurchaseOrderState to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<PurchaseOrderState> allowedNext(PurchaseOrderState state) {
        return TRANSITIONS.getOrDefault(state, Set.of());
    }

    private PurchaseOrderStateMachine() {}
}
