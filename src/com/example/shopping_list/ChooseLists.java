package com.example.shopping_list;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.*;
import com.example.database.DBConstants;
import com.example.database.SQLiteHelper;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: sizhao
 * Date: 6/30/13
 * Time: 8:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChooseLists extends Activity {

    private SimpleCursorAdapter mDataAdapter;
    private SQLiteHelper mdbHelper;
    private ListView listView;

    SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lists);

        listView = (ListView) findViewById(R.id.choose_list_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        final Button addToListButton = (Button) findViewById(R.id.add_to_list_button);


        mdbHelper = new SQLiteHelper(this);

        displayListView();

    }

    private void displayListView() {

        Cursor cursor = mdbHelper.fetchAllLists();

        // data from the database
        String[] columns = new String[]{DBConstants.ShoppingListsCols.TITLE};

        // destination views for the data from the database
        int[] to = new int[]{R.id.list_title_check_box};

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        mDataAdapter = new SimpleCursorAdapter(
                this, R.layout.list_info,
                cursor,
                columns,
                to) {
            @Override
            public void bindView(View view, Context context, final Cursor cursor) {
                super.bindView(view, context, cursor);
                final int position = cursor.getPosition();
                final CheckBox listCheckBox = (CheckBox) view.findViewById(R.id.list_title_check_box);
                listCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        sparseBooleanArray.put(position, isChecked);
                    }
                });
            }
//
//            @Override
//            public View newView(Context context, final Cursor cursor, ViewGroup parent) {
//
//                View v = super.newView(context, cursor, parent);
//
//                return v;
//            }
        };

        // Assign adapter to ListView
        listView.setAdapter(mDataAdapter);
    }

    public void addToLists(View view) {
        ArrayList<Integer> checkedOrder = new ArrayList<Integer>();
        int listCount = listView.getCount();
        Log.d("listCount: ", listCount + "");

        String s = "";

        Log.d("SparseBooleanArraySize: ", String.valueOf(sparseBooleanArray.size()));

        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            Log.d("chooselists", "checked item"+i+" is " + sparseBooleanArray.get(i));
//            Toast.makeText(this, "checked item is " + sparseBooleanArray.get(i), Toast.LENGTH_SHORT).show();
        }

    }



}
