package org.sinou.pydia.client.core

enum class Status {
    OK, WARNING, DANGER
}

enum class ServerConnection {
    OK, LIMITED, UNREACHABLE;

    fun isConnected(): Boolean {
        return when (this) {
            OK, LIMITED -> true
            UNREACHABLE -> false
        }
    }
}

enum class NetworkStatus {
    OK, METERED, ROAMING, CAPTIVE, UNAVAILABLE, UNKNOWN;

    fun isConnected(): Boolean {
        return when (this) {
            UNKNOWN, OK, METERED, ROAMING -> true
            UNAVAILABLE, CAPTIVE -> false
        }
    }
}

enum class LoadingState {
    STARTING, PROCESSING, IDLE;

    fun isRunning(): Boolean {
        return when (this) {
            STARTING, PROCESSING -> true
            else -> false
        }
    }

}

//enum class SessionStatus {
//    NO_INTERNET, CAPTIVE, SERVER_UNREACHABLE, NOT_LOGGED_IN, CAN_RELOG, ROAMING, METERED, OK
//}

enum class LoginStatus(val id: String) {
    Undefined("undefined"),
    New("new"),
    NoCreds("no-credentials"),
    Unauthorized("unauthorized"),
    Expired("expired"),
    Refreshing("refreshing"),
    Connected("connected");

    fun isConnected(): Boolean {
        return when (this) {
            Connected -> true
            else -> false
        }
    }

    companion object {
        fun fromId(id: String): LoginStatus {
            return entries.find { it.id == id }
                ?: throw IllegalArgumentException("Invalid LoginStatus id: $id")
        }
    }
}

enum class ListType {
    DEFAULT, TRANSFER, JOB
}

enum class ListContext(val id: String) {
    ACCOUNTS("accounts"),
    BROWSE("browse"),
    BOOKMARKS("bookmarks"),
    OFFLINE("offline"),
    SEARCH("search"),
    TRANSFERS("transfers"),
    SYSTEM("system"),
}

enum class JobStatus(val id: String) {
    NEW("new"),
    PROCESSING("processing"),
//    CANCELLING("cancelling"),
    PAUSING("pausing"),
    CANCELLED("cancelled"),
    DONE("done"),
    PAUSED("paused"),
    WARNING("warning"),
    ERROR("error"),
    TIMEOUT("timeout"),
    NO_FILTER("no_filter"),
}
