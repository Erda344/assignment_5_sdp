package delivery;

import java.util.List;
import java.util.Objects;
public final class StrategyComparison {
    private final List<RouteStrategy> strategies;
    public StrategyComparison(List<RouteStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
        if (this.strategies.isEmpty()) throw new IllegalArgumentException("At least one strategy required");
    }
    public List<RouteDecision> compare(List<Route> routes, DeliveryRequest request) {
        Objects.requireNonNull(routes, "routes");
        List<Route> snapshot = List.copyOf(routes);
        return strategies.stream().map(strategy -> new RoutePlanner(strategy).plan(snapshot, request)).toList();
    }
}
