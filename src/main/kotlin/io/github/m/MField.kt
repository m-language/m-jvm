package io.github.m

/**
 * Annotation which marks a field to be accessible from M code.
 *
 * @param name The name of the field in M code.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class MField(val name: String)