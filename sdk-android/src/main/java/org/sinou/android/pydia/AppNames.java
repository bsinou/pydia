package org.sinou.android.pydia;

public interface AppNames {

    // TODO  make this generic
    String KEY_PREFIX = "org.sinou.android.pydia";
    String KEY_PREFIX_ = KEY_PREFIX + ".";

    /* SHARED PREFERENCE KEYS */
    String PREF_KEY_CURRENT_STATE = "current_state";
    String PREF_KEY_CURR_RECYCLER_LAYOUT = "current_recycler_layout";
    String KEY_DESTINATION = KEY_PREFIX_ + "destination";

    /* SHARED PREFERENCE WELL KNOWN VALUES */
    String RECYCLER_LAYOUT_LIST = "list";
    String RECYCLER_LAYOUT_GRID = "grid";

    /* GENERIC ACTIONS */
    String ACTION_MORE = "more";
    String ACTION_OPEN = "open";
    String ACTION_CANCEL = "cancel";
    String ACTION_RESTART = "restart";

    String ACTION_LOGIN = "login";
    String ACTION_LOGOUT = "logout";
    String ACTION_FORGET = "forget";

    /* SUPPORTED MODIFICATION STATUS */
    String LOCAL_MODIF_DELETE = "deleting";
    String LOCAL_MODIF_RENAME = "renaming";
    String LOCAL_MODIF_MOVE = "moving";
    String LOCAL_MODIF_RESTORE = "restore";

    /* INTENTS */
    String EXTRA_STATE = KEY_PREFIX_ + "state";
    String EXTRA_ACCOUNT_ID = KEY_PREFIX_ + "account.id";
    String EXTRA_SERVER_URL = KEY_PREFIX_ + "server.url";
    String EXTRA_SERVER_IS_LEGACY = KEY_PREFIX_ + "server.islegacy";
    String EXTRA_AFTER_AUTH_ACTION = KEY_PREFIX_ + "auth.next.action";
    String EXTRA_SESSION_UID = KEY_PREFIX_ + "session.uid";

    String KEY_CODE = "code";
    String KEY_STATE = "state";

    // Workaround to store additional destinations as state
    String CUSTOM_PATH_ACCOUNTS = "/__acounts__";
    String CUSTOM_PATH_BOOKMARKS = "/__bookmarks__";
    String CUSTOM_PATH_OFFLINE = "/__offline__";
    String CUSTOM_PATH_SHARES = "/__shares__";

    // TODO finalize auth state management
    String AUTH_STATUS_NEW = "new";
    String AUTH_STATUS_NO_CREDS = "no-credentials";
    String AUTH_STATUS_EXPIRED = "expired";
    String AUTH_STATUS_REFRESHING = "refreshing";
    String AUTH_STATUS_CONNECTED = "connected";


    // Local tree:
    // baseDir +--- cache +--- accountID +--- thumbs
    //                                   +--- cache
    //         +--- files +--- accountID +--- offline
    String THUMB_PARENT_DIR = "thumbs";
    String CACHED_FILE_PARENT_DIR = "cache";
    String TRANSFER_PARENT_DIR = "transfers";
    String OFFLINE_FILE_PARENT_DIR = "offline";

    // Local file types
    String LOCAL_FILE_TYPE_NONE = "none";
    String LOCAL_FILE_TYPE_THUMB = "thumb";
    String LOCAL_FILE_TYPE_TRANSFER = "transfer";
    String LOCAL_FILE_TYPE_CACHE = "cache";
    String LOCAL_FILE_TYPE_OFFLINE = "offline";
    // TODO
    // String LOCAL_FILE_TYPE_EXTERNAL = "external";

}
