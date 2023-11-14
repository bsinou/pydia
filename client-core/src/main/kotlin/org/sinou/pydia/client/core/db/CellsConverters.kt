package org.sinou.pydia.client.core.db

import androidx.annotation.Keep
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.sinou.pydia.sdk.api.ServerURL
import org.sinou.pydia.sdk.transport.ServerURLImpl
import java.util.Properties

/** Enable serialization / deserialization to store complex objects in the DB using JSON */
class CellsConverters {

    @TypeConverter
    @Keep
    fun toProperties(value: String): Properties {
        val newType = TypeToken.get(Properties::class.javaObjectType)
        // val propType: Type = object : TypeToken<Properties>() {}.type
        return Gson().fromJson(value, newType)
    }

    @TypeConverter
    fun fromProperties(meta: Properties): String {
        return Gson().toJson(meta)
    }

    @TypeConverter
    fun fromServerURL(url: ServerURL): String {
        return url.toJson() ?: throw IllegalArgumentException("${url.id} toJson is null")
    }

    @TypeConverter
    @Keep
    fun toServerURL(value: String): ServerURL {
        return ServerURLImpl.fromJson(value)
    }
}
