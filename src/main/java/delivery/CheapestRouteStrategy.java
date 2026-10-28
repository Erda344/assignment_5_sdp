package delivery;

import java.util.List;
import java.util.function.ToDoubleFunction;
public final class CheapestRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Cheapest"; }
    protected String formula() { return "cost = (base + distance * rate + fees) * priority multiplier (URGENT 1.2, others 1.0)"; }
    protected ToDoubleFunction<Route> scoring(List<Route> routes, DeliveryRequest request) { return route -> route.totalCost(request); }
}
