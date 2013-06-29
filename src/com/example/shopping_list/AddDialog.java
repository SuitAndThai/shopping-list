package com.example.shopping_list;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class AddDialog extends DialogFragment implements EditText.OnEditorActionListener {

    private EditText mEditText;
    public static int ADD_ITEM = 0;
    public static int ADD_LIST = 1;
    private int mAddOption;

    public interface AddDialogListener {

        void onFinishAddDialog(String inputText);

    }

    public AddDialog() {
        // empty constructor required for DialogFragment
    }

    public AddDialog(int add_option) {
        this.mAddOption = add_option;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (ADD_ITEM == this.mAddOption) {
            view = inflater.inflate(R.layout.dialog_add_item, container);
            this.mEditText = (EditText) view.findViewById(R.id.add_item_edit_text);
            this.mEditText.setSingleLine();
            getDialog().setTitle(getString(R.string.add_an_item));
        } else {
            view = inflater.inflate(R.layout.dialog_add_list, container);
            this.mEditText = (EditText) view.findViewById(R.id.add_list_edit_text);
            this.mEditText.setSingleLine();
            getDialog().setTitle(getString(R.string.add_list));
        }

        // show the soft keyboard programmatically
        this.mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        this.mEditText.setOnEditorActionListener(this);

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // return the input text to the activity
            AddDialogListener activity = (AddDialogListener) getActivity();
            activity.onFinishAddDialog(this.mEditText.getText().toString());
            this.dismiss();
            return true;
        }

        return false;
    }
}