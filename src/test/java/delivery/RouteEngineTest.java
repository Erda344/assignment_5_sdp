package delivery;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RouteEngineTest {
    private static final double EPS = 1e-9;
    private final DeliveryRequest request = DemoData.request(DeliveryPriority.STANDARD, 1000, 100, false);
    private Route route(String id, double km, int minutes, int traffic, int weather, double cost, double co2, boolean available) {
        return DemoData.route(id, km, minutes, traffic, weather, cost, 0, 0, co2, available);
    }
    private List<Route> candidates() {
        return List.of(route("A",10,20,0,0,500,5,true),
                route("B",20,40,0,0,100,1,true), route("C",5,30,0,0,300,2,true));
    }
    private String id(RouteStrategy strategy, List<Route> routes, DeliveryRequest req) {
        return strategy.selectRoute(routes, req).route().id();
    }
    private BalancedRouteStrategy balanced(double t, double c, double d, double e) {
        return new BalancedRouteStrategy(t,c,d,e);
    }
    private StrategySelector selector() {
        return new StrategySelector(Map.of(DeliveryPriority.URGENT, new FastestRouteStrategy(),
                DeliveryPriority.ECONOMY, new CheapestRouteStrategy(), DeliveryPriority.ECO, new EcoFriendlyRouteStrategy(),
                DeliveryPriority.STANDARD, balanced(.4,.3,.1,.2)));
    }
    @Test void fastestIncludesTrafficAndWeather() {
        List<Route> routes = List.of(route("A",10,10,15,5,100,1,true), route("B",10,25,0,0,100,1,true));
        assertEquals("B", id(new FastestRouteStrategy(), routes, request));
    }
    @Test void cheapestUsesDistanceFeesAndUrgentSurcharge() {
        Route a = DemoData.route("A",10,20,0,0,100,30,50,1,true);
        Route b = DemoData.route("B",10,20,0,0,200,10,0,1,true);
        DeliveryRequest urgent = DemoData.request(DeliveryPriority.URGENT,1000,100,false);
        RouteDecision result = new CheapestRouteStrategy().selectRoute(List.of(a,b), urgent);
        assertEquals("B", result.route().id());
        assertEquals(360, result.score(), EPS);
    }
    @Test void ecoIncludesDistanceAsWellAsEmissions() {
        Route a = route("A",100,20,0,0,100,.1,true);
        Route b = route("B",5,20,0,0,100,1,true);
        RouteDecision result = new EcoFriendlyRouteStrategy().selectRoute(List.of(a,b),request);
        assertEquals("B", result.route().id());
        assertEquals(1.8,result.score(),EPS);
    }
    @Test void balancedNormalizesDifferentUnits() {
        RouteDecision result = balanced(.4,.3,.1,.2).selectRoute(candidates(),request);
        assertEquals("C",result.route().id());
        assertEquals(.4,result.score(),EPS);
    }
    @Test void balancedWeightsCanChangeWinner() {
        assertEquals("A",id(balanced(1,0,0,0),candidates(),request));
        assertEquals("B",id(balanced(0,1,0,0),candidates(),request));
    }
    @Test void identicalMetricsAvoidDivisionByZeroAndTieById() {
        Route a=route("A",10,20,0,0,100,1,true), b=route("B",10,20,0,0,100,1,true);
        RouteDecision result=balanced(.4,.3,.1,.2).selectRoute(List.of(b,a),request);
        assertEquals("A",result.route().id());
        assertEquals(0,result.score(),EPS);
    }
    @Test void samePlannerSupportsThreeRuntimeSwitches() {
        RoutePlanner planner=new RoutePlanner(new FastestRouteStrategy());
        assertEquals("A",planner.plan(candidates(),request).route().id());
        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("B",planner.plan(candidates(),request).route().id());
        planner.setStrategy(new EcoFriendlyRouteStrategy());
        assertEquals("C",planner.plan(candidates(),request).route().id());
        planner.setStrategy(balanced(.4,.3,.1,.2));
        assertEquals("C",planner.plan(candidates(),request).route().id());
    }
    @Test void selectorMapsEveryPriority() {
        StrategySelector selector=selector();
        assertInstanceOf(FastestRouteStrategy.class,selector.select(DemoData.request(DeliveryPriority.URGENT,1000,100,false)));
        assertInstanceOf(CheapestRouteStrategy.class,selector.select(DemoData.request(DeliveryPriority.ECONOMY,1000,100,false)));
        assertInstanceOf(EcoFriendlyRouteStrategy.class,selector.select(DemoData.request(DeliveryPriority.ECO,1000,100,false)));
        assertInstanceOf(BalancedRouteStrategy.class,selector.select(request));
    }
    @Test void ecoPreferenceOverridesStandardButNotUrgency() {
        assertInstanceOf(EcoFriendlyRouteStrategy.class,selector().select(DemoData.request(DeliveryPriority.STANDARD,1000,100,true)));
        assertInstanceOf(FastestRouteStrategy.class,selector().select(DemoData.request(DeliveryPriority.URGENT,1000,100,true)));
    }
    @Test void selectorAcceptsCustomMapping() {
        RouteStrategy custom=new CheapestRouteStrategy();
        StrategySelector selector=new StrategySelector(Map.of(DeliveryPriority.URGENT,custom,
                DeliveryPriority.ECONOMY,custom,DeliveryPriority.ECO,custom,DeliveryPriority.STANDARD,custom));
        assertSame(custom,selector.select(request));
    }
    @Test void unavailableRouteCannotWinAnyAlgorithm() {
        Route unavailable=route("X",1,1,0,0,0,0,false);
        Route valid=candidates().get(0);
        for(RouteStrategy strategy: List.of(new FastestRouteStrategy(),new CheapestRouteStrategy(),new EcoFriendlyRouteStrategy(),balanced(.4,.3,.1,.2))) {
            RouteDecision result=strategy.selectRoute(List.of(unavailable,valid),request);
            assertEquals("A",result.route().id());
            assertTrue(result.rejectedRoutes().get("X").contains("unavailable"));
        }
    }
    @Test void budgetExcludesFastestRoute() {
        DeliveryRequest limited=DemoData.request(DeliveryPriority.STANDARD,300,100,false);
        assertEquals("C",id(new FastestRouteStrategy(),candidates(),limited));
    }
    @Test void deadlineIncludesPenalties() {
        Route a=route("A",10,10,20,5,100,1,true), b=route("B",10,25,0,0,200,1,true);
        RouteDecision result=new CheapestRouteStrategy().selectRoute(List.of(a,b),DemoData.request(DeliveryPriority.STANDARD,1000,30,false));
        assertEquals("B",result.route().id());
        assertTrue(result.rejectedRoutes().get("A").contains("deadline"));
    }
    @Test void emissionLimitIsHardConstraint() {
        DeliveryRequest limited=new DeliveryRequest(DemoData.ORIGIN,DemoData.DESTINATION,DeliveryPriority.STANDARD,1000,100,false,1);
        assertEquals("B",id(new FastestRouteStrategy(),candidates(),limited));
    }
    @Test void differentEndpointsAreRejected() {
        Route wrong=new Route("X",new Location("Other",0,0),DemoData.DESTINATION,1,1,0,0,0,0,0,0,true);
        assertEquals("A",id(new FastestRouteStrategy(),List.of(wrong,candidates().get(0)),request));
    }
    @Test void noValidRouteHasExplicitReason() {
        NoSuitableRouteException ex=assertThrows(NoSuitableRouteException.class,()->new FastestRouteStrategy()
                .selectRoute(candidates(),DemoData.request(DeliveryPriority.STANDARD,0,1,false)));
        assertTrue(ex.getMessage().contains("Budget exceeded"));
        assertTrue(ex.getMessage().contains("deadline"));
    }
    @Test void emptyListFailsExplicitly() {
        assertThrows(NoSuitableRouteException.class,()->new FastestRouteStrategy().selectRoute(List.of(),request));
    }
    @Test void invalidBalancedWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->balanced(.5,.5,.5,.5));
        assertThrows(IllegalArgumentException.class,()->balanced(-.1,.5,.3,.3));
        assertThrows(IllegalArgumentException.class,()->balanced(Double.NaN,0,0,1));
        assertThrows(IllegalArgumentException.class,()->balanced(Double.POSITIVE_INFINITY,0,0,0));
    }
    @Test void comparisonRunsSameRequestThroughDifferentAlgorithms() {
        List<RouteDecision> results=new StrategyComparison(List.of(new FastestRouteStrategy(),new CheapestRouteStrategy())).compare(candidates(),request);
        assertEquals(List.of("A","B"),results.stream().map(d->d.route().id()).toList());
        assertEquals(List.of("Fastest","Cheapest"),results.stream().map(RouteDecision::strategyName).toList());
    }
    @Test void constraintsAcceptExactBoundary() {
        Route route=route("A",1,10,2,3,100,1,true);
        DeliveryRequest exact=new DeliveryRequest(DemoData.ORIGIN,DemoData.DESTINATION,DeliveryPriority.STANDARD,100,15,false,1);
        assertEquals("A",id(new FastestRouteStrategy(),List.of(route),exact));
    }
    @Test void invalidRouteDataIsRejected() {
        assertThrows(IllegalArgumentException.class,()->route("A",-1,10,0,0,100,1,true));
        assertThrows(IllegalArgumentException.class,()->route("A",1,10,0,0,-1,1,true));
        assertThrows(IllegalArgumentException.class,()->route("A",1,10,0,0,Double.NaN,1,true));
    }
    @Test void missingDataFailsEarly() {
        assertThrows(NullPointerException.class,()->new RoutePlanner(null));
        assertThrows(NullPointerException.class,()->new RoutePlanner(new FastestRouteStrategy()).setStrategy(null));
        assertThrows(NullPointerException.class,()->new FastestRouteStrategy().selectRoute(candidates(),null));
        assertThrows(NullPointerException.class,()->new DeliveryRequest(null,DemoData.DESTINATION,DeliveryPriority.STANDARD,10,10,false,1));
    }
    @Test void sameStrategyChangesResultWithRequestBudget() {
        RouteStrategy fastest=new FastestRouteStrategy();
        assertEquals("A",id(fastest,candidates(),request));
        assertEquals("C",id(fastest,candidates(),DemoData.request(DeliveryPriority.STANDARD,300,100,false)));
        assertEquals("B",id(fastest,candidates(),DemoData.request(DeliveryPriority.STANDARD,100,100,false)));
    }
    @Test void explanationContainsPenaltiesFormulaAndRejections() {
        Route a=route("A",1,20,3,2,100,1,true), x=route("X",1,1,0,0,100,1,false);
        RouteDecision result=new FastestRouteStrategy().selectRoute(List.of(a,x),request);
        assertTrue(result.explanation().contains("traffic 3 + weather 2 = 25.0"));
        assertTrue(result.explanation().contains("time ="));
        assertTrue(result.rejectedRoutes().containsKey("X"));
        assertThrows(UnsupportedOperationException.class,()->result.rejectedRoutes().clear());
    }
    @Test void plannerAcceptsIndependentCustomStrategy() {
        RouteStrategy custom=new RouteStrategy() {
            public String getStrategyName(){return "Custom";}
            public RouteDecision selectRoute(List<Route> routes,DeliveryRequest req) {
                return new RouteDecision(routes.get(1),getStrategyName(),0,"Chosen by custom algorithm",Map.of());
            }
        };
        assertEquals("B",new RoutePlanner(custom).plan(candidates(),request).route().id());
    }
    @Test void duplicateIdsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new FastestRouteStrategy().selectRoute(List.of(candidates().get(0),candidates().get(0)),request));
    }
    @Test void invalidRequestAndCoordinatesAreRejected() {
        assertThrows(IllegalArgumentException.class,()->DemoData.request(DeliveryPriority.STANDARD,-1,100,false));
        assertThrows(IllegalArgumentException.class,()->DemoData.request(DeliveryPriority.STANDARD,100,0,false));
        assertThrows(IllegalArgumentException.class,()->new Location("X",91,0));
    }
    @Test void inputRoutesAreNotReorderedOrModified() {
        List<Route> routes=new ArrayList<>(candidates());
        List<Route> before=List.copyOf(routes);
        new CheapestRouteStrategy().selectRoute(routes,request);
        assertEquals(before,routes);
    }
}
