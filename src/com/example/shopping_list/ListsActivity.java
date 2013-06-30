package com.example.shopping_list;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.*;
import com.example.database.DBConstants;
import com.example.database.SQLiteHelper;
import com.example.model.Item;
import com.example.model.ShoppingList;
import com.example.shopping_list.AddDialog.AddDialogListener;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/24/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListsActivity extends FragmentActivity implements AddDialogListener {

    public static final String EXTRA_MESSAGE = "Lists";

    private SimpleCursorAdapter mDataAdapter;
    private SQLiteHelper mdbHelper;
    private int currentListOrder = 0;
    private TextView listTitle;
    private ListView listView;
    private Context context;

    // TODO: A quick hack, we need to use SharedPreferences instead though
    private int ADD_OBJECT = -1;
    private static int DEFAULT_OBJECT = -1;
    private static int ITEM_OBJECT = 0;
    private static int LIST_OBJECT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Typical Activity calls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists);
        this.context = this.getApplicationContext();

        // connect to the database
        mdbHelper = new SQLiteHelper(this);

        // Finding our layout elements
        listTitle = (TextView) findViewById(R.id.list_title_text_view);
        listView = (ListView) findViewById(R.id.item_info_list_view);
        final Button addItemButton = (Button) findViewById(R.id.add_item_button);
        final Button addListButton = (Button) findViewById(R.id.add_list_button);

        // Set our button listeners to open dialogs
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ADD_OBJECT = ITEM_OBJECT;
                showAddItemDialog();
            }
        });

        addListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ADD_OBJECT = LIST_OBJECT;
                showAddListDialog();
            }
        });

        //Generate ListView from SQLite Database
        displayListView(this.currentListOrder);
    }

    private void showAddListDialog() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AddDialog addListDialog = new AddDialog(AddDialog.ADD_LIST);
        addListDialog.show(fm, "dialog_add_list");
    }


    private void showAddItemDialog() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AddDialog addItemDialog = new AddDialog(AddDialog.ADD_ITEM);
        addItemDialog.show(fm, "dialog_add_item");
    }

    private void addList(String title) {
        //create a list based on the text
        ShoppingList newList = new ShoppingList();
        newList.title = title;
        this.currentListOrder = mdbHelper.addList(newList);

        displayListView(this.currentListOrder);
        refreshView();
    }

    private void addItem(String name) {
        //create an item based on the text
        Item newItem = new Item();
        newItem.name = name;
        newItem.listId = mdbHelper.getList(currentListOrder).id;

        mdbHelper.addItem(newItem);
        refreshView();
    }

    private void displayListView(int listOrder) {
        this.currentListOrder = listOrder;
        ShoppingList list = mdbHelper.getList(this.currentListOrder);
        if (list == null) {
            list = new ShoppingList();
            list.title = getString(R.string.new_list_title);
            list.favorite = ShoppingList.UNFAVORITE;
//            list.order = 0;

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
                Item item = toggleBought(view, cursor);

                // update the DB
                mdbHelper.updateItem(item);

            }
        });

        // Assign adapter to ListView
        listView.setAdapter(mDataAdapter);
    }

    private Item toggleBought(final View view, Cursor cursor) {
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
        return item;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onFinishAddDialog(String inputText) {
        if (ADD_OBJECT == ITEM_OBJECT) {
            Toast.makeText(this, "Added " + inputText, Toast.LENGTH_SHORT).show();
            addItem(inputText);
        } else if (ADD_OBJECT == LIST_OBJECT) {
            Toast.makeText(this, "Started list " + inputText, Toast.LENGTH_SHORT).show();
            addList(inputText);
        } else {
            Toast.makeText(this, "I can't tell if this is an ITEM or a LIST.", Toast.LENGTH_SHORT).show();
        }

        ADD_OBJECT = DEFAULT_OBJECT;
    }

    private void refreshView() {
        ShoppingList list = mdbHelper.getList(currentListOrder);
        if (list == null) {
            list = new ShoppingList();
            list.title = getString(R.string.new_list_title);
            list.favorite = ShoppingList.UNFAVORITE;
            list.order = 0;

            mdbHelper.addList(list);
        }

        Cursor cursor = mdbHelper.fetchAllItems(list);
        this.mDataAdapter.changeCursor(cursor);
    }
}