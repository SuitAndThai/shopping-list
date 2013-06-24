package com.example.shopping_list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;

public class MyActivity extends Activity {
    public static String mt = "main_tag";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button lists_button = (Button) findViewById(R.id.lists_button);

        lists_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // start a new intent
                Intent i = new Intent(getApplicationContext(), lists.class);

                Log.e(mt, "First Screen");
                startActivity(i);
        }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {

    }
}
