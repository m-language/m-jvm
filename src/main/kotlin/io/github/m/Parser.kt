package io.github.m

import kotlin.Char

/**
 * A parser for the M grammar.
 */
@Suppress("MemberVisibilityCanBePrivate")
@ExperimentalUnsignedTypes
object Parser {
    data class Result(val rest: Seq<Char>, val expr: Expr)

    const val newlines = "\r\n"
    const val whitespace = " \t\u000A\u000C$newlines"
    const val separators = "();\"$whitespace"
    val escapeMap = mapOf('b' to '\b', 't' to '\t', 'n' to '\n', 'r' to '\r', 'v' to '\u000A', 'f' to '\u000C')

    tailrec fun parseComment(input: Seq.Cons<Char>, position: Position): Result = when (input.car) {
        in newlines -> parseExpr(input, position)
        else -> parseComment(input.cdr as Seq.Cons, position)
    }

    tailrec fun parseIdentifierLiteralExpr(input: Seq.Cons<Char>, start: Position, end: Position, acc: Seq<Char>): Result = when (input.car) {
        '"' -> Result(input.cdr, Expr.Identifier(String(acc.reversed().toCharArray()), start, end.nextChar()))
        '\\' -> {
            val char = (input.cdr as Seq.Cons).car
            parseIdentifierLiteralExpr(input.cdr.cdr as Seq.Cons, start, end.nextChar().nextChar(), Seq.Cons((escapeMap[char] ?: char), acc))
        }
        in newlines -> parseIdentifierLiteralExpr(input.cdr as Seq.Cons, start, end.nextLine(), Seq.Cons(input.car, acc))
        else -> parseIdentifierLiteralExpr(input.cdr as Seq.Cons, start, end.nextChar(), Seq.Cons(input.car, acc))
    }

    tailrec fun parseIdentifierExpr(input: Seq.Cons<Char>, start: Position, end: Position, acc: Seq<Char>): Result = when (input.car) {
        in separators -> Result(input, Expr.Identifier(String(acc.reversed().toCharArray()), start, end))
        else -> parseIdentifierExpr(input.cdr as Seq.Cons, start, end.nextChar(), Seq.Cons(input.car, acc))
    }

    tailrec fun parseListExpr(input: Seq.Cons<Char>, start: Position, end: Position, acc: Seq<Expr>): Result = when (input.car) {
        ')' -> Result(input.cdr, Expr.List(acc.reversed(), start, end.nextChar()))
        else -> {
            val (rest, expr) = parseExpr(input, end)
            parseListExpr(rest as Seq.Cons, start, expr.end, Seq.Cons(expr, acc))
        }
    }

    tailrec fun parseExpr(input: Seq.Cons<Char>, position: Position): Result = when (input.car) {
        '(' -> parseListExpr(input.cdr as Seq.Cons, position, position, Seq.Nil)
        '"' -> parseIdentifierLiteralExpr(input.cdr as Seq.Cons, position, position, Seq.Nil)
        ';' -> parseComment(input.cdr as Seq.Cons, position)
        in newlines -> parseExpr(input.cdr as Seq.Cons, position.nextLine())
        in whitespace -> parseExpr(input.cdr as Seq.Cons, position.nextChar())
        else -> parseIdentifierExpr(input, position, position, Seq.Nil)
    }

    tailrec fun parse(input: Seq<Char>, position: Position, acc: Seq<Expr>): Seq<Expr> = when (input) {
        Seq.Nil -> Seq.valueOf(acc.reversed().asSequence())
        is Seq.Cons -> {
            val (input1, expr1) = parseExpr(input, position)
            parse(input1, expr1.end, Seq.Cons(expr1, acc))
        }
    }

    fun parse(input: Seq<Char>): Seq<Expr> = parse(
            input.toList().foldRight(Seq.Nil as Seq<Char>) { car, cdr -> Seq.Cons(car, cdr) },
            Position(1U, 1U),
            Seq.Nil
    )
}