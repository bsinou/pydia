package org.sinou.android.pydia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.pydio.cells.api.Server;
import com.pydio.cells.transport.CellsTransport;
import com.pydio.cells.transport.auth.jwt.OAuthConfig;
import com.pydio.cells.utils.Log;
import com.pydio.cells.utils.Str;

import org.sinou.android.pydia.auth.AuthenticationEventHandler;
import org.sinou.android.pydia.auth.OAuthCallbackManager;
import org.sinou.android.pydia.model.State;

public class Authenticate extends AppCompatActivity implements AppNames, AuthenticationEventHandler {

    private State previousState;
    private Server server;
    private int errorCode;

    private FrameLayout rootView;


    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_authenticate);

        Intent intent = getIntent();

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri == null) {
                finish();
                return;
            }

            System.out.println("In CellsAuthURLHandler, URI: " + uri.toString());
            App.getOAuthCallbackManager().handleOAuthResponse(uri);
            finish();
        }

        String encodedState = intent.getStringExtra(EXTRA_STATE);
        if (Str.empty(encodedState)) {
            return;
        }

        previousState = State.fromEncodedState(encodedState);
        errorCode = intent.getIntExtra(EXTRA_ERROR_CODE, 0);
        launchOAuthProcess();

/*        try {

        } catch (SDKException se) {
            if (se.getCode() == ErrorCodes.init_failed) {
                showMessage("Could not initiate authentication process: ".concat(se.getMessage()));
            } else {
                showMessage("Unexpected error: ".concat(se.getMessage()));
                Log.e(Log.TAG_UI, "Unable to create authenticate activity");
                se.printStackTrace();
            }
            finish();
        }*/
    }

    private void launchOAuthProcess() {

        server = App.getSessionFactory().getServer(previousState.getServerUrl());

        OAuthCallbackManager authCallbackManager = App.getOAuthCallbackManager();
        CellsTransport transport = (CellsTransport) App.getSessionFactory().getAnonymousTransport(previousState.getServerUrl());
        String state = authCallbackManager.prepareCallback(transport, this);

        OAuthConfig cfg = transport.getServer().getOAuthConfig();
        Uri uri = authCallbackManager.getUriData(cfg, state);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }

    public void onError(String error, String description) {
        // String message = getResources().getString(R.string.authentication_failed);
        showMessage("AUthentication failed....");
        Log.e(Log.TAG_AUTH, error.concat(": ").concat(description));
    }

    public void showMessage(String msg) {
        Snackbar snackbar = Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void afterAuth(State newState) {
        if (newState == null) {
            Log.d(Log.TAG_AUTH, "In afterAuth with a null state, this should not happen");
            Thread.dumpStack();
            return;
        }

/*        String message = String.format(getString(R.string.authenticated_as), newState.getUsername());
        showMessage(message);

        newState.setSaver((v, err) -> App.setPreference(App.PREF_APP_STATE, v));

        Intent intent = new Intent(this, Browser.class);
        if (newState.getServerUrl().equals(previousState.getServerUrl()) && previousState.getPath() != null) {
            intent.putExtra(GuiNames.EXTRA_STATE, newState.toString());
            setResult(IntentCode.authentication, intent);
        } else {
            // newState has a username (previous state has not if we come from RegisterServer Activity)
            intent.putExtra(GuiNames.EXTRA_STATE, newState.toString());
            startActivity(intent);
        }

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);*/
        finish();
    }


}
