package delivery;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DistanceFirstRouteStrategyTest {
    private final DeliveryRequest request = DemoData.request(DeliveryPriority.STANDARD, 10000, 60, false);

    @Test void fifthStrategySelectsShortestAvailableRoute() {
        RoutePlanner planner = new RoutePlanner(new DistanceFirstRouteStrategy());
        RouteDecision result = planner.plan(DemoData.routes(), request);
        assertEquals("E", result.route().id());
        assertEquals(6, result.score(), 1e-9);
        assertTrue(result.explanation().contains("distance score"));
    }
    @Test void fifthStrategyRespectsBudgetAndDeadline() {
        RouteStrategy strategy = new DistanceFirstRouteStrategy();
        RouteDecision cheap = strategy.selectRoute(DemoData.routes(),
                DemoData.request(DeliveryPriority.STANDARD, 2000, 60, false));
        assertEquals("C", cheap.route().id());
        RouteDecision urgent = strategy.selectRoute(DemoData.routes(),
                DemoData.request(DeliveryPriority.STANDARD, 10000, 30, false));
        assertEquals("B", urgent.route().id());
    }
    @Test void fifthStrategyJoinsComparisonWithoutContextChanges() {
        List<RouteDecision> results = new StrategyComparison(List.of(
                new FastestRouteStrategy(), new DistanceFirstRouteStrategy())).compare(DemoData.routes(), request);
        assertEquals(List.of("B", "E"), results.stream().map(d -> d.route().id()).toList());
    }
}
