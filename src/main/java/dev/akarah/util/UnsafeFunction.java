package dev.akarah.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Documents that the given function is unsafe, and should not be used without careful consideration.
 * These functions, atleast in this library, usually allow you to bypass Java's generic type system.
 * However, this can lead to unexpected crashes if used incorrectly.
 */
@Target(ElementType.METHOD)
public @interface UnsafeFunction {
}
