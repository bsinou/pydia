package org.sinou.android.pydia.auth;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pydio.cells.api.ServerURL;
import com.pydio.cells.transport.ServerURLImpl;
import com.pydio.cells.utils.Str;

import org.sinou.android.pydia.R;

import java.net.MalformedURLException;

/**
 * Let the user enter an URL toward a Pydio (P8 or Cells) server.
 * This also handles the process to accept a self-signed certificate.
 * <p>
 * Upon successful termination, we can forward to the Authenticate activity with a ServerURL
 * that can open connection to the server - with or without trusted certificate.
 */
public class ServerUrlFragment extends Fragment {

    private static final String TAG = "ServerUrlFragment";

    private View fragmentRoot;
    private LinearLayout statusLayout;

    private EditText urlEditText;
    private Button actionButton;

    // Until we resolve to a server, serverURL is the reference,
    private ServerURL serverURL;
    private byte[] certificateData;
    private boolean processing;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        fragmentRoot = inflater.inflate(R.layout.fragment_ask_url, container, false);

        urlEditText = fragmentRoot.findViewById(R.id.url_edit_text);
        urlEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                onActionButtonClicked(v);
                return true;
            }
            return false;
        });

        Button actionButton = fragmentRoot.findViewById(R.id.action_button);
        actionButton.setOnClickListener(this::onActionButtonClicked);

//        statusLayout = fragmentRoot.findViewById(R.id.status_layout);
//        setStatusEditing();

        // Inflate the layout for this fragment
        return fragmentRoot;
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        urlEditText.requestFocus();
    }


    private void resolveServer() {
//        hideKeyBoard(urlEditText);

        String host = urlEditText.getText().toString();
        if (Str.empty(host)) {
            // TODO i18n
//            showMessage("Please provide your server address.");
            return;
        }

        try {
            serverURL = ServerURLImpl.fromAddress(host);
        } catch (MalformedURLException e) {
            // TODO i18n
//            showMessage("Not a valid URL");
            return;
        }

        setStatusLoading();

/*
        final RegisterServer registerServerTask = new RegisterServer(serverURL);
        registerServerTask.onFailure(this::handleRegistrationError);
        registerServerTask.onComplete(() -> handleOK(registerServerTask.getServer()));
        Worker worker = new Worker(registerServerTask);
        worker.execute();
*/
    }

/*
    private void resolveServer() {
        hideKeyBoard(urlEditText);

        String host = urlEditText.getText().toString();
        if (Str.empty(host)) {
            // TODO i18n
            showMessage("Please provide your server address.");
            return;
        }

        try {
            serverURL = ServerURLImpl.fromAddress(host);
        } catch (MalformedURLException e) {
            // TODO i18n
            showMessage("Not a valid URL");
            return;
        }

        setStatusLoading();

        final RegisterServer registerServerTask = new RegisterServer(serverURL);
        registerServerTask.onFailure(this::handleRegistrationError);
        registerServerTask.onComplete(() -> handleOK(registerServerTask.getServer()));
        Worker worker = new Worker(registerServerTask);
        worker.execute();
    }

    private void handleRegistrationError(ErrorInfo errorInfo) {
        setStatusEditing();
        if (errorInfo.isConnectionFailed()) {
            showMessage(R.string.server_con_failed);
        } else if (errorInfo.isSslError() || errorInfo.isServerSSLNotVerified()) {
            byte[][] certs = serverURL.getCertificateChain();
            if (certs != null && certs.length > 0) {
                certificateData = certs[0];
            }
            handleSelfSigned();

        } else if (errorInfo.isUnsupportedServer()) {
            showMessage(R.string.pydio_8_not_supported);
        } else {
            showMessage(R.string.could_not_reach_server_at, serverURL.getId());
            Log.e(Log.TAG_AUTH, errorInfo.getMessage());
        }
    }

    private void handleOK(Server server) {
        // After the RegisterServer task succeed:
        // - the server URL is correctly configured (including skip verify flag)
        // - the server object has been registered via the factory
        setStatusEditing();
        legacyWarning(server);
        goToLoginPage(server);
    }

    private void handleSelfSigned() {
        AcceptCertificateInfo info = new AcceptCertificateInfo(serverURL.getURL().toString(), certificateData);
        acceptCertificateActivityLauncher.launch(info);
    }

    private void selfSignedHasBeenAccepted() {
        serverURL = ServerURLImpl.withSkipVerify(serverURL);
        final RegisterServer registerServerTask = new RegisterServer(serverURL);
        registerServerTask.onFailure(this::handleRegistrationError);
        registerServerTask.onComplete(() -> handleOK(registerServerTask.getServer()));
        Worker worker = new Worker(registerServerTask);
        worker.execute();
    }

    private void legacyWarning(Server server) {
        boolean vanillaP8 = server.isLegacy() && !server.hasLicenseFeatures();
        if (vanillaP8) {
            // String version = server.getVersionName();

            // FIXME re-enable the Legacy warning for P8 home users.
            // if (version.length() != 32) {
            // String[] parts = version.split("\\.");
            // if (parts.length >= 3) {
            // String majorStr = parts[0];
            // int major = Integer.parseInt(majorStr);
            // if (major < 8) {
            // AlertDialog alertDialog = new AlertDialog.Builder(DeclareServer.this).create();
            // alertDialog.setTitle(getString(R.string.warning));
            // alertDialog.setMessage(getString(R.string.pydio_old_version_end_of_life));
            // alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
            // (dialog, which) -> {
            // dialog.dismiss();
            // AccountNode session = new AccountNode();
            // session.server = DeclareServer.this.server;
            // Application.addSession(session);
            // goToLoginPage(session);
            // }
            // );
            // alertDialog.show();
            // return;
            // }
            // }
            // }
        }
    }

    private void goToLoginPage(Server server) {
        rootView.post(() -> {
            StateID stateID = new StateID(server.url());
            Intent intent = new Intent(App.context(), Authenticate.class);
            intent.putExtra(GuiNames.EXTRA_STATE, stateID.toString());
            startActivity(intent);
            DeclareServer.this.startActivity(intent);
            DeclareServer.this.finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }
*/

    private void onActionButtonClicked(View v) {
        if (processing) {
            setStatusEditing();
            return;
        }
        resolveServer();
    }

    private void setStatusLoading() {
        if (processing) {
            return;
        }
        actionButton.setText(R.string.ask_url_cancel);
        processing = true;
        statusLayout.setVisibility(View.VISIBLE);
    }

    private void setStatusEditing() {
        processing = false;
        actionButton.setText(R.string.ask_url_confirm);
        statusLayout.setVisibility(View.INVISIBLE);
    }
/*
    private void afterCertificateValidated(Boolean accepted) {
        if (accepted) {
            selfSignedHasBeenAccepted();
        } else {
            showMessage(R.string.could_not_verify_cert);
        }
    }*/
}
