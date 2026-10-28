package delivery;

import java.util.List;
import java.util.function.ToDoubleFunction;
public final class BalancedRouteStrategy extends ScoredRouteStrategy {
    private final double timeWeight, costWeight, distanceWeight, emissionWeight;
    public BalancedRouteStrategy(double time, double cost, double distance, double emission) {
        Validation.nonNegative(time, "Time weight");
        Validation.nonNegative(cost, "Cost weight");
        Validation.nonNegative(distance, "Distance weight");
        Validation.nonNegative(emission, "Emission weight");
        if (Math.abs(time + cost + distance + emission - 1.0) > 1e-9)
            throw new IllegalArgumentException("Weights must sum to 1.0");
        timeWeight = time; costWeight = cost; distanceWeight = distance; emissionWeight = emission;
    }
    public String getStrategyName() { return "Balanced"; }
    protected String formula() {
        return "balanced score = normalized time * " + timeWeight + " + normalized cost * " + costWeight
                + " + normalized distance * " + distanceWeight + " + normalized CO2 * " + emissionWeight;
    }
    protected ToDoubleFunction<Route> scoring(List<Route> routes, DeliveryRequest request) {
        ToDoubleFunction<Route> time = normalize(routes, Route::finalMinutes);
        ToDoubleFunction<Route> cost = normalize(routes, route -> route.totalCost(request));
        ToDoubleFunction<Route> distance = normalize(routes, Route::distanceKm);
        ToDoubleFunction<Route> emission = normalize(routes, Route::carbonEmissionKg);
        return route -> time.applyAsDouble(route) * timeWeight + cost.applyAsDouble(route) * costWeight
                + distance.applyAsDouble(route) * distanceWeight + emission.applyAsDouble(route) * emissionWeight;
    }
    private ToDoubleFunction<Route> normalize(List<Route> routes, ToDoubleFunction<Route> metric) {
        double min = routes.stream().mapToDouble(metric).min().orElseThrow();
        double max = routes.stream().mapToDouble(metric).max().orElseThrow();
        return route -> max == min ? 0.0 : (metric.applyAsDouble(route) - min) / (max - min);
    }
}
