package com.example.shopping_list;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.example.database.DBConstants;
import com.example.database.SQLiteHelper;
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
    private CheckBox listCheckBox;
    //private ArrayList<Count>

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lists);

        listView = (ListView) findViewById(R.id.choose_list_list_view);
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
                to);

        ArrayList<ShoppingList> listArray = new ArrayList<ShoppingList>();
        int listCount = listView.getCount();
//        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//        {
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long i)
//            {
//                BrowserAdapter bla = (BrowserAdapter) parent.getAdapter();
//                bla.setCheckedItem(item);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> arg0) {
//                // TODO Auto-generated method stub
//
//            }
//        });

        // Assign adapter to ListView
        listView.setAdapter(mDataAdapter);
    }

//    private class MyAdapter extends BaseAdapter{
//        public HashMap<String,String> checked = new HashMap<String,String>();
//
//        public void setCheckedItem(int item) {
//
//
//            if (checked.containsKey(String.valueOf(item))){
//                checked.remove(String.valueOf(item));
//            }
//
//            else {
//                checked.put(String.valueOf(item), String.valueOf(item));
//            }
//        }
//        public HashMap<String, String> getCheckedItems(){
//            return checked;
//        }
//
//        @Override
//        public int getCount() {
//            return 0;  //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        public String getItem(int n){
//            return null;
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return 0;  //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        @Override
//        public View getView(int i, View view, ViewGroup viewGroup) {
//            return null;  //To change body of implemented methods use File | Settings | File Templates.
//        }
//    }

//    private void checkButtonClick() {
//
//
//        Button myButton = (Button) findViewById(R.id.add_to_list_button);
//        myButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                StringBuffer responseText = new StringBuffer();
//                responseText.append("The following were selected...\n");
//
//                ArrayList<ShoppingList> lists = mdbHelper.getListArray();
//                for (int i = 0; i < lists.size(); i++) {
//                    ShoppingList shoppingList = lists.get(i);
//                    if (shoppingList.isSelected()) {
//                        responseText.append("\n" + ShoppingList.getName());
//                    }
//                }
//
//                Toast.makeText(getApplicationContext(),
//                        responseText, Toast.LENGTH_LONG).show();
//
//            }
//        });
//
//    }


}
