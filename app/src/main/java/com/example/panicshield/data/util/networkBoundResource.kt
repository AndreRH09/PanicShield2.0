package com.example.panicshield.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Función que implementa el patrón NetworkBoundResource para sincronizar
 * datos entre la base de datos local y la red
 */
fun <ResultType, RequestType> networkBoundResource(
    query: () -> Flow<ResultType>,
    fetch: suspend () -> RequestType,
    saveFetchResult: suspend (RequestType) -> Unit,
    shouldFetch: (ResultType) -> Boolean = { true }
): Flow<Resource<ResultType>> = flow {
    // Primero obtenemos los datos locales
    val initialData = query().first()

    // Emitimos estado de carga con los datos existentes (si los hay)
    emit(Resource.Loading(initialData))

    // Decidimos si necesitamos hacer petición a la red
    if (shouldFetch(initialData)) {
        try {
            // Hacemos la petición a la red
            val networkResult = fetch()

            // Guardamos los datos en la base de datos local
            saveFetchResult(networkResult)

            // Emitimos los datos actualizados desde la base de datos local
            query().collect { localData ->
                emit(Resource.Success(localData))
            }
        } catch (throwable: Throwable) {
            // En caso de error, emitimos error pero conservamos los datos locales
            emit(Resource.Error(
                message = "Error de red: ${throwable.message ?: "Error desconocido"}",
                data = initialData
            ))
        }
    } else {
        // Si no necesitamos hacer petición a la red, solo emitimos los datos locales
        query().collect { localData ->
            emit(Resource.Success(localData))
        }
    }
}