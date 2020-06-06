/**
 * Assignment Name: Summative - Network Activity Coordinator
 * Date: June 3rd, 2018
 * Author: Sukhveer Sahota
 * <p>
 * This program is the client side of a planning app that has data stored on a server.
 * This program allows the client to log in to their account and see a list of upcoming
 * activities (the user can filter out specific activities using search parameters). The
 * user is able to change properties of the activities, as well as indicate their participation
 * status. The user is also able to compose an SMS message and send it to other accounts stored
 * on the server (this SMS messsage will inform others about the activity).
 */

package com.ics4u.nac;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nac.entity.ActivityList_RTO;
import com.nac.entity.Activity_RTO;
import com.nac.entity.CompoundExpr_RTO;
import com.nac.entity.Expr_RTO;
import com.nac.entity.ParticipationList_RTO;
import com.nac.entity.Participation_RTO;

import java.util.ArrayList;
import java.util.Locale;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

/**
 * This is the ViewActivitiesActivity class for the NAC app. It falls under the View
 * category in the Model-View-Controller paradigm.
 *
 * @author Sukhveer Sahota
 * @version 1.0
 * @since June 3rd, 2018
 */
public class ViewActivitiesActivity extends AppCompatActivity {

    // Tag used for debugging purposes
    private static final String TAG = "ViewActivitiesActivity";

    // Constants used for communication between activities
    private static final int MODIFY_ACTIVITY_PROPERTIES_REQUEST = 1;
    private static final int SHARE_ACTIVITY_REQUEST = 2;
    private static final int LOGIN_REQUEST = 3;

    private static final int SEND_SMS_PERMISSION_RESULT = 4;

    // These store the requested data that is fetched from the server
    private static ArrayList<Activity_RTO> activities;
    private static ArrayList<Participation_RTO> userParticipationInActivities;

    /**
     * Implementation of the onCreate() method, which is used to initialize the
     * various widgets on the UI, as well as initialize the SortController
     *
     * @param savedInstanceState Unused - passed to super constructor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If credentials have not yet been specified, get them
        if (LoginActivity.loggedInUserName == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST);
        }
        // Otherwise, start the activity
        else {
            // Since we are about to do a fetching operation from the server, we display
            // a loading animation to the user
            setViewToLoading();

            // Fetch the data (Network I/O) on a new Thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Update the activity list and the participation status from each account
                    fetchAndUpdateActivityList(null);
                    fetchAndUpdateParticipationListForRequestedActivities();

                    // Display the fetched data (from above) in a scrollable list format
                    setViewToScrollableActivityList();

                    Intent initiationIntent = getIntent();
                    if (initiationIntent.getData() != null) {
                        int activity_to_share_index = Integer.parseInt(initiationIntent.getData().toString().substring(26)) - 1;
                        Intent intent = new Intent(getApplicationContext(), ActivityPropertyActivity.class);
                        intent.putExtra("Activity_Index", activity_to_share_index);
                        startActivityForResult(intent, MODIFY_ACTIVITY_PROPERTIES_REQUEST);
                    }
                }
            }).start();
        }
    }

    /**
     * Helper method used to set the view to the Loading Screen
     */
    private void setViewToLoading() {
        // Ensure that the correct Thread is used, as per the requirements
        // of the Android subsystem.
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
     * Helper method used to set the view to the View Activity Screen
     */
    private void setViewToScrollableActivityList() {
        // Ensure that the correct Thread is used, as per the requirements
        // of the Android subsystem.
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setContentView(R.layout.activity_view_activity);
            initActivityListScrollableView();
            initSearchParameterWidgets();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.activity_view_activity);
                    initActivityListScrollableView();
                    initSearchParameterWidgets();
                }
            });
        }
    }

    /**
     * Helper method used to fetch activities from the server that match a given
     * search criteria, and store these activities in the activities ArrayList
     *
     * @param searchParams The search parameters that filter out which activities are fetched
     */
    private void fetchAndUpdateActivityList(@Nullable CompoundExpr_RTO searchParams) throws ProcessingException {
        Network_IO_Controller.getInstance().setBaseURLString(getResources().getString(R.string.baseURL));
        Network_IO_Controller.getInstance().initClient(LoginActivity.loggedInUserName, LoginActivity.loggedInPassword);

        // Fetch the activities from the server
        try {
            ActivityList_RTO list;
            if (searchParams == null) {
                list = Network_IO_Controller.getInstance().getAllActivities();
                //searchParams = new CompoundExpr_RTO();
                //list = Network_IO_Controller.getInstance().findActivities(searchParams);
            } else {
                list = Network_IO_Controller.getInstance().findActivities(searchParams);
            }
            activities = new ArrayList<>(list.getActivitys());
        }
        // If the server is unavailable, show pre-made "demo" activities
        catch (ProcessingException e) {
            Log.d(TAG, "Unable to fetch activities from server. Showing demo mode activities!");
            activities = new ArrayList<>();

            // Initialize the activity list using arbitrary demo activities
            for (int i = 0; i < 5; i++) {
                Activity_RTO demoActivity = new Activity_RTO();
                demoActivity.setActivityType("Demo Activity" + i);
                demoActivity.setShortName("Demo " + i);
                demoActivity.setStartTime(String.format(Locale.getDefault(), "%d000-0%d-0%d 00:00", i, i, i));
                demoActivity.setEndTime(String.format(Locale.getDefault(), "%d000-0%d-0%d 23:59", i + 1, i + 1, i + 1));
                activities.add(demoActivity);
            }

            if (searchParams != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "In Demo Mode! Cannot filter results (as this functionality is supposed to be handled by the server).", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        // If any other exceptions are thrown, end the activity
        catch (WebApplicationException e) {
            Log.d(TAG, e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }

    /**
     * Helper method used to fetch the participation status for the fetched activities
     * from the server, and store these participation entities in the participations ArrayList
     */
    private void fetchAndUpdateParticipationListForRequestedActivities() throws ProcessingException {
        Network_IO_Controller.getInstance().setBaseURLString(getResources().getString(R.string.baseURL));
        Network_IO_Controller.getInstance().initClient(LoginActivity.loggedInUserName, LoginActivity.loggedInPassword);

        userParticipationInActivities = new ArrayList<>();

        try {
            for (int i = 0; i < activities.size(); i++) {
                // For each activity, compile the search parameter that will get the current account's
                // participation status in that activity
                CompoundExpr_RTO user_participation_search_expression = new CompoundExpr_RTO();
                Expr_RTO single_expression = new Expr_RTO();
                single_expression.set("account", "=", Integer.toString(LoginActivity.loggedInAccountEID));
                user_participation_search_expression.addExpression(null, single_expression);

                single_expression = new Expr_RTO();
                single_expression.set("activity", "=", Integer.toString(activities.get(i).getEid()));
                user_participation_search_expression.addExpression("AND", single_expression);

                // Fetch the participation status
                ParticipationList_RTO participationList = Network_IO_Controller.getInstance().findParticipationEntities(user_participation_search_expression);

                // If there is no participation status, make one (this will later be stored on the
                // server)
                if (participationList.size() == 0) {
                    Participation_RTO emptyParticipation = new Participation_RTO();
                    emptyParticipation.setProbability(0);
                    emptyParticipation.setEngagementType("NO");
                    emptyParticipation.setAccount(Integer.toString(LoginActivity.loggedInAccountEID));
                    emptyParticipation.setActivity(Integer.toString(i + 1));
                    emptyParticipation.setEid(-1);
                    userParticipationInActivities.add(emptyParticipation);
                }
                // Otherwise, the list should only have one element in it, which is the participation
                // status
                else {
                    ArrayList<Participation_RTO> list = new ArrayList<>(participationList.getParticipations());
                    userParticipationInActivities.add(list.get(0));
                }
            }
        }
        // If the server is unavailable, show pre-made "demo" participation status
        catch (ProcessingException e) {
            Log.d(TAG, "Unable to fetch participation status from server. Showing demo mode participation status!");
            userParticipationInActivities = new ArrayList<>();

            // Initialize the participation status list using arbitrary demo participation statuses
            for (int i = 0; i < 5; i++) {
                Participation_RTO demoParticipation = new Participation_RTO();
                demoParticipation.setProbability(0);
                demoParticipation.setEngagementType("NO");
                demoParticipation.setAccount("7");
                demoParticipation.setActivity(Integer.toString(i + 1));
                demoParticipation.setEid(-1);
                userParticipationInActivities.add(demoParticipation);
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
     * Getter method for the activities ArrayList
     *
     * @return ArrayList    The activities ArrayList
     */
    public static ArrayList<Activity_RTO> getActivities() {
        return activities;
    }

    /**
     * Getter method for the userParticipationInActivities ArrayList
     *
     * @return ArrayList    The userParticipationInActivities ArrayList
     */
    public static ArrayList<Participation_RTO> getParticipations() {
        return userParticipationInActivities;
    }

    /**
     * Helper method used to initialize the ListView that displays the scrollable
     * list of activities
     */
    private void initActivityListScrollableView() {
        // Initialize the ListView's onClickListener
        ListView activityListView = findViewById(R.id.activity_list_view);
        activityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ActivityPropertyActivity.class);
                intent.putExtra("Activity_Index", i);
                startActivityForResult(intent, MODIFY_ACTIVITY_PROPERTIES_REQUEST);
            }
        });
        // Initialize the adapter of the list (stores the data to be displayed)
        ActivityAdapter activityAdapter = new ActivityAdapter(this, activities, userParticipationInActivities);
        activityListView.setAdapter(activityAdapter);
    }

    /**
     * Helper method used to initialize the widgets used in the searching
     */
    private void initSearchParameterWidgets() {
        // Initialize the name search parameter widgets
        // and their event listeners
        final EditText nameFilterInput = findViewById(R.id.name_filter_input);
        final CheckBox filterByNameCheckBox = findViewById(R.id.filter_by_name);
        filterByNameCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                nameFilterInput.setEnabled(isChecked);
                // Set the visibility of the input fields based on whether or not the
                // checkbox has been selected/deselected
                if (isChecked) {
                    nameFilterInput.setVisibility(View.VISIBLE);
                } else {
                    nameFilterInput.setVisibility(View.INVISIBLE);
                }
            }
        });

        // ***************** TEMP*******************
        //final Spinner typeFilterInput = findViewById(R.id.type_filter_input);
        //final CheckBox filterByTypeCheckBox = findViewById(R.id.filter_by_type);
        //filterByTypeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        //    @Override
        //    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        //        typeFilterInput.setEnabled(isChecked);
        //        Set the visibility of the input fields based on whether or not the
        //        checkbox has been selected/deselected
        //        if (isChecked) {
        //            typeFilterInput.setVisibility(View.VISIBLE);
        //        } else {
        //            typeFilterInput.setVisibility(View.INVISIBLE);
        //        }
        //    }
        //});
        // ***************** TEMP*******************

        // Initialize the date search parameter widgets
        // and their event listeners
        final EditText dateStartBoundInput = findViewById(R.id.date_start_bound_input);
        final EditText dateEndBoundInput = findViewById(R.id.date_end_bound_input);
        final TextView fromTextView = findViewById(R.id.from_text_view);
        final TextView toTextView = findViewById(R.id.to_text_view);
        final CheckBox filterByDateCheckBox = findViewById(R.id.filter_by_date);
        filterByDateCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                dateStartBoundInput.setEnabled(isChecked);
                dateEndBoundInput.setEnabled(isChecked);
                // Set the visibility of the input fields based on whether or not the
                // checkbox has been selected/deselected
                if (isChecked) {
                    dateStartBoundInput.setVisibility(View.VISIBLE);
                    dateEndBoundInput.setVisibility(View.VISIBLE);
                    fromTextView.setVisibility(View.VISIBLE);
                    toTextView.setVisibility(View.VISIBLE);
                } else {
                    dateStartBoundInput.setVisibility(View.INVISIBLE);
                    dateEndBoundInput.setVisibility(View.INVISIBLE);
                    fromTextView.setVisibility(View.INVISIBLE);
                    toTextView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Iniitialize the search button
        // and its event listener
        ImageButton searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Assemble search parameters
                final CompoundExpr_RTO activity_search_parameters = new CompoundExpr_RTO();

                boolean hasParamBeenAdded = false;

                // Initialize the name search expression
                if (filterByNameCheckBox.isChecked()) {
                    Expr_RTO name_expression_line = new Expr_RTO();
                    name_expression_line.set("shortName", "LIKE", "'" + nameFilterInput.getText().toString() + "'");
                    activity_search_parameters.addExpression(null, name_expression_line);
                    hasParamBeenAdded = true;
                }

                //if (filterByTypeCheckBox.isChecked()) {
                //    Expr_RTO type_expression_line = new Expr_RTO();
                //    type_expression_line.set("activityType", "=", typeFilterInput.getText().toString());
                //    if (!hasParamBeenAdded) {
                //      activity_search_parameters.addExpression("null", type_expression_line);
                //      hasParamBeenAdded = true;
                //    }
                //    else {
                //      activity_search_parameters.addExpression("AND", type_expression_line);
                //    }
                //}

                // Initialize the date search expression
                if (filterByDateCheckBox.isChecked()) {
                    Expr_RTO date_expression_line = new Expr_RTO();
                    date_expression_line.set("startTime", ">=", dateStartBoundInput.getText().toString() + " 00:00");
                    if (!hasParamBeenAdded) {
                        activity_search_parameters.addExpression(null, date_expression_line);
                        hasParamBeenAdded = true;
                    } else {
                        activity_search_parameters.addExpression("AND", date_expression_line);
                    }

                    date_expression_line = new Expr_RTO();
                    date_expression_line.set("endTime", "<=", dateEndBoundInput.getText().toString() + " 23:59");
                    activity_search_parameters.addExpression("AND", date_expression_line);
                }

                setViewToLoading();

                final boolean searchWithParameter = hasParamBeenAdded;
                // Execute the Network I/O on a new Thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchWithParameter) {
                            fetchAndUpdateActivityList(activity_search_parameters);
                        } else {
                            fetchAndUpdateActivityList(null);
                        }
                        fetchAndUpdateParticipationListForRequestedActivities();
                        setViewToScrollableActivityList();
                    }
                }).start();
            }
        });
    }

    /**
     * Called every time we return from an activity. This is used to determine what needs to
     * be updated (both on the client and on the server side)
     *
     * @param requestCode Used to determine which activity we returned from
     * @param resultCode  The result of the activity (cause of its end)
     * @param resultData  Intent storing data from the activity that ended
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent resultData) {
        // Figure out which activity we are returning from
        // and perform the appropriate task in response
        if (requestCode == MODIFY_ACTIVITY_PROPERTIES_REQUEST) {
            if (resultCode == RESULT_OK) {
                final int modifiedActivityIndex = resultData.getIntExtra("Activity_Index", -1);
                activities.get(modifiedActivityIndex).setShortName(resultData.getStringExtra("Activity_Name"));
                activities.get(modifiedActivityIndex).setDescription(resultData.getStringExtra("Activity_Description"));
                activities.get(modifiedActivityIndex).setStartTime(resultData.getStringExtra("Activity_Start_Date_Time"));
                activities.get(modifiedActivityIndex).setEndTime(resultData.getStringExtra("Activity_End_Date_Time"));
                userParticipationInActivities.get(modifiedActivityIndex).setProbability(resultData.getIntExtra("Activity_Participation", 0));
                if (userParticipationInActivities.get(modifiedActivityIndex).getProbability() == 0) {
                    userParticipationInActivities.get(modifiedActivityIndex).setEngagementType("NO");
                } else if (userParticipationInActivities.get(modifiedActivityIndex).getProbability() == 100) {
                    userParticipationInActivities.get(modifiedActivityIndex).setEngagementType("YES");
                } else {
                    userParticipationInActivities.get(modifiedActivityIndex).setEngagementType("PROBABLE");
                }
                ListView activityListView = findViewById(R.id.activity_list_view);
                ((BaseAdapter) activityListView.getAdapter()).notifyDataSetChanged();

                // Execute the Network I/O on a new Thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Network_IO_Controller.getInstance().setBaseURLString(getResources().getString(R.string.baseURL));
                        Network_IO_Controller.getInstance().initClient(LoginActivity.loggedInUserName, LoginActivity.loggedInPassword);

                        try {
                            Network_IO_Controller.getInstance().putActivityOnServer(activities.get(modifiedActivityIndex));

                            if (userParticipationInActivities.get(modifiedActivityIndex).getEid() == -1) {
                                ArrayList<Participation_RTO> list = new ArrayList<>(Network_IO_Controller.getInstance().getAllParticipationEntities().getParticipations());
                                userParticipationInActivities.get(modifiedActivityIndex).setEid(list.size() + 1);
                            }
                            Network_IO_Controller.getInstance().putParticipationEntityOnServer(userParticipationInActivities.get(modifiedActivityIndex));
                        } catch (ProcessingException e) {
                            Log.d(TAG, "Cannot write to server since in demo mode!");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "In Demo Mode! Changes to entities are temporary as there is no server to store the changes.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                }).start();
            }
            /*else if (resultCode == RESULT_FIRST_USER) {
                final Activity_RTO activity = new Activity_RTO();
                activity.setShortName(resultData.getStringExtra("Activity_Name"));
                activity.setDescription(resultData.getStringExtra("Activity_Description"));
                activity.setStartTime(resultData.getStringExtra("Activity_Start_Date_Time"));
                activity.setEndTime(resultData.getStringExtra("Activity_End_Date_Time"));

                final Participation_RTO participation = new Participation_RTO();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        participation = Network_IO_Controller.getInstance().findParticipationEntities()
                    }
                })
                userParticipationInActivities.get(modifiedActivityIndex).setProbability(resultData.getIntExtra("Activity_Participation", 0));
                if (userParticipationInActivities.get(modifiedActivityIndex).getProbability() == 0) {
                    userParticipationInActivities.get(modifiedActivityIndex).setEngagementType("NO");
                } else if (userParticipationInActivities.get(modifiedActivityIndex).getProbability() == 100) {
                    userParticipationInActivities.get(modifiedActivityIndex).setEngagementType("YES");
                } else {
                    userParticipationInActivities.get(modifiedActivityIndex).setEngagementType("PROBABLE");
                }
            }*/
        } else if (requestCode == SHARE_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Send the SMS using the data specified in the resultData Intent
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SmsManager sms = SmsManager.getDefault();
                            String message = resultData.getStringExtra("SMS_Message");
                            int numberOfMessagesToSend = resultData.getIntExtra("Number_Of_Subsequent_Phone_Numbers", 0);
                            for (int i = 0; i < numberOfMessagesToSend; i++) {
                                String phoneNumber = resultData.getStringExtra("Phone_Number_" + Integer.toString(i));
                                sms.sendTextMessage(phoneNumber, null, message, null, null);
                            }
                        }
                    }).start();
                }
                // If permissions are not given, request for them
                else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_RESULT);
                }

            }
        }
        // In response to a successful login, initialize the activities
        else if (requestCode == LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                setViewToLoading();

                // Execute the Network I/O on a new Thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fetchAndUpdateActivityList(null);
                        fetchAndUpdateParticipationListForRequestedActivities();
                        // The activity was not started from SMS
                        setViewToScrollableActivityList();

                        Intent initiationIntent = getIntent();
                        if (initiationIntent.getData() != null) {
                            int activity_to_share_index = Integer.parseInt(initiationIntent.getData().toString().substring(26)) - 1;
                            Intent intent = new Intent(getApplicationContext(), ActivityPropertyActivity.class);
                            intent.putExtra("Activity_Index", activity_to_share_index);
                            startActivityForResult(intent, MODIFY_ACTIVITY_PROPERTIES_REQUEST);
                        }
                    }
                }).start();
            }
        }
    }

    /**
     * Used to request SMS Permissions
     *
     * @param requestCode  The request code
     * @param permissions  The permission that was requested
     * @param grantResults The results of the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == SEND_SMS_PERMISSION_RESULT) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            }
        }
    }

    /**
     * Helper method used to open the ActivityShareActivity
     *
     * @param index The index of the activity to share
     */
    void shareActivityDetails(int index) {
        // Start the ActivityShareActivity using an Intent with the appropriate
        // data specified in it
        Intent intent = new Intent(getApplicationContext(), ActivityShareActivity.class);
        intent.putExtra("Activity_Index", index);
        startActivityForResult(intent, SHARE_ACTIVITY_REQUEST);
    }
}