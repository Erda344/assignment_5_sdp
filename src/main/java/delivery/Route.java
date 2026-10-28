package delivery;

import java.util.Objects;
public record Route(String id, Location origin, Location destination, double distanceKm,
                    int estimatedMinutes, int trafficPenaltyMinutes, int weatherPenaltyMinutes,
                    double baseCost, double costPerKm, double extraFees,
                    double carbonEmissionKg, boolean available) {
    public Route {
        Validation.text(id, "Route ID");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
        Validation.nonNegative(distanceKm, "Distance");
        Validation.nonNegative(baseCost, "Base cost");
        Validation.nonNegative(costPerKm, "Cost per km");
        Validation.nonNegative(extraFees, "Extra fees");
        Validation.nonNegative(carbonEmissionKg, "Emissions");
        if (distanceKm == 0 || estimatedMinutes <= 0 || trafficPenaltyMinutes < 0 || weatherPenaltyMinutes < 0)
            throw new IllegalArgumentException("Distance/time must be positive; penalties non-negative");
        if (!Double.isFinite((baseCost + distanceKm * costPerKm + extraFees) * 1.2))
            throw new IllegalArgumentException("Total cost overflows");
    }
    public double finalMinutes() {
        return (double) estimatedMinutes + trafficPenaltyMinutes + weatherPenaltyMinutes;
    }
    public double totalCost(DeliveryRequest request) {
        double surcharge = request.priority() == DeliveryPriority.URGENT ? 1.2 : 1.0;
        return (baseCost + distanceKm * costPerKm + extraFees) * surcharge;
    }
}
