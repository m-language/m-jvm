package io.github.m

import io.github.m.Expr.Identifier
import io.github.m.Expr.List

/**
 * A parser for the M1 grammar.
 */
@Suppress("MemberVisibilityCanBePrivate")
@SelfHosted
object Parser {
    interface Parser<out R, S, T> {
        operator fun invoke(input: Sequence<T>, state: S): Result<R, S, T>

        companion object {
            inline operator fun <R, S, T> invoke(crossinline fn: (Sequence<T>, S) -> Result<R, S, T>) = object : Parser<R, S, T> {
                override fun invoke(input: Sequence<T>, state: S) = fn(input, state)
            }
        }
    }

    @Suppress("unused")
    sealed class Result<out R, out S, out T> {
        data class Failure<out S>(val state: S) : Result<Nothing, S, Nothing>()
        data class Success<out R, out S, out T>(val result: R, val state: S, val rest: Sequence<T>) : Result<R, S, T>()
    }

    fun <S, T> predicateParser(predicate: (T) -> Boolean) = Parser<T, S, T> { input, state ->
        if (input.any() && predicate(input.first()))
            Result.Success(input.first(), state, input.drop(1))
        else
            Result.Failure(state)
    }

    fun <A, B, S, T> mapParser(parser: Parser<A, S, T>, fn: (Result<A, S, T>) -> Result<B, S, T>) = Parser<B, S, T> { input, state ->
        fn(parser(input, state))
    }

    fun <A, B, S, T> mapParserSuccess(parser: Parser<A, S, T>, fn: (Result.Success<A, S, T>) -> Result.Success<B, S, T>): Parser<B, S, T> =
            mapParser(parser) { if (it is Result.Success) fn(it) else it as Result.Failure<S> }

    fun <A, B, S, T> mapParserResult(parser: Parser<A, S, T>, fn: (A) -> B) =
            mapParserSuccess(parser) { Result.Success(fn(it.result), it.state, it.rest) }

    fun <R, S, T> mapParserState(parser: Parser<R, S, T>, fn: (R, S) -> S) =
            mapParserSuccess(parser) { it.copy(state = fn(it.result, it.state)) }

    fun <R, S, T> injectPastState(parser: Parser<R, S, T>): Parser<Pair<R, S>, S, T> = Parser { input, state ->
        mapParserResult(parser) { it to state }.invoke(input, state)
    }

    fun <A, B, S, T> combineParser(parser1: Parser<A, S, T>, parser2: Parser<B, S, T>) = Parser<Pair<A, B>, S, T> { input, state ->
        val result1 = parser1(input, state)
        when (result1) {
            is Result.Success -> {
                val result2 = parser2(result1.rest, result1.state)
                when (result2) {
                    is Result.Success -> Result.Success(result1.result to result2.result, result2.state, result2.rest)
                    is Result.Failure -> result2
                }
            }
            is Result.Failure -> result1
        }
    }

    fun <R, S, T> combineParserLeft(parser1: Parser<R, S, T>, parser2: Parser<*, S, T>) =
            mapParserResult(combineParser(parser1, parser2), Pair<R, *>::first)

    fun <R, S, T> combineParserRight(parser1: Parser<*, S, T>, parser2: Parser<R, S, T>) =
            mapParserResult(combineParser(parser1, parser2), Pair<*, R>::second)

    fun <R, S, T> repeatParser(parser: Parser<R, S, T>): Parser<Sequence<R>, S, T> = Parser { input, state ->
        val result = parser(input, state)
        when (result) {
            is Result.Success -> {
                val restResult = repeatParser(parser)(result.rest, result.state) as Result.Success<Sequence<R>, S, T>
                Result.Success(sequenceOf(result.result) + restResult.result, restResult.state, restResult.rest)
            }
            is Result.Failure -> Result.Success(emptySequence(), state, input)
        }
    }

    fun <R, S, T> repeatParser1(parser: Parser<R, S, T>): Parser<Sequence<R>, S, T> =
            mapParserResult(combineParser(parser, repeatParser(parser))) { (head, tail) -> sequenceOf(head) + tail }

    fun <R, S, T> alternativeParser(parser1: Parser<R, S, T>, parser2: Parser<R, S, T>) = Parser<R, S, T> { input, state ->
        val result1 = parser1(input, state)
        when (result1) {
            is Result.Success -> result1
            is Result.Failure -> parser2(input, state)
        }
    }

    fun <R, S, T> lazyParser(parser: () -> Parser<R, S, T>) = Parser<R, S, T> { input, state ->
        parser()(input, state)
    }

    fun isWhitespace(c: Char) = isNewline(c) || c in " \t"
    fun isNewline(c: Char) = c in "\r\n"

    val newlineParser: Parser<Char, Int, Char> = mapParserState(predicateParser(io.github.m.Parser::isNewline)) { _, position -> position + 1 }
    val whitespaceParser: Parser<Char, Int, Char> = alternativeParser(newlineParser, predicateParser { isWhitespace(it) })
    
    fun <R> ignoreUnused(parser: Parser<R, Int, Char>) =
            combineParserRight(repeatParser(whitespaceParser), parser)

    fun isIdentifierCharacter(c: Char) = !(isWhitespace(c) || c == '(' || c == ')')

    val identifierCharParser: Parser<Char, Int, Char> = predicateParser(io.github.m.Parser::isIdentifierCharacter)
    val identifierParser: Parser<Identifier, Int, Char> = ignoreUnused(mapParserResult(injectPastState(repeatParser1(identifierCharParser))) { (e, p) -> Identifier(e, p) })

    val openParenParser: Parser<Char, Int, Char> = ignoreUnused(predicateParser { it == '(' })
    val closeParenParser: Parser<Char, Int, Char> = ignoreUnused(predicateParser { it == ')' })

    val listExprParser1: Parser<Sequence<Expr>, Int, Char> = combineParserRight(openParenParser, combineParserLeft(lazyParser { parser }, closeParenParser))
    val listExprParser: Parser<List, Int, Char> = ignoreUnused(mapParserResult(injectPastState(listExprParser1)) { (e, p) -> List(e.toList(), p) })
    val exprParser: Parser<Expr, Int, Char> = alternativeParser(identifierParser, listExprParser)

    val parser: Parser<Sequence<Expr>, Int, Char> = repeatParser(exprParser)
}