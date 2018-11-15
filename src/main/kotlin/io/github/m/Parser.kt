package io.github.m

import io.github.m.Expr.Identifier
import io.github.m.Expr.List
import io.github.m.Char as MChar
import io.github.m.Int as MInt
import io.github.m.Pair as MPair

/**
 * A parser for the M grammar.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
@Suppress("MemberVisibilityCanBePrivate")
object Parser {
    interface Parser<out R, S, T> {
        operator fun invoke(input: Sequence<T>, state: S): Result<R, S, T>

        companion object {
            inline operator fun <R, S, T> invoke(crossinline fn: (Sequence<T>, S) -> Result<R, S, T>) =
                    object : Parser<R, S, T> {
                        override fun invoke(input: Sequence<T>, state: S) = fn(input, state)
                    }
        }
    }

    @Suppress("unused")
    sealed class Result<out R, out S, out T> {
        data class Failure<out S>(val state: S) : Result<Nothing, S, Nothing>()
        data class Success<out R, out S, out T>(val value: R, val state: S, val rest: Sequence<T>) : Result<R, S, T>()
    }

    fun <S, T> predicateParser(predicate: (T) -> Boolean) = Parser<T, S, T> { input, state ->
        if (input.any() && predicate(input.first()))
            Result.Success(input.first(), state, input.drop(1))
        else
            Result.Failure(state)
    }

    fun <A, B, S, T> mapParser(parser: Parser<A, S, T>, fn: (Result<A, S, T>) -> Result<B, S, T>) =
            Parser<B, S, T> { input, state ->
                fn(parser(input, state))
            }

    fun <A, B, S, T> mapParserSuccess(parser: Parser<A, S, T>, fn: (Result.Success<A, S, T>) -> Result.Success<B, S, T>) =
            mapParser(parser) { if (it is Result.Success) fn(it) else it as Result.Failure<S> }

    fun <A, B, S, T> mapParserValue(parser: Parser<A, S, T>, fn: (A) -> B) =
            mapParserSuccess(parser) { Result.Success(fn(it.value), it.state, it.rest) }

    fun <R, S, T> mapParserState(parser: Parser<R, S, T>, fn: (R, S) -> S) =
            mapParserSuccess(parser) { it.copy(state = fn(it.value, it.state)) }

    fun <R, S, T> providePastState(parser: Parser<R, S, T>): Parser<Pair<R, S>, S, T> =
            Parser { input, state ->
                mapParserValue(parser) { it to state }.invoke(input, state)
            }

    fun <A, B, S, T> combineParser(parser1: Parser<A, S, T>, parser2: Parser<B, S, T>) =
            Parser<Pair<A, B>, S, T> { input, state ->
                val result1 = parser1(input, state)
                when (result1) {
                    is Result.Success -> {
                        val result2 = parser2(result1.rest, result1.state)
                        when (result2) {
                            is Result.Success -> Result.Success(
                                    result1.value to result2.value,
                                    result2.state,
                                    result2.rest
                            )
                            is Result.Failure -> result2
                        }
                    }
                    is Result.Failure -> result1
                }
            }

    fun <R, S, T> combineParserLeft(parser1: Parser<R, S, T>, parser2: Parser<*, S, T>) =
            mapParserValue(combineParser(parser1, parser2), Pair<R, *>::first)

    fun <R, S, T> combineParserRight(parser1: Parser<*, S, T>, parser2: Parser<R, S, T>) =
            mapParserValue(combineParser(parser1, parser2), Pair<*, R>::second)

    fun <R, S, T> repeatParser(parser: Parser<R, S, T>): Parser<Sequence<R>, S, T> =
            Parser { input, state ->
                val result = parser(input, state)
                when (result) {
                    is Result.Success -> {
                        val restResult = repeatParser(parser)(result.rest, result.state)
                        restResult as Result.Success<Sequence<R>, S, T>
                        Result.Success(
                                sequenceOf(result.value) + restResult.value,
                                restResult.state,
                                restResult.rest
                        )
                    }
                    is Result.Failure -> Result.Success(emptySequence(), state, input)
                }
            }

    fun <R, S, T> repeatParser1(parser: Parser<R, S, T>) =
            mapParserValue(combineParser(parser, repeatParser(parser))) { (head, tail) ->
                sequenceOf(head) + tail
            }

    fun <R, S, T> alternativeParser(parser1: Parser<R, S, T>, parser2: Parser<R, S, T>) =
            Parser<R, S, T> { input, state ->
                val result1 = parser1(input, state)
                when (result1) {
                    is Result.Success -> result1
                    is Result.Failure -> parser2(input, state)
                }
            }

    fun <R, S, T> lazyParser(parser: () -> Parser<R, S, T>) =
            Parser<R, S, T> { input, state ->
                parser()(input, state)
            }

    fun <S> charParser(c: Char) = predicateParser<S, Char> { it == c }
    fun <S, T> successParser() = predicateParser<S, T> { true }

    fun isWhitespace(c: Char) = isNewline(c) || c in " \t"
    fun isNewline(c: Char) = c in "\r\n"

    fun escapeMap(c: Char) = when (c) {
        'b' -> '\b'
        't' -> '\t'
        'n' -> '\n'
        'r' -> '\r'
        'v' -> '\u000A'
        'f' -> '\u000C'
        else -> c
    }

    val newlineParser: Parser<Char, UInt, Char> = mapParserState(predicateParser(::isNewline)) { _, position -> position + 1.toUInt() }
    val whitespaceParser: Parser<Char, UInt, Char> = alternativeParser(newlineParser, predicateParser(::isWhitespace))
    val commentParser: Parser<Char, UInt, Char> = combineParserLeft(charParser(';'), repeatParser(predicateParser { !isNewline(it) }))

    fun <R> ignoreUnused(parser: Parser<R, UInt, Char>) = combineParserRight(repeatParser(alternativeParser(whitespaceParser, commentParser)), parser)

    fun isIdentifierCharacter(c: Char) = !(isWhitespace(c) || c == '(' || c == ')' || c == ';' || c == '"')

    val identifierLiteralCharParser: Parser<Char, UInt, Char> = predicateParser { it != '"' }
    val identifierLiteralEscapeParser: Parser<Char, UInt, Char> = combineParserRight(charParser('\\'), mapParserValue(successParser(), ::escapeMap))
    val identifierLiteralParser: Parser<Sequence<Char>, UInt, Char> = combineParserRight(charParser('"'), combineParserLeft(repeatParser(alternativeParser(identifierLiteralEscapeParser, identifierLiteralCharParser)), charParser('"')))
    val identifierCharParser: Parser<Char, UInt, Char> = predicateParser(::isIdentifierCharacter)
    private val identifierParser1 = providePastState(alternativeParser(identifierLiteralParser, repeatParser1(identifierCharParser)))
    val identifierParser: Parser<Expr, UInt, Char> = mapParserValue(identifierParser1) { (e, p) -> Identifier(e, p) }
    private val listExprParser1: Parser<Sequence<Expr>, UInt, Char> = combineParserRight(charParser('('), combineParserLeft(lazyParser { parser }, ignoreUnused(charParser(')'))))
    val listExprParser: Parser<Expr, UInt, Char> = mapParserValue(providePastState(listExprParser1)) { (e, p) -> List(e, p) }
    val exprParser: Parser<Expr, UInt, Char> = ignoreUnused(alternativeParser(identifierParser, listExprParser))
    val parser: Parser<Sequence<Expr>, UInt, Char> = repeatParser(exprParser)
}