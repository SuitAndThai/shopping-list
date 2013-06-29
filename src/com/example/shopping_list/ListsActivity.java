package com.example.shopping_list;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/24/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListsActivity extends Activity {

    public static final String EXTRA_MESSAGE = "Lists";

    private SimpleCursorAdapter mDataAdapter;
    private SQLiteHelper mdbHelper;
    private int currentListOrder = 0;
    private TextView listTitle;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // IF OUR DATABASE ISN'T EMPTY
        setContentView(R.layout.lists_activity);

        // connect to the database
        mdbHelper = new SQLiteHelper(this);

        listTitle = (TextView) findViewById(R.id.list_title_text_view);
        listView = (ListView) findViewById(R.id.item_info_list_view);
        final EditText addItemEditText = (EditText) findViewById(R.id.add_item_edit_text);
        final EditText addListEditText = (EditText) findViewById(R.id.add_list_edit_text);

        // The item is added from th edit text.
        addItemEditText.setImeActionLabel("Add Item", KeyEvent.KEYCODE_ENTER);
        addItemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //get the text
                String name = addItemEditText.getText().toString();


                //create an item based on the text
                Item newItem = new Item();
                newItem.name = name;
                newItem.listId = mdbHelper.getList(currentListOrder).id;

                mdbHelper.addItem(newItem);
                mDataAdapter.notifyDataSetChanged();
                return true;
            }
        });

        // set edit text listener
        addListEditText.setImeActionLabel("Add List", KeyEvent.KEYCODE_ENTER);
        addListEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //get the text
                String title = addListEditText.getText().toString();


                //create a list based on the text
                ShoppingList newList = new ShoppingList();
                newList.title = title;

                mdbHelper.addList(newList);

                return true;
            }
        });

//        final ArrayList<String> list = new ArrayList<String>();
//        for (int i = 0; i < values.length; ++i) {
//            list.add(values[i]);
//        }

        //Generate ListView from SQLite Database
        displayListView(currentListOrder);


    }

    private void displayListView(int listOrder) {

        ShoppingList list = mdbHelper.getList(listOrder);
        if (list == null) {
            list = new ShoppingList();
            list.title = getString(R.string.new_list_title);
            list.favorite = ShoppingList.UNFAVORITE;
            list.order = 0;

            mdbHelper.addList(list);
        }

        Cursor cursor = mdbHelper.fetchAllItems(list);

        // data from the database
        String[] columns = new String[]{DBConstants.ItemsCols.NAME};

        // destination views for the data from the database
        int[] to = new int[]{R.id.item_info_text_view};

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        mDataAdapter = new SimpleCursorAdapter(
                this, R.layout.item_info,
                cursor,
                columns,
                to,
                0);

        listTitle.setText(list.title);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                // get whether the item is bought
                Item item = new Item();
                item.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._ID));
                item.name = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.NAME));
                item.status = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.STATUS));
                item.order = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.ITEM_ORDER));
                item.listId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._SHOPPING_LIST_ID));

                // animate so that it turns visible/fades away
                if (item.status == Item.IS_BOUGHT) {
                    // make it unbought
                    item.status = Item.UNBOUGHT;

                    view.animate().setDuration(2000).alpha((float) 0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mDataAdapter.notifyDataSetChanged();
                                    view.setAlpha((float) 0);
                                }
                            });
                } else {
                    // make it bought
                    item.status = Item.IS_BOUGHT;

                    view.animate().setDuration(2000).alpha((float) 0.5)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mDataAdapter.notifyDataSetChanged();
                                    view.setAlpha((float) 0.5);
                                }
                            });
                }

                // update the DB
                mdbHelper.updateItem(item);

            }
        });

        // Assign adapter to ListView
        listView.setAdapter(mDataAdapter);
    }

}