package com.example.shopping_list;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;

public class MainActivity extends SherlockActivity {
    public static String mt = "main-tag";
    public static int LISTS_REQUEST = 10000;
    public static int BARCODE_REQUEST = 10001;
    public static int RECIPES_REQUEST = 10002;
    public static int SCAN_SUCCESS_REQUEST = 10003;
    public static final String ITEM_INTENT = "ITEM_INTENT";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Button lists_button = (Button) findViewById(R.id.lists_button);
        final Button barcode_button = (Button) findViewById(R.id.barcode_button);
        final Button transfer_button = (Button) findViewById(R.id.add_recipe_button);

        lists_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ListsActivity.class);
                Log.d("main", "before result");
                startActivityForResult(i, LISTS_REQUEST);
            }
        });
        barcode_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
                startActivityForResult(intent, BARCODE_REQUEST);
            }
        });
        transfer_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RecipesActivity.class);
                startActivityForResult(i, RECIPES_REQUEST);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == LISTS_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(mt, "Result OK");
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(mt, "Result Cancelled");
            }
        } else if (requestCode == BARCODE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(mt, "Result OK");
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format , Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
                // Handle successful scan

                Intent i = new Intent(getApplicationContext(), ScanSuccessActivity.class);
                i.putExtra("CONTENT", contents);
                i.putExtra("FORMAT", format);
                startActivityForResult(i, SCAN_SUCCESS_REQUEST);
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(mt, "Result Cancelled");
            }
        } else if (requestCode == RECIPES_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Recipe added", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else {
            Log.d(mt, "Unknown request code " + requestCode);
        }
    }
}
