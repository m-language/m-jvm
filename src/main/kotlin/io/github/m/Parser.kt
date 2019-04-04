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

    tailrec fun parseComment(input: Sequence<Char>, path: String, position: Position): Result =
            if (input.none() || input.car in newlines)
                parseExpr(input, path, position)
            else
                parseComment(input.cdr, path, position)

    tailrec fun parseSingleQuote(input: Sequence<Char>, path: String, start: Position, end: Position, acc: Sequence<Char>): Result = when (input.car) {
        '"' -> Result(input.cdr, Expr.Symbol(String(acc.reversed().toCharArray()), path, start, end.nextChar()))
        in newlines -> parseSingleQuote(input.cdr, path, start, end.nextLine(), input.car.cons(acc))
        else -> parseSingleQuote(input.cdr, path, start, end.nextChar(), input.car.cons(acc))
    }

    tailrec fun parseDoubleQuote(input: Sequence<Char>, path: String, start: Position, end: Position, acc: Sequence<Char>): Result = when (input.car) {
        '"' -> if (input.cdr.car == '"')
            Result(input.cdr.cdr, Expr.Symbol(String(acc.reversed().toCharArray()), path, start, end.nextChar().nextChar()))
        else
            parseDoubleQuote(input.cdr, path, start, end.nextChar(), '"'.cons(acc))
        in newlines -> parseDoubleQuote(input.cdr, path, start, end.nextLine(), input.car.cons(acc))
        else -> parseDoubleQuote(input.cdr, path, start, end.nextChar(), input.car.cons(acc))
    }

    fun parseIdentifierLiteralExpr(input: Sequence<Char>, path: String, start: Position, end: Position, acc: Sequence<Char>): Result = when (input.car) {
        '"' -> parseDoubleQuote(input.cdr, path, start, end.nextChar(), acc)
        else -> parseSingleQuote(input, path, start, end, acc)
    }

    tailrec fun parseIdentifierExpr(input: Sequence<Char>, path: String, start: Position, end: Position, acc: Sequence<Char>): Result = when (input.car) {
        in separators -> Result(input, Expr.Symbol(String(acc.reversed().toCharArray()), path, start, end))
        else -> parseIdentifierExpr(input.cdr, path, start, end.nextChar(), input.car.cons(acc))
    }

    tailrec fun parseListExpr(input: Sequence<Char>, path: String, start: Position, end: Position, acc: Sequence<Expr>): Result = when (input.car) {
        ')' -> Result(input.cdr, Expr.List(acc.reversed(), path, start, end.nextChar()))
        else -> {
            val (rest, expr) = parseExpr(input, path, end)
            parseListExpr(rest, path, start, expr.end, expr.cons(acc))
        }
    }

    fun parseExpr(input: Sequence<Char>, path: String, position: Position): Result = when (input.car) {
        '(' -> parseListExpr(input.cdr, path, position, position, nil())
        '"' -> parseIdentifierLiteralExpr(input.cdr, path, position, position, nil())
        ';' -> parseComment(input.cdr, path, position)
        in newlines -> parseExpr(input.cdr, path, position.nextLine())
        in whitespace -> parseExpr(input.cdr, path, position.nextChar())
        else -> parseIdentifierExpr(input, path, position, position, nil())
    }

    tailrec fun parse(input: Sequence<Char>, path: String, position: Position, acc: Sequence<Expr>): Sequence<Expr> =
            when {
                input.none() -> acc.reversed().asSequence()
                input.car in newlines -> parse(input.cdr, path, position.nextLine(), acc)
                input.car in whitespace -> parse(input.cdr, path, position.nextChar(), acc)
                else -> {
                    val (input1, expr1) = parseExpr(input, path, position)
                    parse(input1, path, expr1.end, expr1.cons(acc))
                }
            }

    @UseExperimental(ExperimentalUnsignedTypes::class)
    fun parse(file: File, path: String, init: Boolean): Sequence<Expr> = when {
        file.isDirectory -> file.childFiles().asSequence().flatMap {
            parse(it, if (init) "" else "$path${file.name}/", false)
        }
        else -> parse(file.read().asCons(), "$path${file.nameWithoutExtension}", Position(1U, 1U), nil()).asSequence()
    }
}