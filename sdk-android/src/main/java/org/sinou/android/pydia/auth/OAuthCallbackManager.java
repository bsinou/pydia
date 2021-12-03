package org.sinou.android.pydia.auth;

import android.net.Uri;
import android.os.Handler;

import com.pydio.cells.api.Client;
import com.pydio.cells.api.CustomEncoder;
import com.pydio.cells.api.SDKException;
import com.pydio.cells.api.Store;
import com.pydio.cells.api.ui.WorkspaceNode;
import com.pydio.cells.transport.CellsTransport;
import com.pydio.cells.transport.ClientData;
import com.pydio.cells.transport.StateID;
import com.pydio.cells.transport.auth.Token;
import com.pydio.cells.transport.auth.credentials.JWTCredentials;
import com.pydio.cells.transport.auth.jwt.IdToken;
import com.pydio.cells.transport.auth.jwt.OAuthConfig;
import com.pydio.cells.utils.Log;

import org.sinou.android.pydia.App;
import org.sinou.android.pydia.Authenticate;
import org.sinou.android.pydia.model.AccountRecord;
import org.sinou.android.pydia.model.State;
import org.sinou.android.pydia.services.SessionFactory;
import org.sinou.android.pydia.utils.AndroidCustomEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OAuthCallbackManager {

    private final Store<Token> tokenStore;
    private final CustomEncoder encoder = new AndroidCustomEncoder();

    private final Map<String, CellsTransport> transports;
    public OAuthCallbackManager(Store<Token> tokenStore) {
        this.tokenStore = tokenStore;
        transports = new HashMap<>();
        callbacks = new HashMap<>();
    }

    private final Map<String, AuthenticationEventHandler> callbacks;

    public String prepareCallback(CellsTransport transport, AuthenticationEventHandler handler) {

        OAuthConfig cfg = transport.getServer().getOAuthConfig();
        // TODO insure the config is conform here.

        String savedState = createState(12);
        transports.put(savedState, transport);
        callbacks.put(savedState, handler);
        return savedState;
    }

    public Uri getUriData(OAuthConfig cfg, String state) {

        Uri.Builder uriBuilder = Uri.parse(cfg.authorizeEndpoint).buildUpon();
        uriBuilder = uriBuilder.appendQueryParameter("state", state)
                .appendQueryParameter("scope", cfg.scope)
                .appendQueryParameter("client_id", ClientData.getClientId())
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("redirect_uri", cfg.redirectURI);

        if (cfg.audience != null && !"".equals(cfg.audience)) {
            uriBuilder.appendQueryParameter("audience_id", cfg.audience);
        }

        return uriBuilder.build();
    }

    public void handleOAuthResponse(Uri uri) {
        try {
            // TODO Rather use an URI object to enable moving this to the SDK
            // URI otherURI = new URI(uri.toString());
            // System.out.println(otherURI);
            final CodeResponse response = readFromQuery(uri.getQuery());

            CellsTransport transport = transports.get(response.state);

            // TODO make this in background
            try {
                Token token = transport.getTokenFromCode(response.code, encoder);
                manageRetrievedToken(transport, response.state, token);
            } catch (Exception e) {
                Log.e("Init", "Could not finalize credential auth flow");
                e.printStackTrace();
                redirectToCallerWithError(response.state, e.getMessage());
            }

            //} catch (SDKException | URISyntaxException e) {
        } catch (SDKException e) {
            redirectToCallerWithError(null, e.getMessage());
        }
    }

    private void manageRetrievedToken(CellsTransport transport, String reqState, Token token) throws Exception {
        // TODO finer management of errors

        IdToken idToken = IdToken.parse(encoder, token.idToken);

        StateID accountID = new StateID(idToken.claims.name, transport.getServer().url());
        JWTCredentials jwt = new JWTCredentials(accountID.getUsername(), token);

        // TODO: here also launch a refresh workspace task and wait
        SessionFactory sf = App.getSessionFactory();
        sf.registerSession(transport.getServer().getServerURL(), jwt);

        // This will directly try to use the newly registered session to get a Client
        Client client = sf.getUnlockedClient(accountID.getId());
        Map<String, WorkspaceNode> workspaces = new HashMap<>();
        client.workspaceList((ws) -> workspaces.put(((WorkspaceNode) ws).getSlug(), (WorkspaceNode) ws));
        AccountRecord account = sf.getSession(accountID.getId()).getAccount();
        account.setWorkspaces(workspaces);
        App.getAccountService().updateAccount(account);
        App.getSessionFactory().loadKnownAccounts();


        // TODO ?
        // Set the session as current in the app
        // Adapt poll and tasks
        // check if it was a background thread

        redirectToCallerWithNewState(State.fromAccountId(accountID.getId()), reqState);
    }

    private void redirectToCallerWithNewState(State uiState, String reqState) {
        AuthenticationEventHandler currHandler = callbacks.get(reqState);
        if (currHandler == null) {
            Log.w("OAUTH", "Got an error but the handler seems not to be known");
        } else {
            Handler handler = new Handler(App.context().getMainLooper());
            handler.post(() -> currHandler.afterAuth(uiState));
        }
    }

    private void redirectToCallerWithError(String state, String description) {

        if (state == null) {
            Log.e("OAUTH", "No state for this error: " + description);
        }
        AuthenticationEventHandler currHandler = callbacks.get(state);
        if (currHandler == null) {
            Log.e("OAUTH", "Got an error but the handler seems not to be known: " + description);
        } else {
            Handler handler = new Handler(App.context().getMainLooper());
            handler.post(() -> currHandler.onError("could not get authentication token", description));
        }
    }


    private final static String SEED_CHARS = "abcdef1234567890";

    private String createState(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(SEED_CHARS.charAt(rand.nextInt(SEED_CHARS.length())));
        }
        return sb.toString();
    }

    private CodeResponse readFromQuery(String query) throws SDKException {

        if (query == null || "".equals(query)) {
            throw new SDKException("The callback URL query part is empty");
        }

        CodeResponse codeResponse = new CodeResponse();

        String[] splitQuery = query.split("&");
        loop:
        for (String part : splitQuery) {
            String[] pair = part.split("=");
            if (pair.length > 1) {
                switch (pair[0]) {
                    case "state":
                        codeResponse.state = pair[1];
                        continue loop;
                    case "code":
                        codeResponse.code = pair[1];
                        continue loop;
                    case "scope":
                        codeResponse.scope = pair[1];
                        continue loop;
                    case "error":
                        codeResponse.error = pair[1];
                        continue loop;
                    case "error_description":
                        codeResponse.errorDescription = pair[1];
                }
            }
        }

        if (codeResponse.error != null || codeResponse.errorDescription != null) {
            throw new SDKException(codeResponse.error + ": " + codeResponse.errorDescription);
        }

        if (codeResponse.state == null || !transports.containsKey(codeResponse.state)) {
            throw new SDKException("The returned state is invalid");
        }

        return codeResponse;
    }

    private static class CodeResponse {
        public String state;
        public String code;
        public String scope;
        public String error;
        public String errorDescription;
    }
}
