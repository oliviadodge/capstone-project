package com.example.olivi.maphap.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.example.olivi.maphap.R;
import com.example.olivi.maphap.utils.Utility;

import java.util.ArrayList;

/**
 * Created by olivi on 1/17/2016.
 */
public class CategoryDialog extends DialogFragment {

    private static final String TAG = CategoryDialog.class.getSimpleName();
    private ArrayList<Integer> mSelectedItems;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
        boolean[] catArray = Utility.getPreferredCategoriesBooleanArray(getActivity());

        for (int i = 0; i < catArray.length; i++) {
            if (catArray[i]) {
                mSelectedItems.add(i);
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the dialog title
        builder.setTitle(R.string.summary_category_preference)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(getResources().getStringArray(R.array
                                .entries_category_preference), Utility
                                .getPreferredCategoriesBooleanArray(getActivity()),
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    Log.i(TAG, "category checked: " + which);
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                        // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        for (int item : mSelectedItems) {
                            Log.i(TAG, "category from dialog: " + item);
                        }
                        Utility.savePreferredCategories(getActivity(), mSelectedItems);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }
}
