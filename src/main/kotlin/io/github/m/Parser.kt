package io.github.m

import kotlin.Char

/**
 * A parser for the M grammar.
 */
@Suppress("MemberVisibilityCanBePrivate")
@ExperimentalUnsignedTypes
object Parser {
    data class Result(val rest: Sequence<Char>, val expr: Expr)

    const val newlines = "\r\n"
    const val whitespace = " \t\u000A\u000C$newlines"
    const val separators = "();\"$whitespace"
    val escapeMap = mapOf('b' to '\b', 't' to '\t', 'n' to '\n', 'r' to '\r', 'v' to '\u000A', 'f' to '\u000C')

    tailrec fun parseComment(input: Sequence<Char>, path: String, position: Position): Result = when (input.car) {
        in newlines -> parseExpr(input, path, position)
        else -> parseComment(input.cdr, path, position)
    }

    tailrec fun parseIdentifierLiteralExpr(input: Sequence<Char>, path: String, start: Position, end: Position, acc: Sequence<Char>): Result = when (input.car) {
        '"' -> Result(input.cdr, Expr.Identifier(String(acc.reversed().toCharArray()), path, start, end.nextChar()))
        '\\' -> {
            val char = input.cdr.car
            parseIdentifierLiteralExpr(input.cdr.cdr, path, start, end.nextChar().nextChar(), (escapeMap[char]
                    ?: char).cons(acc))
        }
        in newlines -> parseIdentifierLiteralExpr(input.cdr, path, start, end.nextLine(), input.car.cons(acc))
        else -> parseIdentifierLiteralExpr(input.cdr, path, start, end.nextChar(), input.car.cons(acc))
    }

    tailrec fun parseIdentifierExpr(input: Sequence<Char>, path: String, start: Position, end: Position, acc: Sequence<Char>): Result = when (input.car) {
        in separators -> Result(input, Expr.Identifier(String(acc.reversed().toCharArray()), path, start, end))
        else -> parseIdentifierExpr(input.cdr, path, start, end.nextChar(), input.car.cons(acc))
    }

    tailrec fun parseListExpr(input: Sequence<Char>, path: String, start: Position, end: Position, acc: Sequence<Expr>): Result = when (input.car) {
        ')' -> Result(input.cdr, Expr.List(acc.reversed(), path, start, end.nextChar()))
        else -> {
            val (rest, expr) = parseExpr(input, path, end)
            parseListExpr(rest, path, start, expr.end, expr.cons(acc))
        }
    }

    tailrec fun parseExpr(input: Sequence<Char>, path: String, position: Position): Result = when (input.car) {
        '(' -> parseListExpr(input.cdr, path, position, position, nil())
        '"' -> parseIdentifierLiteralExpr(input.cdr, path, position, position, nil())
        ';' -> parseComment(input.cdr, path, position)
        in newlines -> parseExpr(input.cdr, path, position.nextLine())
        in whitespace -> parseExpr(input.cdr, path, position.nextChar())
        else -> parseIdentifierExpr(input, path, position, position, nil())
    }

    tailrec fun parse(input: Sequence<Char>, path: String, position: Position, acc: Sequence<Expr>): Sequence<Expr> =
            if (input.none()) {
                acc.reversed().asSequence()
            } else {
                val (input1, expr1) = parseExpr(input, path, position)
                parse(input1, path, expr1.end, expr1.cons(acc))
            }

    fun parse(file: File, path: String, init: Boolean): Sequence<Expr> = when {
        file.isDirectory -> file.childFiles().asSequence().flatMap {
            parse(it, if (init) "" else "$path${file.name}.", false)
        }
        else -> parse(file.read().asCons(), "$path${file.nameWithoutExtension}", Position(1U, 1U), nil()).asSequence()
    }
}