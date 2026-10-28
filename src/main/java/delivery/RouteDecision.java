package delivery;

import java.util.Map;
import java.util.Objects;
public record RouteDecision(Route route, String strategyName, double score,
                            String explanation, Map<String, String> rejectedRoutes) {
    public RouteDecision {
        Objects.requireNonNull(route, "route");
        Validation.text(strategyName, "Strategy name");
        Validation.text(explanation, "Explanation");
        Validation.nonNegative(score, "Score");
        rejectedRoutes = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(rejectedRoutes));
    }
}
