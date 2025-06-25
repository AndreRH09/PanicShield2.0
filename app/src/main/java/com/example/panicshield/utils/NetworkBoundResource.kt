package com.example.panicshield.utils

import kotlinx.coroutines.flow.*

fun <ResultType, RequestType> networkBoundResource(
    query: () -> Flow<ResultType>,
    fetch: suspend () -> RequestType,
    saveFetchResult: suspend (RequestType) -> Unit,
    shouldFetch: (ResultType) -> Boolean = { true }
): Flow<Resource<ResultType>> = flow {

    val initialData = query().first()
    emit(Resource.Loading(initialData))

    if (shouldFetch(initialData)) {
        try {
            val networkResult = fetch()
            saveFetchResult(networkResult)
            query().collect { emit(Resource.Success(it)) }
        } catch (throwable: Throwable) {
            emit(Resource.Error(
                "Error de red: ${throwable.message ?: "Error desconocido"}",
                initialData
            ))
        }
    } else {
        query().collect { emit(Resource.Success(it)) }
    }
}
