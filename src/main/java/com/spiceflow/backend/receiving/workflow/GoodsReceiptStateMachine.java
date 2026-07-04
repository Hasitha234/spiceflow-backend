package com.spiceflow.backend.receiving.workflow;

import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class GoodsReceiptStateMachine {

    private static final Map<GoodsReceiptState, Set<GoodsReceiptState>> TRANSITIONS;

    static {
        Map<GoodsReceiptState, Set<GoodsReceiptState>> map = new EnumMap<>(GoodsReceiptState.class);

        map.put(GoodsReceiptState.DRAFT, Set.of(
                GoodsReceiptState.INSPECTING,
                GoodsReceiptState.CANCELLED
        ));

        map.put(GoodsReceiptState.INSPECTING, Set.of(
                GoodsReceiptState.VERIFIED,
                GoodsReceiptState.CANCELLED
        ));

        map.put(GoodsReceiptState.VERIFIED, Set.of(
                GoodsReceiptState.POSTED,
                GoodsReceiptState.CANCELLED
        ));

        map.put(GoodsReceiptState.POSTED, Set.of());
        map.put(GoodsReceiptState.CANCELLED, Set.of());

        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(GoodsReceiptState from, GoodsReceiptState to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<GoodsReceiptState> allowedNext(GoodsReceiptState state) {
        return TRANSITIONS.getOrDefault(state, Set.of());
    }

    private GoodsReceiptStateMachine() {}
}
