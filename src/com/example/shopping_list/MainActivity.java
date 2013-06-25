package com.example.shopping_list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static String mt = "main_tag";
    public static int LISTS_REQUEST = 10000;
    public static int BARCODE_REQUEST = 10001;
    public static int XFER_REQUEST = 10002;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button lists_button = (Button) findViewById(R.id.lists_button);
        final Button barcode_button = (Button) findViewById(R.id.barcode_button);
        final Button transfer_button = (Button) findViewById(R.id.transfer_receive_button);

        lists_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ListsActivity.class);
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
                Intent i = new Intent(getApplicationContext(), ListsActivity.class);
                startActivityForResult(i, LISTS_REQUEST);
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
        }
        else if (requestCode == BARCODE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(mt, "Result OK");
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format , Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
                // Handle successful scan
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(mt, "Result Cancelled");
            }
        }
        else if (requestCode == XFER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(mt, "Result OK");
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(mt, "Result Cancelled");
            }
        }
        else {
               Log.d(mt, "Unknown request code " + requestCode);
        }
    }
}
