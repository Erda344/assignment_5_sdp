package delivery;

import java.util.List;
import java.util.function.ToDoubleFunction;

/** Fifth strategy, added after the original four strategies were verified. */
public final class DistanceFirstRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Distance first"; }
    protected String formula() { return "distance score = distance in km"; }
    protected ToDoubleFunction<Route> scoring(List<Route> routes, DeliveryRequest request) {
        return Route::distanceKm;
    }
}
