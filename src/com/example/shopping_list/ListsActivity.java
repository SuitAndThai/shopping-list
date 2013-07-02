package com.example.shopping_list;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.database.DBConstants;
import com.example.database.ItemAdapter;
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

    //    private SimpleCursorAdapter mDataAdapter;
    private ItemAdapter mDataAdapter;
    private SQLiteHelper mdbHelper;
    private int currentListOrder = 0;
    private Button prevList;
    private Button favButton;
    private EditText listTitle;
    private Button nextList;
    private ListView listView;
    private Context context;
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private static final float mThreshold = 10f;


    // TODO: A quick hack, we need to use SharedPreferences instead though
    private int ADD_OBJECT = -1;
    private static int DEFAULT_OBJECT = -1;
    private static int ITEM_OBJECT = 0;
    private static int LIST_OBJECT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Typical Activity calls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists_pager);

        android.app.ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);




        this.context = this.getApplicationContext();

        // connect to the database
        mdbHelper = new SQLiteHelper(this);

        // Finding our layout elements
        prevList = (Button) findViewById(R.id.left_arrow_button);
        favButton = (Button) findViewById(R.id.favorite);
        listTitle = (EditText) findViewById(R.id.list_title_edit_text);
        nextList = (Button) findViewById(R.id.right_arrow_button);
        listView = (ListView) findViewById(R.id.item_info_list_view);
        final Button addItemButton = (Button) findViewById(R.id.add_item_button);
        final Button addListButton = (Button) findViewById(R.id.add_list_button);

        // Set our button listeners to open dialogs
        prevList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mdbHelper.listExists(currentListOrder - 1)) {
                    currentListOrder = currentListOrder - 1;
                    displayListView(currentListOrder);
                    refreshView();
                }
            }
        });

        nextList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mdbHelper.listExists(currentListOrder + 1)) {
                    currentListOrder = currentListOrder + 1;
                    displayListView(currentListOrder);
                    refreshView();
                }
            }
        });



        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });


        listTitle.setSingleLine();
        listTitle.setCursorVisible(false);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        listTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listTitle.setCursorVisible(true);
            }
        });
        listTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    // get text
                    String input = v.getText().toString();

                    // check to see if it already exists
                    ShoppingList list;
                    if (mdbHelper.listExists(input)) {
                        list = mdbHelper.getList(currentListOrder);
                        Toast.makeText(context, input + " already exists", Toast.LENGTH_SHORT);
                        v.setText(list.title);
                    } else {
                        list = mdbHelper.getList(currentListOrder);
                        list.title = input;
                        mdbHelper.updateList(list);
                    }
                    v.setSelected(false);
                    v.setCursorVisible(false);

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    mdbHelper.orderLists();
                    return true;
                }
                return false;
            }
        });
        listTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deleteListDialog();
                return true;
            }
        });

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
        refreshView();

        // Getting sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;



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




    private void deleteListDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ListsActivity.this);

        String dialogTitle = getResources().getString(R.string.delete_ze_list);
        String positiveText = getResources().getString(R.string.delete);
        String negativeText = getResources().getString(R.string.cancel);


        // set title
        alertDialogBuilder.setTitle(dialogTitle);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mdbHelper.deleteList(currentListOrder);

                        // check if we need to update the current list order
                        if (!mdbHelper.listExists(currentListOrder)) {

                            // if it doesn't exist, check to see if there's something previous
                            if (mdbHelper.listExists(currentListOrder - 1)) {
                                currentListOrder = currentListOrder - 1;
                            }
                        }
                        displayListView(currentListOrder);
                        refreshView();
//                        finish();
                    }
                })
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            // if we're shaking
            if (delta > mThreshold) {
                isShaking();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void toggleFavorite() {
        ShoppingList list = mdbHelper.getList(currentListOrder);
        if (list.favorite == ShoppingList.FAVORITE) {
            Drawable d = getResources().getDrawable(R.drawable.snowflake_transparent_2);
            favButton.setBackground(d);
            list.favorite = ShoppingList.UNFAVORITE;
        } else {
            Drawable d = getResources().getDrawable(R.drawable.snowflake);
            favButton.setBackground(d);
            list.favorite = ShoppingList.FAVORITE;
        }

        mdbHelper.updateList(list);
        mdbHelper.orderLists();
        ShoppingList newList = mdbHelper.getList(list.title);
        currentListOrder = newList.order;
    }

    private void isShaking() {
        Toast.makeText(getApplicationContext(), "let's shuffle it a bit", Toast.LENGTH_LONG).show();

        ShoppingList list = mdbHelper.getList(currentListOrder);
        mdbHelper.orderListItems(list);
        displayListView(currentListOrder);
        refreshView();
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
        mdbHelper.addList(newList);
        ShoppingList list = mdbHelper.getList(title);
        currentListOrder = list.order;

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

            mdbHelper.addList(list);
        }

        Cursor cursor = mdbHelper.fetchAllItems(list);

        // data from the database
        String[] columns = new String[]{DBConstants.ItemsCols.NAME};

        // destination views for the data from the database
        int[] to = new int[]{R.id.item_info_text_view};

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        mDataAdapter = new ItemAdapter(
                this, R.layout.item_info,
                cursor,
                columns,
                to);
        listTitle.setText(list.title);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                toggleBought(view, cursor, position);
            }
        });

        // Assign adapter to ListView
        listView.setAdapter(mDataAdapter);
    }

    private void toggleBought(final View view, Cursor cursor, int position) {
        // get whether the item is bought
        Item item = new Item();

        item.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._ID));
        item.status = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.STATUS));

        // animate so that it turns visible/fades away
        if (item.status == Item.BOUGHT) {
            view.animate().setDuration(2000).alpha((float) 1)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            view.setAlpha((float) 1);
                        }
                    });
        } else {
            view.animate().setDuration(2000).alpha((float) 0.3)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            view.setAlpha((float) 0.3);
                        }
                    });
        }
        this.mdbHelper.toggleItem(item);
        refreshView();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayListView(this.currentListOrder);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onFinishAddDialog(String inputText) {
        if (ADD_OBJECT == ITEM_OBJECT) {
            if (mdbHelper.itemExists(inputText)) {
                Toast.makeText(this, "You already have " + inputText, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Added " + inputText, Toast.LENGTH_SHORT).show();
                addItem(inputText);
            }
        } else if (ADD_OBJECT == LIST_OBJECT) {
            if (mdbHelper.listExists(inputText)) {
                Toast.makeText(this, inputText + " already exists", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Started list " + inputText, Toast.LENGTH_SHORT).show();
                addList(inputText);
            }
        } else {
            Toast.makeText(this, "I can't tell if this is an ITEM or a LIST.", Toast.LENGTH_SHORT).show();
        }
        ADD_OBJECT = DEFAULT_OBJECT;
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
        if (list.favorite == ShoppingList.FAVORITE) {
            favButton.setBackground(getResources().getDrawable(R.drawable.snowflake));
        } else {
            favButton.setBackground(getResources().getDrawable(R.drawable.snowflake_transparent_2));
        }
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}