package com.example.panicshield.data.remote.dto

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class JsonMapDeserializer : JsonDeserializer<Map<String, Any>?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Map<String, Any>? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonObject -> {
                // Si ya es un objeto JSON, convertirlo a Map
                val mapType = object : TypeToken<Map<String, Any>>() {}.type
                context?.deserialize<Map<String, Any>>(json, mapType)
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                // Si es un string, intentar parsearlo como JSON
                try {
                    val jsonString = json.asString
                    if (jsonString.isBlank() || jsonString == "null") {
                        null
                    } else {
                        val gson = Gson()
                        val mapType = object : TypeToken<Map<String, Any>>() {}.type
                        gson.fromJson<Map<String, Any>>(jsonString, mapType)
                    }
                } catch (e: Exception) {
                    // Si no se puede parsear, devolver un mapa con el string original
                    mapOf("raw_data" to json.asString)
                }
            }
            else -> {
                // Para otros tipos, intentar convertir a mapa o devolver null
                null
            }
        }
    }
}
