package delivery;

import java.util.List;
import java.util.function.ToDoubleFunction;
public final class EcoFriendlyRouteStrategy extends ScoredRouteStrategy {
    public String getStrategyName() { return "Eco"; }
    protected String formula() { return "eco score = CO2 kg * 0.8 + distance km * 0.2 (simulation coefficients)"; }
    protected ToDoubleFunction<Route> scoring(List<Route> routes, DeliveryRequest request) {
        return route -> route.carbonEmissionKg() * 0.8 + route.distanceKm() * 0.2;
    }
}
