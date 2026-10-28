package delivery;

import java.util.ArrayList;
import java.util.List;
final class RouteConstraints {
    private RouteConstraints() {}
    static List<String> violations(Route route, DeliveryRequest request) {
        List<String> reasons = new ArrayList<>();
        if (!route.available()) reasons.add("Route unavailable");
        if (!route.origin().equals(request.origin()) || !route.destination().equals(request.destination()))
            reasons.add("Origin/destination mismatch");
        if (route.totalCost(request) > request.maxBudget()) reasons.add("Budget exceeded");
        if (route.finalMinutes() > request.maxDeliveryMinutes()) reasons.add("Delivery deadline exceeded");
        if (route.carbonEmissionKg() > request.maxEmissionKg()) reasons.add("Emission limit exceeded");
        return reasons;
    }
}
