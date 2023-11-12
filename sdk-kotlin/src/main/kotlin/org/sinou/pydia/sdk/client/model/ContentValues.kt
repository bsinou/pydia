package org.sinou.pydia.sdk.client.model

/*
 * Copyright (C) 2007 The Android Open Source Project
 * Modifications: Copyright (C) 2020 Abstrium SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /**
 * This class is used to store a set of values that the ContentResolver can
 * process.
 */
class ContentValues {
    /**
     * Holds the actual values
     */
    private val mValues: HashMap<String, Any?>

    /**
     * Creates an empty set of values using the default initial size
     */
    constructor() {
        // Choosing a default size of 8 based on analysis of typical
        // consumption by applications.
        mValues = HashMap(8)
    }

    /**
     * Creates an empty set of values using the given initial size
     *
     * @param size the initial size of the set of values
     */
    constructor(size: Int) {
        mValues = HashMap(size, 1.0f)
    }

    /**
     * Creates a set of values copied from the given set
     *
     * @param from the values to copy
     */
    constructor(from: ContentValues) {
        mValues = HashMap(from.mValues)
    }

    /**
     * Creates a set of values copied from the given HashMap. This is used by the
     * Parcel unmarshalling code.
     *
     * @param values the values to start with
     */
    private constructor(values: HashMap<String, Any?>) {
        mValues = values
    }

    override fun equals(`object`: Any?): Boolean {
        return if (`object` !is ContentValues) {
            false
        } else mValues == `object`.mValues
    }

    override fun hashCode(): Int {
        return mValues.hashCode()
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: String?) {
        mValues[key] = value
    }

    /**
     * Adds all values from the passed in ContentValues.
     *
     * @param other the ContentValues from which to copy
     */
    fun putAll(other: ContentValues) {
        mValues.putAll(other.mValues)
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: Byte?) {
        mValues[key] = value
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: Short?) {
        mValues[key] = value
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: Int?) {
        mValues[key] = value
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: Long?) {
        mValues[key] = value
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: Float?) {
        mValues[key] = value
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: Double?) {
        mValues[key] = value
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: Boolean?) {
        mValues[key] = value
    }

    /**
     * Adds a value to the set.
     *
     * @param key   the name of the value to put
     * @param value the data for the value to put
     */
    fun put(key: String, value: ByteArray?) {
        mValues[key] = value
    }

    /**
     * Adds a null value to the set.
     *
     * @param key the name of the value to make null
     */
    fun putNull(key: String) {
        mValues[key] = null
    }

    /**
     * Returns the number of values.
     *
     * @return the number of values
     */
    fun size(): Int {
        return mValues.size
    }

    /**
     * Remove a single value.
     *
     * @param key the name of the value to remove
     */
    fun remove(key: String) {
        mValues.remove(key)
    }

    /**
     * Removes all values.
     */
    fun clear() {
        mValues.clear()
    }

    /**
     * Returns true if this object has the named value.
     *
     * @param key the value to check for
     * @return `true` if the value is present, `false` otherwise
     */
    fun containsKey(key: String): Boolean {
        return mValues.containsKey(key)
    }

    /**
     * Gets a value. Valid value types are [String], [Boolean], and
     * [Number] implementations.
     *
     * @param key the value to getRequest
     * @return the data for the value
     */
    operator fun get(key: String): Any? {
        return mValues[key]
    }

    /**
     * Gets a value and converts it to a String.
     *
     * @param key the value to getRequest
     * @return the String for the value
     */
    fun getAsString(key: String): String? {
        val value = mValues[key]
        return value?.toString()
    }

    /**
     * Gets a value and converts it to a Long.
     *
     * @param key the value to getRequest
     * @return the Long value, or null if the value is missing or cannot be
     * converted
     */
    fun getAsLong(key: String): Long? {
        val value = mValues[key]
        return try {
            if (value != null) (value as Number).toLong() else null
        } catch (e: ClassCastException) {
            if (value is CharSequence) {
                try {
                    java.lang.Long.valueOf(value.toString())
                } catch (e2: NumberFormatException) {
                    //// Log.e(TAG, "Cannot parse Long value for " + value + " at key " + key);
                    null
                }
            } else {
                //// Log.e(TAG, "Cannot cast value for " + key + " to a Long: " + value, e);
                null
            }
        }
    }

    /**
     * Gets a value and converts it to an Integer.
     *
     * @param key the value to getRequest
     * @return the Integer value, or null if the value is missing or cannot be
     * converted
     */
    fun getAsInteger(key: String): Int? {
        val value = mValues[key]
        return try {
            if (value != null) (value as Number).toInt() else null
        } catch (e: ClassCastException) {
            if (value is CharSequence) {
                try {
                    Integer.valueOf(value.toString())
                } catch (e2: NumberFormatException) {
                    //// Log.e(TAG, "Cannot parse Integer value for " + value + " at key " + key);
                    null
                }
            } else {
                //// Log.e(TAG, "Cannot cast value for " + key + " to a Integer: " + value, e);
                null
            }
        }
    }

    /**
     * Gets a value and converts it to a Short.
     *
     * @param key the value to getRequest
     * @return the Short value, or null if the value is missing or cannot be
     * converted
     */
    fun getAsShort(key: String): Short? {
        val value = mValues[key]
        return try {
            if (value != null) (value as Number).toShort() else null
        } catch (e: ClassCastException) {
            if (value is CharSequence) {
                try {
                    value.toString().toShort()
                } catch (e2: NumberFormatException) {
                    //// Log.e(TAG, "Cannot parse Short value for " + value + " at key " + key);
                    null
                }
            } else {
                //// Log.e(TAG, "Cannot cast value for " + key + " to a Short: " + value, e);
                null
            }
        }
    }

    /**
     * Gets a value and converts it to a Byte.
     *
     * @param key the value to getRequest
     * @return the Byte value, or null if the value is missing or cannot be
     * converted
     */
    fun getAsByte(key: String): Byte? {
        val value = mValues[key]
        return try {
            if (value != null) (value as Number).toByte() else null
        } catch (e: ClassCastException) {
            if (value is CharSequence) {
                try {
                    java.lang.Byte.valueOf(value.toString())
                } catch (e2: NumberFormatException) {
                    //// Log.e(TAG, "Cannot parse Byte value for " + value + " at key " + key);
                    null
                }
            } else {
                //// Log.e(TAG, "Cannot cast value for " + key + " to a Byte: " + value, e);
                null
            }
        }
    }

    /**
     * Gets a value and converts it to a Double.
     *
     * @param key the value to getRequest
     * @return the Double value, or null if the value is missing or cannot be
     * converted
     */
    fun getAsDouble(key: String): Double? {
        val value = mValues[key]
        return try {
            if (value != null) (value as Number).toDouble() else null
        } catch (e: ClassCastException) {
            if (value is CharSequence) {
                try {
                    java.lang.Double.valueOf(value.toString())
                } catch (e2: NumberFormatException) {
                    //// Log.e(TAG, "Cannot parse Double value for " + value + " at key " + key);
                    null
                }
            } else {
                //// Log.e(TAG, "Cannot cast value for " + key + " to a Double: " + value, e);
                null
            }
        }
    }

    /**
     * Gets a value and converts it to a Float.
     *
     * @param key the value to getRequest
     * @return the Float value, or null if the value is missing or cannot be
     * converted
     */
    fun getAsFloat(key: String): Float? {
        val value = mValues[key]
        return try {
            if (value != null) (value as Number).toFloat() else null
        } catch (e: ClassCastException) {
            if (value is CharSequence) {
                try {
                    java.lang.Float.valueOf(value.toString())
                } catch (e2: NumberFormatException) {
                    //// Log.e(TAG, "Cannot parse Float value for " + value + " at key " + key);
                    null
                }
            } else {
                //// Log.e(TAG, "Cannot cast value for " + key + " to a Float: " + value, e);
                null
            }
        }
    }

    /**
     * Gets a value and converts it to a Boolean.
     *
     * @param key the value to getRequest
     * @return the Boolean value, or null if the value is missing or cannot be
     * converted
     */
    fun getAsBoolean(key: String): Boolean? {
        val value = mValues[key]
        return try {
            value as Boolean?
        } catch (e: ClassCastException) {
            if (value is CharSequence) {
                java.lang.Boolean.valueOf(value.toString())
            } else if (value is Number) {
                value.toInt() != 0
            } else {
                //// Log.e(TAG, "Cannot cast value for " + key + " to a Boolean: " + value, e);
                null
            }
        }
    }

    /**
     * Gets a value that is a byte array. Note that this method will not convert any
     * other types to byte arrays.
     *
     * @param key the value to getRequest
     * @return the byte[] value, or null is the value is missing or not a byte[]
     */
    fun getAsByteArray(key: String): ByteArray? {
        val value = mValues[key]
        return if (value is ByteArray) {
            value
        } else {
            null
        }
    }

    /**
     * Returns a set of all of the keys and values
     *
     * @return a set of all of the keys and values
     */
    fun valueSet(): Set<Map.Entry<String, Any?>> {
        return mValues.entries
    }

    /**
     * Returns a set of all of the keys
     *
     * @return a set of all of the keys
     */
    fun keySet(): Set<String> {
        return mValues.keys
    }

    /*
     * public static final Parcelable.Creator<ContentValues> CREATOR = new
     * Parcelable.Creator<ContentValues>() {
     *
     * @SuppressWarnings({"deprecation", "unchecked"}) public ContentValues
     * createFromParcel(Parcel in) { // TODO - what ClassLoader should be passed to
     * readHashMap? HashMap<String, Object> values = in.readHashMap(null); return
     * new ContentValues(values); }
     *
     * public ContentValues[] newArray(int size) { return new ContentValues[size]; }
     * };
     */
    fun describeContents(): Int {
        return 0
    }
    /*
     * @SuppressWarnings("deprecation") public void writeToParcel(Parcel parcel, int
     * flags) { parcel.writeMap(mValues); }
     */
    /**
     * Unsupported, here until we getRequest proper bulk insert APIs.
     */
    @Deprecated("")
    fun putStringArrayList(key: String, value: ArrayList<String?>?) {
        mValues[key] = value
    }

    /**
     * Unsupported, here until we getRequest proper bulk insert APIs.
     */
    @Deprecated("")
    fun getStringArrayList(key: String): ArrayList<String>? {
        return mValues[key] as ArrayList<String>?
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * object.
     *
     * @return a printable representation of this object.
     */
    override fun toString(): String {
        val sb = StringBuilder()
        for (name in mValues.keys) {
            val value = getAsString(name)
            if (sb.length > 0) sb.append(" ")
            sb.append(name).append("=").append(value)
        }
        return sb.toString()
    }

    companion object {
        const val TAG = "ContentValues"
    }
}