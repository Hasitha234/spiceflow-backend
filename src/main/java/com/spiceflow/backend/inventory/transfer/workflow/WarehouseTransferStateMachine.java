package com.spiceflow.backend.inventory.transfer.workflow;

import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class WarehouseTransferStateMachine {

    private static final Map<WarehouseTransferState, Set<WarehouseTransferState>> TRANSITIONS;

    static {
        Map<WarehouseTransferState, Set<WarehouseTransferState>> map = new EnumMap<>(WarehouseTransferState.class);

        map.put(WarehouseTransferState.DRAFT, Set.of(
                WarehouseTransferState.REQUESTED,
                WarehouseTransferState.CANCELLED
        ));

        map.put(WarehouseTransferState.REQUESTED, Set.of(
                WarehouseTransferState.APPROVED,
                WarehouseTransferState.CANCELLED
        ));

        map.put(WarehouseTransferState.APPROVED, Set.of(
                WarehouseTransferState.SHIPPED,
                WarehouseTransferState.CANCELLED
        ));

        map.put(WarehouseTransferState.SHIPPED, Set.of(
                WarehouseTransferState.RECEIVED
        ));

        map.put(WarehouseTransferState.RECEIVED, Set.of());
        map.put(WarehouseTransferState.CANCELLED, Set.of());

        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(WarehouseTransferState from, WarehouseTransferState to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<WarehouseTransferState> allowedNext(WarehouseTransferState state) {
        return TRANSITIONS.getOrDefault(state, Set.of());
    }

    private WarehouseTransferStateMachine() {}
}
