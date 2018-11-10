package io.github.m

/**
 * Class representing an M environment.
 */
data class Env(val vars: Map<List, Variable>,
               val file: List,
               val def: List,
               val index: Int)