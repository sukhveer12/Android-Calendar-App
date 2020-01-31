package com.ics4u.nac;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nac.entity.Account_RTO;

import java.util.ArrayList;

public class AccountAdapter extends BaseAdapter {

    private ActivityShareActivity owner;
    private LayoutInflater layoutInflater;

    private ArrayList<Account_RTO> accountList;

    AccountAdapter(ActivityShareActivity owner, ArrayList<Account_RTO> accountList) {
        this.accountList = accountList;
        this.owner = owner;
        layoutInflater = (LayoutInflater) owner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return accountList.size();
    }

    @Override
    public Object getItem(int index) {
        return accountList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        View rowView = layoutInflater.inflate(R.layout.account_list_item, parent, false);

        String accountFullName;
        if (accountList.get(index).getMiddleName().equals("")) {
            accountFullName = accountList.get(index).getFirstName() + " " + accountList.get(index).getLastName();
        }
        else {
            accountFullName = accountList.get(index).getFirstName() + " " + accountList.get(index).getMiddleName() + " " + accountList.get(index).getLastName();
        }

        TextView account_name_textview = rowView.findViewById(R.id.account_name);
        account_name_textview.setText(accountFullName);

        TextView account_phone_number_textview = rowView.findViewById(R.id.account_phone_number);
        account_phone_number_textview.setText(accountList.get(index).getPhone());

        CheckBox accountSelected = rowView.findViewById(R.id.share_with_account_checkbox);
        accountSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    owner.addAccountIndexToShareActivityWith(index);
                }
                else {
                    owner.removeAccountIndexToShareActivityWith(index);
                }
            }
        });

        return rowView;
    }


}
