package com.example.shopping_list;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.example.database.DBConstants;
import com.example.database.SQLiteHelper;
import com.example.model.Item;
import com.example.model.ShoppingList;

import java.util.ArrayList;

public class ChooseLists extends Activity {

    protected SimpleCursorAdapter dataAdapter;
    protected SQLiteHelper dbHelper;
    protected ListView listView;
    protected ArrayList<String> itemsList;

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

        dbHelper = new SQLiteHelper(this);

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
        Cursor cursor = dbHelper.fetchAllLists();

        // data from the database
        String[] columns = new String[]{DBConstants.ShoppingListsCols.TITLE};

        // destination views for the data from the database
        int[] to = new int[]{R.id.list_title_check_box};

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        dataAdapter = new SimpleCursorAdapter(
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
        listView.setAdapter(dataAdapter);
    }

    public void addToLists(View view) {
        StringBuilder s = new StringBuilder();
        Log.d("chooselists", "clicked the add button");
        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            int key = sparseBooleanArray.keyAt(i);
            if (sparseBooleanArray.get(key)) {
                ShoppingList list = new ShoppingList(dbHelper.getList(key));

                Item item = new Item();
                item.listId = list.id;


                for (String name : itemsList) {
                    s.append(name);
                    item.name = name;
                    if (!dbHelper.itemExists(item)) {
                        dbHelper.addItem(item);
                        Log.d("chooselists", "added " + name + " successfully");
                    }
                }
            }
        }

        Context context = getApplicationContext();
        CharSequence text = s.toString() + "has been added successfully";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();



    }
}
