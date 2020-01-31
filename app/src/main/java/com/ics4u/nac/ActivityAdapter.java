package com.ics4u.nac;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nac.entity.Activity_RTO;
import com.nac.entity.Participation_RTO;

import java.util.ArrayList;

public class ActivityAdapter extends BaseAdapter {

    private ViewActivitiesActivity owner;
    private LayoutInflater layoutInflater;

    private ArrayList<Activity_RTO> activityList;
    private ArrayList<Participation_RTO> participationInActivityList;

    public ActivityAdapter(ViewActivitiesActivity owner, ArrayList<Activity_RTO> activityList, ArrayList<Participation_RTO> participationInActivityList) {
        this.activityList = activityList;
        this.participationInActivityList = participationInActivityList;
        this.owner = owner;
        layoutInflater = (LayoutInflater) owner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return activityList.size();
    }

    @Override
    public Object getItem(int index) {
        return activityList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        View rowView = layoutInflater.inflate(R.layout.activity_list_item, parent, false);

        TextView activity_name_textview = rowView.findViewById(R.id.activity_name_textview);
        activity_name_textview.setText(activityList.get(index).getShortName());

        TextView activity_description_textview = rowView.findViewById(R.id.activity_description_textview);
        activity_description_textview.setText(activityList.get(index).getDescription());

        TextView activity_start_date_time_textview = rowView.findViewById(R.id.start_date_time);
        activity_start_date_time_textview.setText(activityList.get(index).getStartTime());

        TextView activity_end_date_time_textview = rowView.findViewById(R.id.end_date_time);
        activity_end_date_time_textview.setText(activityList.get(index).getEndTime());

        TextView participation_status_textview = rowView.findViewById(R.id.participation_status_textview);
        participation_status_textview.setText(participationInActivityList.get(index).getEngagementType());

        ImageButton shareButton = rowView.findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                owner.shareActivityDetails(index);
            }
        });

        return rowView;
    }


}
