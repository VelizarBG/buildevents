package velizarbg.buildevents.utils;

/**
 * A slightly limited implementation of {@link java.util.function.Function} which throws exceptions
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of the exception the function throws
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
	R apply(T t) throws E;
}
