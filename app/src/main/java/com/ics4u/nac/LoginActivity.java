package com.ics4u.nac;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nac.entity.AccountList_RTO;
import com.nac.entity.Account_RTO;
import com.nac.entity.ActivityList_RTO;
import com.nac.entity.Activity_RTO;
import com.nac.entity.CompoundExpr_RTO;
import com.nac.entity.Expr_RTO;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

/**
 * Assignment Name: The Login Screen
 * <p>
 * This is the LoginActivity class for the Login Screen, in which the user
 * must enter the correct username and password into the provided text fields.
 * If the user enters the correct credentials, then the program will display a
 * "Redirecting..." message. Otherwise, the user will be prompted to try again.
 * In addition, if the user fails three attempts in a row, they will not be allowed
 * to enter anything for some random time between 10 and 20 seconds.
 *
 * @author Sukhveer Sahota
 * @version 1.0
 * @since November 21st, 2017
 */
public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    // The following are objects for the various UI components, including
    // the buttons, text fields, and text views.
    private Toast currentlyDisplayedToast;

    public static String loggedInUserName, loggedInPassword;
    public static int loggedInAccountEID;

    /**
     * Implementation of the onCreate() method, which is used to set up this Activity
     * at the time of its creation (i.e. at the beginning of the program).
     *
     * @param savedInstanceState Required for the Activity superclass
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loggedInUserName = null;
        loggedInPassword = null;

        Button loginButton = findViewById(R.id.login_button);

        final EditText usernameEditText = findViewById(R.id.login_edit_text);
        final EditText passwordEditText = findViewById(R.id.password_edit_text);

        // Set the loginButton so that when the user presses it, the program checks the entered
        // credentials, and perform the appropriate action based on whether or not they are correct,
        // as well as the number of attempts that the user has made.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String enteredUsername = usernameEditText.getText().toString();
                final String enteredPassword = passwordEditText.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Try validating the user's credentials
                        try {
                            // If the enteredCredentials are correct (which can be checked using the
                            // areEnteredCredentialsCorrect() helper method), then display a "Redirecting"
                            // message to the user.
                            if (areEnteredCredentialsCorrect(enteredUsername, enteredPassword)) {
                                if (currentlyDisplayedToast != null) {
                                    currentlyDisplayedToast.cancel();
                                }
                                loggedInUserName = enteredUsername;
                                loggedInPassword = enteredPassword;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        currentlyDisplayedToast = Toast.makeText(getApplicationContext(), "Redirecting...", Toast.LENGTH_SHORT);
                                        currentlyDisplayedToast.show();
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                });
                            }
                            // Otherwise, display a "Wrong Credentials" message to the user.
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (currentlyDisplayedToast != null) {
                                            currentlyDisplayedToast.cancel();
                                        }
                                        currentlyDisplayedToast = Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_LONG);
                                        currentlyDisplayedToast.show();
                                    }
                                });
                            }
                        }
                        // If the user's credentials cannot be validated (due to an issue with connecting to the server),
                        // run the app in demo mode
                        catch (ProcessingException e) {
                            Log.d(TAG, "Credentials were not validated. Running in demo mode!");
                            if (currentlyDisplayedToast != null) {
                                currentlyDisplayedToast.cancel();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentlyDisplayedToast = Toast.makeText(getApplicationContext(), "Server currently unavailable. Running in demo mode...", Toast.LENGTH_LONG);
                                    currentlyDisplayedToast.show();
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        // Set the cancelButton so that when the user presses it, the app comes to an end.
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Helper method that checks if the credentials entered by the user are correct.
     *
     * @param enteredUsername The username that the user entered
     * @param enteredPassword The password that the user entered
     * @return boolean          True if the credentials are correct. False if they are incorrect
     */
    private boolean areEnteredCredentialsCorrect(String enteredUsername, String enteredPassword) throws ProcessingException {
        // Initialize the Network IO Controller
        Network_IO_Controller.getInstance().setBaseURLString(getResources().getString(R.string.baseURL));
        Network_IO_Controller.getInstance().initClient(getString(R.string.default_uid), getString(R.string.default_pwd));

        // Try to validate the credentials by sending them to the server
        try {
            Account_RTO enteredAccount = Network_IO_Controller.getInstance().getAccountFromUserID(enteredUsername);
            if (enteredAccount == null) {
                return false;
            }
            if (enteredAccount.getPwd().equals(enteredPassword)) {
                loggedInAccountEID = enteredAccount.getEid();
                return true;
            }
            return false;
        } catch (WebApplicationException e) {
            Log.d(TAG, e.toString());
            return false;
        }
    }
}