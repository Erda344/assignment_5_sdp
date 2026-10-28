package delivery;

import java.util.*;
public final class StrategySelector {
    private final Map<DeliveryPriority, RouteStrategy> strategies;
    public StrategySelector(Map<DeliveryPriority, RouteStrategy> strategies) {
        this.strategies = Map.copyOf(strategies);
        if (!this.strategies.keySet().containsAll(EnumSet.allOf(DeliveryPriority.class)))
            throw new IllegalArgumentException("Every priority needs a strategy");
    }
    public RouteStrategy select(DeliveryRequest request) {
        Objects.requireNonNull(request, "request");
        // Urgency takes precedence over the optional eco preference.
        DeliveryPriority key = request.priority() != DeliveryPriority.URGENT && request.ecoPreferred()
                ? DeliveryPriority.ECO : request.priority();
        return strategies.get(key);
    }
}
