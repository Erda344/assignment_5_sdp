package delivery;

import java.util.List;
import java.util.function.ToDoubleFunction;
public final class FastestRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Fastest"; }
    protected String formula() { return "time = estimated minutes + traffic penalty + weather penalty"; }
    protected ToDoubleFunction<Route> scoring(List<Route> routes, DeliveryRequest request) { return Route::finalMinutes; }
}
