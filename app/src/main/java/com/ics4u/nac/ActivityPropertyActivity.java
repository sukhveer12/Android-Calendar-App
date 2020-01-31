package com.ics4u.nac;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;


import com.nac.entity.ActivityList_RTO;
import com.nac.entity.Activity_RTO;
import com.nac.entity.Participation_RTO;


import java.util.Locale;

/**
 * This is the ActivityShareActivity class for the NAC app. It falls under the View
 * category in the Model-View-Controller paradigm. It is used when the user wants
 * to send a notification regarding some activity via SMS to other users.
 *
 * @author Sukhveer Sahota
 * @version 1.0
 * @since June 3rd, 2018
 */
public class ActivityPropertyActivity extends AppCompatActivity {

    // Used for debugging
    private static final String TAG = "ActivityPrpertyActivity";

    private int targetActivityIndex;
    private boolean fromSMS;
    private int targetActivityEID;

    /**
     * Implementation of the onCreate() method, which is used to initialize the
     * various widgets on the UI, as well as initialize the SortController
     *
     * @param savedInstanceState Unused - passed to super constructor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_property);

        initActivityProperties();
        initUIListeners();
    }

    /**
     * Helper method used to initialize the various widgets that display the
     * properties of the selected activity
     */
    private void initActivityProperties() {
        Intent initiationIntent = getIntent();
        //Uri data = initiationIntent.getData();

        Activity_RTO targetActivity;
       // if (data == null) {
            fromSMS = false;
            targetActivityIndex = initiationIntent.getIntExtra("Activity_Index", -1);
            targetActivity = ViewActivitiesActivity.getActivities().get(targetActivityIndex);
        //}
        //else {
        //    fromSMS = true;
        //    Log.v(TAG, data.getPath());
        //    targetActivityEID = Integer.parseInt(data.getPath());
        //    targetActivity = Network_IO_Controller.getInstance().getActivityFromEID(targetActivityEID);
        //}

        EditText activity_name_input = findViewById(R.id.activity_name_input);
        activity_name_input.setText(targetActivity.getShortName());

        EditText activity_description_input = findViewById(R.id.activity_description_input);
        activity_description_input.setText(targetActivity.getDescription());

        // Initialize start time and date
        String startTimeAndDate = targetActivity.getStartTime();

        EditText start_time_input = findViewById(R.id.start_time_input);
        start_time_input.setText(extractTimeFromDateTime(startTimeAndDate));

        EditText start_date_input = findViewById(R.id.start_date_input);
        start_date_input.setText(extractDateFromDateTime(startTimeAndDate));

        // Initialize end time and date
        String endTimeAndDate = targetActivity.getEndTime();

        EditText end_time_input = findViewById(R.id.end_time_input);
        end_time_input.setText(extractTimeFromDateTime(endTimeAndDate));

        EditText end_date_input = findViewById(R.id.end_date_input);
        end_date_input.setText(extractDateFromDateTime(endTimeAndDate));

        // Update the participation status
        Participation_RTO userParticipationInActivity = ViewActivitiesActivity.getParticipations().get(targetActivityIndex);
        TextView probability_going_textview = findViewById(R.id.probability_going_textview);
        probability_going_textview.setText(String.format(Locale.getDefault(), "%d", 5 * (userParticipationInActivity.getProbability() / 5)));

        SeekBar maybe_probability_seekbar = findViewById(R.id.maybe_probability_seekbar);
        RadioButton going_radio_button = findViewById(R.id.going_radio_button);
        RadioButton maybe_radio_button = findViewById(R.id.maybe_radio_button);
        RadioButton not_going_radio_button = findViewById(R.id.not_going_radio_button);

        if (userParticipationInActivity.getProbability() == 0) {
            not_going_radio_button.setChecked(true);
            maybe_probability_seekbar.setEnabled(false);
        } else if (userParticipationInActivity.getProbability() == 100) {
            going_radio_button.setChecked(true);
            maybe_probability_seekbar.setEnabled(false);
        } else {
            maybe_radio_button.setChecked(true);
            maybe_probability_seekbar.setProgress(userParticipationInActivity.getProbability() / 5);
        }
    }

    /**
     * Initialize the event listeners of the various UI widgets
     */
    private void initUIListeners() {
        final TextView probability_going_textview = findViewById(R.id.probability_going_textview);
        final SeekBar maybe_probability_seekbar = findViewById(R.id.maybe_probability_seekbar);
        final RadioButton going_radio_button = findViewById(R.id.going_radio_button);
        final RadioButton maybe_radio_button = findViewById(R.id.maybe_radio_button);
        final RadioButton not_going_radio_button = findViewById(R.id.not_going_radio_button);

        maybe_probability_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                probability_going_textview.setText(String.format(Locale.getDefault(), "%d", 5 * i));
                if (i == 0) {
                    maybe_probability_seekbar.setEnabled(false);
                    not_going_radio_button.setChecked(true);
                    maybe_radio_button.setChecked(false);
                }
                else if (i == 20) {
                    maybe_probability_seekbar.setEnabled(false);
                    going_radio_button.setChecked(true);
                    maybe_radio_button.setChecked(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        going_radio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maybe_radio_button.setChecked(false);
                not_going_radio_button.setChecked(false);
                maybe_probability_seekbar.setEnabled(false);
                probability_going_textview.setText(String.format(Locale.getDefault(), "%d", 100));
            }
        });

        maybe_radio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                going_radio_button.setChecked(false);
                not_going_radio_button.setChecked(false);
                maybe_probability_seekbar.setEnabled(true);
                maybe_probability_seekbar.setProgress(10);
                probability_going_textview.setText(String.format(Locale.getDefault(), "%d", 50));
            }
        });

        not_going_radio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                going_radio_button.setChecked(false);
                maybe_radio_button.setChecked(false);
                maybe_probability_seekbar.setEnabled(false);
                probability_going_textview.setText(String.format(Locale.getDefault(), "%d", 0));
            }
        });

        final EditText activity_name_input = findViewById(R.id.activity_name_input);
        final EditText activity_description_input = findViewById(R.id.activity_description_input);
        final EditText start_time_input = findViewById(R.id.start_time_input);
        final EditText start_date_input = findViewById(R.id.start_date_input);
        final EditText end_time_input = findViewById(R.id.end_time_input);
        final EditText end_date_input = findViewById(R.id.end_date_input);

        Button save_button = findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Activity_Name", activity_name_input.getText().toString());
                returnIntent.putExtra("Activity_Description", activity_description_input.getText().toString());
                returnIntent.putExtra("Activity_Start_Date_Time", start_date_input.getText().toString() + " " + start_time_input.getText().toString());
                returnIntent.putExtra("Activity_End_Date_Time", end_date_input.getText().toString() + " " + end_time_input.getText().toString());
                returnIntent.putExtra("Activity_Participation", Integer.parseInt(probability_going_textview.getText().toString()));
                //if (!fromSMS) {
                    returnIntent.putExtra("Activity_Index", targetActivityIndex);
                    setResult(RESULT_OK, returnIntent);
                //} else {
                //    returnIntent.putExtra("Activity_EID", targetActivityEID);
                //    setResult(RESULT_FIRST_USER, returnIntent);
                //}
                finish();
            }
        });

        Button cancel_button = findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    /**
     * Helper method used to get just the time from a timeAndDate String
     * in the format YYYY-MM-DD HH-MM
     * @param timeAndDate String in the format YYYY-MM-DD HH:MM
     * @return Time in the format HH:MM
     */
    private String extractTimeFromDateTime(String timeAndDate) {
        return timeAndDate.substring(11);
    }

    /**
     * Helper method used to get just the date from a timeAndDate String
     * in the format YYYY-MM-DD HH-MM
     * @param timeAndDate String in the format YYYY-MM-DD HH:MM
     * @return Date in the format YYYY-MM-DD
     */
    private String extractDateFromDateTime(String timeAndDate) {
        return timeAndDate.substring(0, 10);
    }
}