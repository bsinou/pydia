package org.sinou.android.pydia.auth;

import org.sinou.android.pydia.model.State;

public interface AuthenticationEventHandler {

        void onError(String error, String description);

        void afterAuth(State state);

    }
