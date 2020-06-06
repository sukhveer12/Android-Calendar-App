package com.ics4u.nac;

import android.content.Intent;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.nac.entity.AccountList_RTO;
import com.nac.entity.Account_RTO;
import com.nac.entity.Activity_RTO;
import com.nac.entity.CompoundExpr_RTO;
import com.nac.entity.Expr_RTO;

import java.util.ArrayList;
import java.util.Locale;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

/**
 * This is the ActivityShareActivity class for the NAC app. It falls under the View
 * category in the Model-View-Controller paradigm. It is used when the user wants
 * to send a notification regarding some activity via SMS to other users.
 *
 * @author Sukhveer Sahota
 * @version 1.0
 * @since June 3rd, 2018
 */
public class ActivityShareActivity extends AppCompatActivity {

    // Used for debugging
    private static final String TAG = "ActivityShareActivity";

    private ArrayList<Account_RTO> accounts;
    private ArrayList<Integer> accountIndicesToShareActivityDetailsWith;
    private int activityToBeSharedIndex;

    /**
     * Implementation of the onCreate() method, which is used to initialize the
     * various widgets on the UI, as well as initialize the SortController
     *
     * @param savedInstanceState Unused - passed to super constructor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewToLoading();

        new Thread(new Runnable() {
            @Override
            public void run() {
                fetchAndUpdateAccountList(null);
                accountIndicesToShareActivityDetailsWith = new ArrayList<>();
                setViewToShareActivity();
            }
        }).start();
    }

    /**
     * Helper method used to set the view to the Loading Screen
     */
    private void setViewToLoading() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setContentView(R.layout.activity_loading);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.activity_loading);
                }
            });
        }
    }

    /**
     * Helper method used to set the view to the Share Activity Screen
     */
    private void setViewToShareActivity() {
        // Ensure that the correct Thread is being used to exectute this task
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setContentView(R.layout.activity_share);
            initShareActivityView();
            initSearchParameterWidgets();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.activity_share);
                    initShareActivityView();
                    initSearchParameterWidgets();
                }
            });
        }
    }

    /**
     * Helper method used to initialize the various widgets that display the
     * properties of the selected activity
     */
    private void initShareActivityView() {
        ListView accountListView = findViewById(R.id.account_list_view);
        accountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox accountSelected = view.findViewById(R.id.share_with_account_checkbox);
                if (!accountSelected.isChecked()) {
                    accountSelected.setChecked(true);
                    accountIndicesToShareActivityDetailsWith.add(i);
                }
                else {
                    accountSelected.setChecked(false);
                    accountIndicesToShareActivityDetailsWith.remove(i);
                }
            }
        });
        AccountAdapter accountAdapter = new AccountAdapter(this, accounts);
        accountListView.setAdapter(accountAdapter);

        final TextView smsMessageBody = findViewById(R.id.sms_message_body);
        smsMessageBody.setMovementMethod(new ScrollingMovementMethod());

        Button addNameToSMS = findViewById(R.id.add_name_to_sms);
        addNameToSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsMessageBody.setText(String.format(Locale.getDefault(), "%s %s", smsMessageBody.getText().toString(), ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getShortName()));
            }
        });
        Button addDescriptionToSMS = findViewById(R.id.add_description_to_sms);
        addDescriptionToSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsMessageBody.setText(String.format(Locale.getDefault(), "%s %s", smsMessageBody.getText().toString(), ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getDescription()));
            }
        });
        Button addStartTimeToSMS = findViewById(R.id.add_start_time_to_sms);
        addStartTimeToSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsMessageBody.setText(String.format(Locale.getDefault(), "%s %s", smsMessageBody.getText().toString(), ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getStartTime()));
            }
        });
        Button addEndTimeToSMS = findViewById(R.id.add_end_time_to_sms);
        addEndTimeToSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsMessageBody.setText(String.format(Locale.getDefault(), "%s %s", smsMessageBody.getText().toString(), ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getEndTime()));
            }
        });
        Button addActivityTypeToSMS = findViewById(R.id.add_type_to_sms);
        addActivityTypeToSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsMessageBody.setText(String.format(Locale.getDefault(), "%s %s", smsMessageBody.getText().toString(), ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getActivityType()));
            }
        });

        Button addHyperlinkToSMS = findViewById(R.id.add_hyperlink_to_sms);
        addHyperlinkToSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsMessageBody.setText(String.format(Locale.getDefault(), "%s %s%s", smsMessageBody.getText().toString(), getString(R.string.sms_hyperlink_prefix), ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getEid()));
                Log.v(TAG, String.format(Locale.getDefault(), "%s%s", getString(R.string.sms_hyperlink_prefix), ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getEid()));
            }
        });

        Button send_activity_button = findViewById(R.id.send_activity_button);
        send_activity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("SMS_Message", smsMessageBody.getText().toString());
                returnIntent.putExtra("Activity_Index", activityToBeSharedIndex);
                returnIntent.putExtra("Number_Of_Subsequent_Phone_Numbers", accountIndicesToShareActivityDetailsWith.size());
                for (int i = 0; i < accountIndicesToShareActivityDetailsWith.size(); i++) {
                    returnIntent.putExtra("Phone_Number_" + Integer.toString(i), accounts.get(accountIndicesToShareActivityDetailsWith.get(i)).getPhone());
                }
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        Button cancel_sending_button = findViewById(R.id.cancel_sending_button);
        cancel_sending_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        initActivityProperties();
    }

    /**
     * Used to initialize the various search parameter widgets
     */
    private void initSearchParameterWidgets() {
        final EditText fnameFilterInput = findViewById(R.id.account_fname_filter_input);
        final CheckBox filterByFNameCheckBox = findViewById(R.id.filter_by_account_fname);
        filterByFNameCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                fnameFilterInput.setEnabled(isChecked);
                if (isChecked) {
                    fnameFilterInput.setVisibility(View.VISIBLE);
                } else {
                    fnameFilterInput.setVisibility(View.INVISIBLE);
                }
            }
        });

        final EditText lnameFilterInput = findViewById(R.id.account_lname_filter_input);
        final CheckBox filterByLNameCheckBox = findViewById(R.id.filter_by_account_lname);
        filterByLNameCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                lnameFilterInput.setEnabled(isChecked);
                if (isChecked) {
                    lnameFilterInput.setVisibility(View.VISIBLE);
                } else {
                    lnameFilterInput.setVisibility(View.INVISIBLE);
                }
            }
        });

        ImageButton searchButton = findViewById(R.id.account_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Assemble search parameters
                final CompoundExpr_RTO account_search_parameters = new CompoundExpr_RTO();

                boolean hasParamBeenAdded = false;

                if (filterByFNameCheckBox.isChecked()) {
                    Expr_RTO fname_expression_line = new Expr_RTO();
                    fname_expression_line.set("firstName", "LIKE", "'" + fnameFilterInput.getText().toString() + "'");
                    account_search_parameters.addExpression(null, fname_expression_line);
                    hasParamBeenAdded = true;
                }

                if (filterByLNameCheckBox.isChecked()) {
                    Expr_RTO lname_expression_line = new Expr_RTO();
                    lname_expression_line.set("lastName", "LIKE", "'" + lnameFilterInput.getText().toString() + "'");
                    if (hasParamBeenAdded) {
                        account_search_parameters.addExpression("AND", lname_expression_line);
                    }
                    else {
                        account_search_parameters.addExpression(null, lname_expression_line);
                        hasParamBeenAdded = true;
                    }

                }

                setViewToLoading();

                final boolean searchWithParameter = hasParamBeenAdded;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchWithParameter) {
                            fetchAndUpdateAccountList(account_search_parameters);
                        } else {
                            fetchAndUpdateAccountList(null);
                        }
                        setViewToShareActivity();
                    }
                }).start();
            }
        });
    }

    /**
     * Initialize widgets to display activity data
     */
    private void initActivityProperties() {
        Intent initiationIntent = getIntent();
        if (initiationIntent == null) {
            activityToBeSharedIndex = initiationIntent.getIntExtra("Activity_Index", -1);

            TextView activityBeingSharedName = findViewById(R.id.activity_being_shared_name);
            activityBeingSharedName.setText(ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getShortName());
        }
        else {
            activityToBeSharedIndex = initiationIntent.getIntExtra("Activity_Index", -1);

            TextView activityBeingSharedName = findViewById(R.id.activity_being_shared_name);
            activityBeingSharedName.setText(ViewActivitiesActivity.getActivities().get(activityToBeSharedIndex).getShortName());
        }
    }

    /**
     * Helper method used to fetch the participation status for the fetched activities
     * from the server, and store these participation entities in the participations ArrayList
     */
    private void fetchAndUpdateAccountList(@Nullable CompoundExpr_RTO searchParams) {
        Network_IO_Controller.getInstance().setBaseURLString(getResources().getString(R.string.baseURL));
        Network_IO_Controller.getInstance().initClient(LoginActivity.loggedInUserName, LoginActivity.loggedInPassword);

        try {
            AccountList_RTO list;
            if (searchParams == null) {
                list = Network_IO_Controller.getInstance().getAllAccounts();
            } else {
                list = Network_IO_Controller.getInstance().findAccounts(searchParams);
            }
            accounts = new ArrayList<>(list.getAccounts());
        }
        // If the server is unavailable, show pre-made "demo" accounts
        catch (ProcessingException e) {
            Log.d(TAG, "Unable to fetch accounts from server. Showing demo mode accounts!");
            accounts = new ArrayList<>();

            // Initialize the account list using arbitrary demo accounts
            for (int i = 0; i < 8; i++) {
                Account_RTO demoAccount = new Account_RTO();
                demoAccount.setFirstName("Account");
                demoAccount.setLastName(Integer.toString(i));
                accounts.add(demoAccount);
            }
        }
        // If any other exceptions are thrown, end the activity
        catch (WebApplicationException e) {
            Log.v(TAG, e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }

    /**
     * Add an element to the accountIndicesToShareActivityDetailsWith ArrayList
     * @param index Element to add
     */
    void addAccountIndexToShareActivityWith(int index) {
        accountIndicesToShareActivityDetailsWith.add(index);
    }

    /**
     * Remove an element from the accountIndicesToShareActivityDetailsWith ArrayList
     * @param index Element to remove
     */
    void removeAccountIndexToShareActivityWith(int index) {
        accountIndicesToShareActivityDetailsWith.remove(Integer.valueOf(index));
    }
}
