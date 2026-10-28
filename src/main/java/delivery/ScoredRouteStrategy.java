package delivery;

import java.util.*;
import java.util.function.ToDoubleFunction;
abstract class ScoredRouteStrategy implements RouteStrategy {
    protected abstract ToDoubleFunction<Route> scoring(List<Route> feasible, DeliveryRequest request);
    protected abstract String formula();
    @Override
    public final RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        Objects.requireNonNull(routes, "routes");
        Objects.requireNonNull(request, "request");
        List<Route> feasible = new ArrayList<>();
        Map<String, String> rejected = new LinkedHashMap<>();
        Set<String> ids = new HashSet<>();
        for (Route route : routes) {
            Objects.requireNonNull(route, "route");
            if (!ids.add(route.id())) throw new IllegalArgumentException("Duplicate route ID: " + route.id());
            List<String> violations = RouteConstraints.violations(route, request);
            if (violations.isEmpty()) feasible.add(route);
            else rejected.put(route.id(), String.join("; ", violations));
        }
        if (feasible.isEmpty()) throw new NoSuitableRouteException("No suitable route: "
                + (routes.isEmpty() ? "empty route list" : rejected));
        ToDoubleFunction<Route> score = scoring(List.copyOf(feasible), request);
        Route selected = feasible.stream().min(Comparator.comparingDouble(score).thenComparing(Route::id)).orElseThrow();
        String reason = String.format(Locale.ROOT,
                "%s; base time %d + traffic %d + weather %d = %.1f min; total cost %.2f; distance %.2f km; CO2 %.2f kg; score %.6f. Lowest score wins; ties use route ID.",
                formula(), selected.estimatedMinutes(), selected.trafficPenaltyMinutes(), selected.weatherPenaltyMinutes(),
                selected.finalMinutes(), selected.totalCost(request), selected.distanceKm(), selected.carbonEmissionKg(), score.applyAsDouble(selected));
        return new RouteDecision(selected, getStrategyName(), score.applyAsDouble(selected), reason, rejected);
    }
}
