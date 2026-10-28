package delivery;

import java.util.List;
public final class DemoData {
    public static final Location ORIGIN = new Location("Warehouse", 51.1694, 71.4491);
    public static final Location DESTINATION = new Location("Customer", 51.12, 71.48);
    private DemoData() {}
    public static Route route(String id, double distance, int minutes, int traffic, int weather,
                              double base, double rate, double fees, double emission, boolean available) {
        return new Route(id, ORIGIN, DESTINATION, distance, minutes, traffic, weather, base, rate, fees, emission, available);
    }
    public static List<Route> routes() {
        return List.of(
            route("A", 10, 18, 12, 0, 1500, 150, 500, 4, true),
            route("B", 14, 20, 2, 0, 1800, 150, 400, 5, true),
            route("C", 8, 42, 0, 0, 900, 100, 100, 0.8, true),
            route("D", 12, 35, 5, 0, 400, 50, 100, 3, true),
            route("E", 6, 28, 3, 2, 1600, 180, 200, 2, true),
            route("F", 9, 15, 0, 0, 300, 20, 0, 0.1, false),
            route("G", 20, 24, 2, 2, 1000, 140, 300, 6, true),
            route("H", 11, 30, 2, 1, 1100, 110, 150, 1.5, true));
    }
    public static DeliveryRequest request(DeliveryPriority priority, double budget, int deadline, boolean eco) {
        return new DeliveryRequest(ORIGIN, DESTINATION, priority, budget, deadline, eco, 10);
    }
}
