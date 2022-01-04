package org.sinou.android.pydia.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pydio.cells.api.SdkNames
import com.pydio.cells.api.ui.WorkspaceNode
import java.lang.reflect.Type

class Converters {

    @TypeConverter
    fun fromString(value: String): List<String> {
        //val listType: Type = object : TypeToken<List<String>>() {}.getType()
        val listType: Type = object : TypeToken<ArrayList<String>>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromJSON(value: String): List<WorkspaceNode> {
        //val listType: Type = object : TypeToken<List<String>>() {}.getType()
        val listType: Type = object : TypeToken<ArrayList<WorkspaceNode>>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromWorkspaces(list: List<WorkspaceNode>): String {
        return Gson().toJson(list)
    }


//    @TypeConverter
//    fun fromTimestamp(value: Long?): Date? {
//        return if (value == null) null else Date(value)
//    }
//
//    @TypeConverter
//    fun dateToTimestamp(date: Date?): Long? {
//        return date?.time
//    }

}