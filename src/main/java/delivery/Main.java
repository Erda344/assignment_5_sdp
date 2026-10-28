package delivery;

import java.util.*;
public final class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        RouteStrategy fastest = new FastestRouteStrategy();
        RouteStrategy cheapest = new CheapestRouteStrategy();
        RouteStrategy eco = new EcoFriendlyRouteStrategy();
        RouteStrategy balanced = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);
        List<RouteStrategy> strategies = List.of(fastest, cheapest, eco, balanced);
        List<Route> routes = DemoData.routes();
        DeliveryRequest standard = DemoData.request(DeliveryPriority.STANDARD, 10000, 60, false);
        RoutePlanner planner = new RoutePlanner(fastest);
        System.out.println("RUNTIME SWITCHING - same planner, three changes");
        print(planner.plan(routes, standard));
        for (RouteStrategy next : List.of(cheapest, eco, balanced)) {
            planner.setStrategy(next);
            print(planner.plan(routes, standard));
        }
        StrategySelector selector = new StrategySelector(Map.of(
                DeliveryPriority.URGENT, fastest, DeliveryPriority.ECONOMY, cheapest,
                DeliveryPriority.ECO, eco, DeliveryPriority.STANDARD, balanced));
        System.out.println("AUTOMATIC SELECTION - dynamic requests");
        for (DeliveryRequest request : List.of(
                DemoData.request(DeliveryPriority.URGENT, 10000, 30, false),
                DemoData.request(DeliveryPriority.ECONOMY, 2000, 60, false),
                DemoData.request(DeliveryPriority.STANDARD, 10000, 60, true),
                DemoData.request(DeliveryPriority.STANDARD, 10000, 35, true))) {
            planner.setStrategy(selector.select(request));
            print(planner.plan(routes, request));
        }
        System.out.println("COMPARISON - same standard request");
        new StrategyComparison(strategies).compare(routes, standard).forEach(Main::print);
        System.out.println("FAILURE EXAMPLES");
        failure(() -> planner.plan(List.of(), standard));
        failure(() -> planner.plan(routes, DemoData.request(DeliveryPriority.STANDARD, 0, 1, false)));
        failure(() -> new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5));
    }
    private static void print(RouteDecision decision) {
        System.out.printf("%s -> Route %s%nReason: %s%n", decision.strategyName(), decision.route().id(), decision.explanation());
        decision.rejectedRoutes().forEach((id, reason) -> System.out.println("Rejected " + id + ": " + reason));
    }
    private static void failure(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException | NoSuitableRouteException error) {
            System.out.println(error.getClass().getSimpleName() + ": " + error.getMessage());
        }
    }
}
