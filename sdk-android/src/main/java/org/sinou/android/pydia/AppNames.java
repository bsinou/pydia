package org.sinou.android.pydia;

public interface AppNames {

    // TODO  make this generic
    String KEY_PREFIX = "org.sinou.android.pydia";
    String KEY_PREFIX_ = KEY_PREFIX + ".";

    /* SHARED PREFERENCE KEYS */
    String PREF_KEY_LAST_STATE = "last_state";

    String KEY_DESTINATION = KEY_PREFIX_ + "destination";

    String EXTRA_STATE = KEY_PREFIX_ + "state";
    String EXTRA_ACCOUNT_ID = KEY_PREFIX_ + "account.id";
    String EXTRA_SERVER_URL = KEY_PREFIX_ + "server.url";
    String EXTRA_SERVER_IS_LEGACY = KEY_PREFIX_ + "server.islegacy";
    String EXTRA_SESSION_UID = KEY_PREFIX_ + "session.uid";

    String KEY_CODE = "code";
    String KEY_STATE = "state";

    // TODO finalize auth state management
    String AUTH_STATUS_NEW = "new";
    String AUTH_STATUS_NO_CREDS = "no-credentials";
    String AUTH_STATUS_EXPIRED = "expired";
    String AUTH_STATUS_REFRESHING = "refreshing";
    String AUTH_STATUS_CONNECTED = "connected";

}
