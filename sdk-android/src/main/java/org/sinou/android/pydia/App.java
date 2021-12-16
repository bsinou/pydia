package org.sinou.android.pydia;

import android.app.Application;
import android.content.Context;

import com.pydio.cells.api.SDKException;

import org.sinou.android.pydia.ui.auth.OAuthCallbackManager;
import org.sinou.android.pydia.data.ktx.AccountDatabase;
import org.sinou.android.pydia.services.AccountService;
import org.sinou.android.pydia.services.BackendService;
import org.sinou.android.pydia.services.SessionFactory;

import java.io.File;


public class App extends Application {

    private static App instance;
    private static BackendService backendService;

    @Override
    public void onCreate() {
        super.onCreate();


        instance = this;

        try {
            backendService = new BackendService(context(), getPackageName());
            // TODO make this asynchone
            // Background.go(() -> backendService.afterCreate());
            backendService.afterCreate();
        } catch (SDKException sdkException) {
            throw new RuntimeException("Could not initialize backend. Exiting.", sdkException);
        }
    }

    public AccountDatabase getDatabase() {
        return AccountDatabase.Companion.getDatabase(this);
    }

    public static boolean isBackendReady() {
        if (backendService == null) {
            return false;
        }
        return backendService.isReady();
    }


    public static OAuthCallbackManager getOAuthCallbackManager() {
        if (!isBackendReady()) {
            return null;
        }
        return backendService.getOAuthCallbackManager();
    }


    public static SessionFactory getSessionFactory() {
        if (!isBackendReady()) {
            return null;
        }
        return backendService.getClientFactory();
    }


    public static AccountService getAccountService() {
        if (!isBackendReady()) {
            return null;
        }
        return backendService.getAccountService();
    }

    public static Context context() {
        return instance.getApplicationContext();
    }

    public static File baseDir() {
        return context().getFilesDir();
    }
}
