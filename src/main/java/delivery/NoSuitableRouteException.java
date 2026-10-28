package delivery;

public final class NoSuitableRouteException extends RuntimeException {
    public NoSuitableRouteException(String message) { super(message); }
}
