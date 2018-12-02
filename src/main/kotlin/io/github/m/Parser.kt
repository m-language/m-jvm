package io.github.m

/**
 * A parser for the M grammar.
 */
@Suppress("MemberVisibilityCanBePrivate")
@ExperimentalUnsignedTypes
object Parser {
    data class Result(val rest: List, val expr: Expr, val line: UInt)

    const val newlines = "\r\n"
    const val whitespace = " \t\u000A\u000C$newlines"
    const val separators = "();\"$whitespace"
    val escapeMap = mapOf('b' to '\b', 't' to '\t', 'n' to '\n', 'r' to '\r', 'v' to '\u000A', 'f' to '\u000C')

    tailrec fun parseComment(input: List.Cons, line: UInt): Result = when ((input.car as Char).value) {
        in newlines -> parseExpr(input, line)
        else -> parseComment(input.cdr as List.Cons, line)
    }

    tailrec fun parseIdentifierLiteralExpr(input: List.Cons, line: UInt, acc: List): Result = when ((input.car as Char).value) {
        '"' -> Result(input.cdr, Expr.Identifier(acc.reverse(), Nat(line)), line)
        '\\' -> {
            val char = (input.cadr as Char).value
            parseIdentifierLiteralExpr(input.cddr as List.Cons, line, List.Cons(Char(escapeMap[char] ?: char), acc))
        }
        else -> parseIdentifierLiteralExpr(input.cdr as List.Cons, line, List.Cons(input.car, acc))
    }

    tailrec fun parseIdentifierExpr(input: List.Cons, line: UInt, acc: List): Result = when ((input.car as Char).value) {
        in separators -> Result(input, Expr.Identifier(acc.reverse(), Nat(line)), line)
        else -> parseIdentifierExpr(input.cdr as List.Cons, line, List.Cons(input.car, acc))
    }

    tailrec fun parseListExpr(input: List.Cons, line: UInt, acc: List): Result = when ((input.car as Char).value) {
        ')' -> Result(input.cdr, Expr.List(acc.reverse(), Nat(line)), line)
        else -> {
            val (rest, expr, line1) = parseExpr(input, line)
            parseListExpr(rest as List.Cons, line1, List.Cons(expr, acc))
        }
    }

    tailrec fun parseExpr(input: List.Cons, line: UInt): Result = when ((input.car as Char).value) {
        '(' -> parseListExpr(input.cdr as List.Cons, line, List.Nil)
        '"' -> parseIdentifierLiteralExpr(input.cdr as List.Cons, line, List.Nil)
        ';' -> parseComment(input.cdr as List.Cons, line)
        in newlines -> parseExpr(input.cdr as List.Cons, line + 1.toUInt())
        in whitespace -> parseExpr(input.cdr as List.Cons, line)
        else -> parseIdentifierExpr(input, line, List.Nil)
    }

    tailrec fun parse(input: List, line: UInt, acc: List): List = when (input) {
        List.Nil -> acc.reverse()
        is List.Cons -> {
            val (input1, expr1, line1) = parseExpr(input, line)
            parse(input1, line1, List.Cons(expr1, acc))
        }
    }
}