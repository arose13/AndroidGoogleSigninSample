package se.stephenro.customcloudtest;

import android.accounts.Account;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.IOException;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;
import se.stephenro.customcloudtest.api.GSPService;

public class Login extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final String TAG = Login.class.getSimpleName();
    public static final String SERVER_CLIENT_ID = "260943555378-md1kjfa8nq2q8he9no9r4m8m6v5ek27v.apps.googleusercontent.com";

    // Request code used to invoke sign in user interactions
    private static final int RC_SIGN_IN = 0;

    // Client used to interact with Google APIs
    private GoogleApiClient googleApiClient;
    private String userIdToken;

    // Sign-in button
    private View googleLoginButton;
    private boolean isResolving = false; // Is there a ConnectionResult resolution in progress?
    private boolean shouldResolve = false; // Should we automatically resolve ConnectionResults when possible?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View Init
        setContentView(R.layout.activity_login);

        googleLoginButton = findViewById(R.id.sign_in_button);
        googleLoginButton.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Google Init
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope((Scopes.EMAIL)))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further
            if (resultCode != RESULT_OK) {
                shouldResolve = false;
            }

            isResolving = false;
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected. Get Whatever you need from the user here!
        if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
            Log.d(TAG, "Proof of successful login");
            Log.d(TAG, currentPerson.getDisplayName());

            // TEST contacting the server with Retrofit
            new ContactServer().execute();

            // Get ID Token for Backend Access
            //new GetIdTokenTask().execute();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                shouldResolve = true;
                googleApiClient.connect();
                Log.d(TAG, "Signing in");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed: " + connectionResult);

        if (!isResolving && shouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    isResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult", e);
                    isResolving = false;
                    googleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an error dialog
                Log.e(TAG, "showErrorDialog: " + connectionResult.toString());
                //showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            Log.i(TAG, "showSignedOutUI");
            //showSignedOutUI();
        }
    }

    private class ContactServer extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "Attempting to contact the server");
            sendTokenToServer();
            return null;
        }

    }

    private class GetIdTokenTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String accountName = Plus.AccountApi.getAccountName(googleApiClient);
            Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            String scopes = "audience:server:client_id:" + SERVER_CLIENT_ID; // Not the app's client ID.
            try {
                userIdToken = GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);

                return userIdToken;
            } catch (IOException e) {
                Log.e(TAG, "IOError retrieving ID token.", e);
                return null;
            } catch (GoogleAuthException e) {
                Log.e(TAG, "GoogleAuthError retrieving ID token.", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // Successfully retrieved ID Token
                Log.i(TAG, "ID token: " + result);
                //sendTokenToServer(); This is the real location for the SendToken method
            } else {
                // There was some error getting the ID Token
                // I don't know why this would happen to be honest
                Log.e(TAG, "ID token was null!");
            }
        }

    }

    // Send Token to the server using Retrofit
    private void sendTokenToServer() {
        // Create a very simple REST adapter which points the GSP API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GSPService.BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        // Create an instance of our GSP API interface
        GSPService.BackendApi gspApi = retrofit.create(GSPService.BackendApi.class);

        // Create a call instance for getting the test data from GSP
        Call<GSPService.TestData> call = gspApi.testResp();

        // Fetch and output the response
        try {
            GSPService.TestData testData = call.execute().body();
            Log.d(TAG, testData.getTitle() + " : " + testData.getContent());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Trouble getting TestData");
        }
    }

}
