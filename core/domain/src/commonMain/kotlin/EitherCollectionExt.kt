package app.vercors.core.domain

import arrow.core.Either

fun <T> Iterable<Either<*, T>>.filterIsRight(): List<T> = filterIsInstance<Either.Right<T>>().map { it.value }

fun <E> Iterable<Either<E, *>>.filterIsLeft(): List<E> = filterIsInstance<Either.Left<E>>().map { it.value }
