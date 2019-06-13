package io.github.m

import kotlin.Char

/**
 * A parser for the M grammar.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Parser {
    data class Result(val rest: Sequence<Char>, val expr: Expr)

    const val newlines = "\n"
    const val whitespace = " \t\r$newlines"
    const val separators = "();\"$whitespace"

    tailrec fun parseComment(input: Sequence<Char>): Result =
            if (input.none() || input.car in newlines)
                parseExpr(input)
            else
                parseComment(input.cdr)

    tailrec fun parseSingleQuote(input: Sequence<Char>, acc: Sequence<Char>): Result = when (input.car) {
        '"' -> Result(input.cdr, Expr.Symbol(String(acc.reversed().toCharArray())))
        in newlines -> parseSingleQuote(input.cdr, input.car.cons(acc))
        else -> parseSingleQuote(input.cdr, input.car.cons(acc))
    }

    tailrec fun parseDoubleQuote(input: Sequence<Char>, acc: Sequence<Char>): Result = when (input.car) {
        '"' -> if (input.cdr.car == '"')
            Result(input.cdr.cdr, Expr.Symbol(String(acc.reversed().toCharArray())))
        else
            parseDoubleQuote(input.cdr, '"'.cons(acc))
        in newlines -> parseDoubleQuote(input.cdr, input.car.cons(acc))
        else -> parseDoubleQuote(input.cdr, input.car.cons(acc))
    }

    fun parseIdentifierLiteralExpr(input: Sequence<Char>, acc: Sequence<Char>): Result = when (input.car) {
        '"' -> parseDoubleQuote(input.cdr, acc)
        else -> parseSingleQuote(input, acc)
    }

    tailrec fun parseIdentifierExpr(input: Sequence<Char>, acc: Sequence<Char>): Result = when (input.car) {
        in separators -> Result(input, Expr.Symbol(String(acc.reversed().toCharArray())))
        else -> parseIdentifierExpr(input.cdr, input.car.cons(acc))
    }

    tailrec fun parseListExpr(input: Sequence<Char>, acc: Sequence<Expr>): Result = when (input.car) {
        ')' -> Result(input.cdr, Expr.List(acc.reversed()))
        else -> {
            val (rest, expr) = parseExpr(input)
            parseListExpr(rest, expr.cons(acc))
        }
    }

    fun parseExpr(input: Sequence<Char>): Result = when (input.car) {
        '(' -> parseListExpr(input.cdr, nil())
        '"' -> parseIdentifierLiteralExpr(input.cdr, nil())
        ';' -> parseComment(input.cdr)
        in newlines -> parseExpr(input.cdr)
        in whitespace -> parseExpr(input.cdr)
        else -> parseIdentifierExpr(input, nil())
    }

    tailrec fun parse(input: Sequence<Char>, acc: Sequence<Expr>): Sequence<Expr> =
            when {
                input.none() -> acc.reversed().asSequence()
                input.car in newlines -> parse(input.cdr, acc)
                input.car in whitespace -> parse(input.cdr, acc)
                else -> {
                    val (input1, expr1) = parseExpr(input)
                    parse(input1, expr1.cons(acc))
                }
            }

    fun parse(file: File): Sequence<Expr> = parse(file.read().asCons(), nil()).asSequence()
}