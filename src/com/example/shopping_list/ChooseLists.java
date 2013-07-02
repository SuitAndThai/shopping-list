package com.example.shopping_list;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.example.database.DBConstants;
import com.example.database.SQLiteHelper;
import com.example.model.Item;
import com.example.model.ShoppingList;

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
    private ArrayList<String> itemsList;

    SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lists);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.choose_list_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        final Button addToListButton = (Button) findViewById(R.id.add_to_list_button);

        Intent intent = getIntent();
        itemsList = new ArrayList<String>(intent.getStringArrayListExtra(MainActivity.ITEM_INTENT));

        mdbHelper = new SQLiteHelper(this);

        displayListView();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
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
        };

        // Assign adapter to ListView
        listView.setAdapter(mDataAdapter);
    }

    public void addToLists(View view) {
//        int listCount = listView.getCount();
//        Log.d("listCount: ", listCount + "");
//        Log.d("chooselists: size of bool array is ", String.valueOf(sparseBooleanArray.size()));

        Log.d("chooselists", "clicked the add button");
        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            int key = sparseBooleanArray.keyAt(i);
            if (sparseBooleanArray.get(key)) {
                ShoppingList list = new ShoppingList(mdbHelper.getList(key));

                Item item = new Item();
                item.listId = list.id;

                for (String name : itemsList) {
                    Log.d("chooselists", "adding " + name + " to the list");
                    item.name = name;
                    if (!mdbHelper.itemExists(item)) {
                        mdbHelper.addItem(item);
                        Log.d("chooselists", "added " + name + " successfully");
                    }
                }
            }
        }

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


}
