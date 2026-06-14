/*
 * Decompiled with CFR 0.152.
 */
package velizarbg.buildevents.utils;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    public R apply(T var1) throws E;
}

