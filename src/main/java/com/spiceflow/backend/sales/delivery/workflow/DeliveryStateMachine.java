package com.spiceflow.backend.sales.delivery.workflow;

import com.spiceflow.backend.sales.delivery.domain.DeliveryState;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Finite State Machine transition registry for Customer Deliveries.
 * Eliminates scattered if-else lifecycle checks across services.
 *
 * Valid transitions:
 *   IN_PROGRESS -> DISPATCHED
 *   IN_PROGRESS -> COMPLETED  (direct completion — field drivers may not use DISPATCHED step)
 *   IN_PROGRESS -> CANCELLED
 *   DISPATCHED  -> COMPLETED
 *   DISPATCHED  -> CANCELLED
 */
public final class DeliveryStateMachine {

    private static final Map<DeliveryState, Set<DeliveryState>> TRANSITIONS;

    static {
        Map<DeliveryState, Set<DeliveryState>> map = new EnumMap<>(DeliveryState.class);

        map.put(DeliveryState.IN_PROGRESS, Set.of(
                DeliveryState.DISPATCHED,
                DeliveryState.COMPLETED,
                DeliveryState.CANCELLED
        ));

        map.put(DeliveryState.DISPATCHED, Set.of(
                DeliveryState.COMPLETED,
                DeliveryState.CANCELLED
        ));

        map.put(DeliveryState.COMPLETED, Set.of());
        map.put(DeliveryState.CANCELLED, Set.of());

        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(DeliveryState from, DeliveryState to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<DeliveryState> allowedNext(DeliveryState state) {
        return TRANSITIONS.getOrDefault(state, Set.of());
    }

    private DeliveryStateMachine() {}
}
