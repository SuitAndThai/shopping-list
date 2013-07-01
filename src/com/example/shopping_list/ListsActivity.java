package com.example.shopping_list;

import android.content.Context;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
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
    private TextView listTitle;
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
        this.context = this.getApplicationContext();

        // connect to the database
        mdbHelper = new SQLiteHelper(this);

        // Finding our layout elements
        listTitle = (TextView) findViewById(R.id.list_title_text_view);
        listView = (ListView) findViewById(R.id.item_info_list_view);
        final Button addItemButton = (Button) findViewById(R.id.add_item_button);
        final Button addListButton = (Button) findViewById(R.id.add_list_button);
        final Button shakeButton = (Button) findViewById(R.id.shake_button);

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

        shakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShaking();
            }
        });

        //Generate ListView from SQLite Database
        displayListView(this.currentListOrder);

        // Getting sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
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

    private void isShaking() {
        Toast.makeText(getApplicationContext(), "shaking", Toast.LENGTH_SHORT).show();

        ShoppingList list = mdbHelper.getList(currentListOrder);
        mdbHelper.orderListItems(list);
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
            if (mdbHelper.doesItemExist(inputText)) {
                Toast.makeText(this, "You already have " + inputText, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Added " + inputText, Toast.LENGTH_SHORT).show();
                addItem(inputText);
            }
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

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}