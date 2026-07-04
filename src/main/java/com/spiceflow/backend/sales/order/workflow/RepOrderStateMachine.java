package com.spiceflow.backend.sales.order.workflow;

import com.spiceflow.backend.sales.order.domain.RepOrderState;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Finite State Machine transition registry for Rep Orders.
 * Eliminates scattered if-else lifecycle checks across services.
 */
public final class RepOrderStateMachine {

    private static final Map<RepOrderState, Set<RepOrderState>> TRANSITIONS;

    static {
        Map<RepOrderState, Set<RepOrderState>> map = new EnumMap<>(RepOrderState.class);

        map.put(RepOrderState.DRAFT, Set.of(
                RepOrderState.SUBMITTED,
                RepOrderState.CANCELLED
        ));

        map.put(RepOrderState.SUBMITTED, Set.of(
                RepOrderState.APPROVED,
                RepOrderState.CANCELLED
        ));

        map.put(RepOrderState.APPROVED, Set.of(
                RepOrderState.LOADED,
                RepOrderState.CANCELLED
        ));

        map.put(RepOrderState.LOADED, Set.of(
                RepOrderState.DELIVERED
        ));

        map.put(RepOrderState.DELIVERED, Set.of());
        map.put(RepOrderState.CANCELLED, Set.of());

        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(RepOrderState from, RepOrderState to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<RepOrderState> allowedNext(RepOrderState state) {
        return TRANSITIONS.getOrDefault(state, Set.of());
    }

    private RepOrderStateMachine() {}
}
