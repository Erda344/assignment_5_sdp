package delivery;

import java.util.Objects;
public record DeliveryRequest(Location origin, Location destination, DeliveryPriority priority,
                              double maxBudget, int maxDeliveryMinutes,
                              boolean ecoPreferred, double maxEmissionKg) {
    public DeliveryRequest {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(priority, "priority");
        Validation.nonNegative(maxBudget, "Budget");
        Validation.nonNegative(maxEmissionKg, "Emission limit");
        if (maxDeliveryMinutes <= 0) throw new IllegalArgumentException("Deadline must be positive");
    }
}
