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

    /* INTENTS */
    String EXTRA_STATE = KEY_PREFIX_ + "state";
    String EXTRA_ACCOUNT_ID = KEY_PREFIX_ + "account.id";
    String EXTRA_SERVER_URL = KEY_PREFIX_ + "server.url";
    String EXTRA_SERVER_IS_LEGACY = KEY_PREFIX_ + "server.islegacy";
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

}
