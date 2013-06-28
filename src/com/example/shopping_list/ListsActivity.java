package com.example.shopping_list;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/24/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListsActivity extends Activity {

    public static final String EXTRA_MESSAGE = "Lists";

    private SimpleCursorAdapter dataAdapter;
    private SQLiteHelper mdbHelper = new SQLiteHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {                                                             SQLiteHelper helper = new SQLiteHelper(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists_activity);


        mdbHelper = new SQLiteHelper(this);
        mdbHelper.open();

        //Generate ListView from SQLite Database
        displayListView();

        final ListView listview = (ListView) findViewById(R.id.listview);
        final Button addButton = (Button) findViewById(R.id.add_button);
        final EditText addEditText = (EditText) findViewById(R.id.add_item_edit_text);

        String[] values = new String[]{"Android", "iPhone", "WindowsMobile"
        };


        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha((float) 0.5)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                view.setAlpha((float) 0.5);
                            }
                        });
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String newItem = addEditText.getText().toString();

                Log.d("ListActivity", newItem);
                adapter.add(newItem);
                adapter.notifyDataSetChanged();
            }
        });


    }

    private void displayListView() {
        // TODO: connect the database to the listview
        // http://www.mysamplecode.com/2012/07/android-listview-cursoradapter-sqlite.html
        //To change body of created methods use File | Settings | File Templates.
    }
}