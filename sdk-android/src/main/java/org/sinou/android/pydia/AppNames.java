package org.sinou.android.pydia;

public interface AppNames {

    // TODO  make this generic
    String KEY_PREFIX = "org.sinou.android.pydia";
    String KEY_PREFIX_ = KEY_PREFIX + ".";

    /* SHARED PREFERENCE KEYS */
    String PREF_KEY_CURRENT_STATE = "current_state";
    String PREF_KEY_CURR_RECYCLER_LAYOUT = "current_recycler_layout";
    String PREF_KEY_CURR_RECYCLER_ORDER = "current_recycler_order";
    String PREF_KEY_CURR_RECYCLER_ORDER_DIR = "current_recycler_order_dir";
    String KEY_DESTINATION = KEY_PREFIX_ + "destination";

    /* SHARED PREFERENCE WELL KNOWN VALUES */
    String RECYCLER_LAYOUT_LIST = "list";
    String RECYCLER_LAYOUT_GRID = "grid";
    int ITEM_TYPE_HEADER = 0;
    int ITEM_TYPE_WS = 1;
    int ITEM_TYPE_NODE = 2;


    /* GENERIC ACTIONS */
    String ACTION_MORE = KEY_PREFIX_ + "more";
    String ACTION_OPEN = KEY_PREFIX_ + "open";
    String ACTION_CANCEL = KEY_PREFIX_ + "cancel";
    String ACTION_RESTART = KEY_PREFIX_ + "restart";
    String ACTION_CHOOSE_TARGET = KEY_PREFIX_ + "choosetarget";
    String ACTION_COPY = KEY_PREFIX_ + "copy";
    String ACTION_MOVE = KEY_PREFIX_ + "move";
    String ACTION_UPLOAD = KEY_PREFIX_ + "upload";

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
    String EXTRA_SERVER_URL = KEY_PREFIX_ + "server.url";
    String EXTRA_SERVER_IS_LEGACY = KEY_PREFIX_ + "server.islegacy";
    String EXTRA_AFTER_AUTH_ACTION = KEY_PREFIX_ + "auth.next.action";
    String EXTRA_ACTION_CONTEXT = KEY_PREFIX_ + "context.action";
    String EXTRA_SESSION_UID = KEY_PREFIX_ + "session.uid";

    String KEY_CODE = "code";
    String KEY_STATE = "state";

    String CELLS_ROOT_ENCODED_STATE = "cells%3A%2F%2Froot";

    // Workaround to store additional destinations as state
    String CUSTOM_PATH_ACCOUNTS = "/__acounts__";
    String CUSTOM_PATH_BOOKMARKS = "/__bookmarks__";
    String CUSTOM_PATH_OFFLINE = "/__offline__";
    String CUSTOM_PATH_SHARES = "/__shares__";

    // TODO finalize auth state management
    // Account Authentication States
    String AUTH_STATUS_NEW = "new";
    String AUTH_STATUS_NO_CREDS = "no-credentials";
    String AUTH_STATUS_UNAUTHORIZED = "unauthorized";
    String AUTH_STATUS_EXPIRED = "expired";
    String AUTH_STATUS_REFRESHING = "refreshing";
    String AUTH_STATUS_CONNECTED = "connected";
    // Session  Lifecycle States
    String SESSION_STATE_NEW = "new";
    String LIFECYCLE_STATE_FOREGROUND = "foreground";
    String LIFECYCLE_STATE_BACKGROUND = "background";
    String LIFECYCLE_STATE_PAUSED = "paused";

    // Local tree:
    // baseDir +--- cache +--- accountID +--- thumbs
    //                                   +--- cache
    //         +--- files +--- accountID +--- offline
    String THUMB_PARENT_DIR = "thumbs";
    String CACHED_FILE_PARENT_DIR = "cache";
    String TRANSFER_PARENT_DIR = "transfers";
    String OFFLINE_FILE_PARENT_DIR = "offline";

    // Local types
    String LOCAL_DIR_TYPE_CACHE = "cache";
    String LOCAL_DIR_TYPE_FILE = "files";
    String LOCAL_FILE_TYPE_NONE = "none";
    String LOCAL_FILE_TYPE_THUMB = "thumb";
    String LOCAL_FILE_TYPE_TRANSFER = "transfer";
    String LOCAL_FILE_TYPE_CACHE = "cache";
    String LOCAL_FILE_TYPE_OFFLINE = "offline";
    // TODO
    // String LOCAL_FILE_TYPE_EXTERNAL = "external";

}
