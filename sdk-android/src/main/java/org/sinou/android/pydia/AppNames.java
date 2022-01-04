package org.sinou.android.pydia;

public interface AppNames {

    // TODO  make this generic
    String KEY_PREFIX_ = "org.sinou.android.pydia.";
    String KEY_DESTINATION = KEY_PREFIX_ + "destination";

    String EXTRA_STATE = KEY_PREFIX_ + "state";
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
