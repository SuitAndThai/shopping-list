package com.example.shopping_list;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class AddDialog extends DialogFragment implements EditText.OnEditorActionListener {

    protected EditText editText;
    public static int ADD_ITEM = 0;
    public static int ADD_LIST = 1;
    protected int addOption;

    public interface AddDialogListener {

        void onFinishAddDialog(String inputText);

    }

    public AddDialog() {
        // empty constructor required for DialogFragment
    }

    public AddDialog(int add_option) {
        this.addOption = add_option;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (ADD_ITEM == this.addOption) {
            view = inflater.inflate(R.layout.dialog_add_item, container);
            this.editText = (EditText) view.findViewById(R.id.add_item_edit_text);
            this.editText.setSingleLine();
            getDialog().setTitle(getString(R.string.add_an_item));
        } else {
            view = inflater.inflate(R.layout.dialog_add_list, container);
            this.editText = (EditText) view.findViewById(R.id.add_list_edit_text);
            this.editText.setSingleLine();
            getDialog().setTitle(getString(R.string.add_list));
        }

        // show the soft keyboard programmatically
        this.editText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        this.editText.setOnEditorActionListener(this);

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // return the input text to the activity
            AddDialogListener activity = (AddDialogListener) getActivity();
            activity.onFinishAddDialog(this.editText.getText().toString());
            this.dismiss();
            return true;
        }

        return false;
    }
}